# swapi-service — a writable Star Wars API

The public [SWAPI](https://swapi.info) mirrors are **read-only and static**: they ignore query
parameters (`?search=luke` returns all 82 people) and accept no writes. So nothing built on them can
demonstrate a complete CRUD.

This serves the same data model — 6 collections, 260 rows, references and all — with **search,
filters, ranges, paging, writes and real references between tables**, over an in-memory H2 seeded at
boot from the original dataset.

**Live:** <https://swapi.ec1.mateu.io> · **Contract:** [`/swagger-ui.html`](https://swapi.ec1.mateu.io/swagger-ui.html) · [`/v3/api-docs`](https://swapi.ec1.mateu.io/v3/api-docs)

## The data model

```
Planet ──1:N── Person ──M:N── Species          Film ──M:N── Person   (characters)
   │              │              │                │
   └──── homeworld ┘              └── homeworld ───┘   ──M:N── Planet, Starship, Vehicle, Species
                  │
                  └──M:N── Vehicle, Starship   (pilots)
```

| collection | rows | owns |
|---|---|---|
| `/api/planets` | 60 | — |
| `/api/people` | 82 | `homeworld`, `species` |
| `/api/species` | 37 | `homeworld` |
| `/api/vehicles` | 39 | `pilots` |
| `/api/starships` | 36 | `pilots` |
| `/api/films` | 6 | `characters`, `planets`, `starships`, `vehicles`, `species` |

An association is written on the side that **owns** it: a film's cast on the film, a person's species
on the person. The other side is read-only — a planet lists its `residentIds`, but a resident moves
by changing their own `homeworldId`.

## Reading

```bash
GET /api/people?search=sky                  # free text over the collection's text fields
GET /api/people?gender=female               # a declared filter
GET /api/people?gender=male&gender=female   # repeated = OR (what a multi-select means)
GET /api/people?height_from=150&height_to=175   # inclusive range
GET /api/people?homeworldId=1               # by reference
GET /api/films?releaseDate_from=1990-01-01&sort=releaseDate
GET /api/people?page=2&size=50
```

Every condition is evaluated **by the database** — that is the point of cloning the API rather than
reading a mirror.

The response is an envelope, because a bare array cannot say how many rows matched:

```json
{ "content": [ … ], "totalElements": 82, "totalPages": 5, "page": 0, "size": 20 }
```

A **filter the collection does not declare is a 400**, never a silently unfiltered page — answering
200 with everything to a misspelled parameter is a bug a caller only finds in production. The message
lists what is supported. Each collection's filters are in its `querySpec()` and in the OpenAPI.

References come back as ids, plus the display name for the single-valued ones:

```json
{ "id": 1, "name": "Luke Skywalker", "homeworldId": 1, "homeworldName": "Tatooine",
  "filmIds": [1, 2, 3, 6], "starshipIds": [12, 7], "url": "/api/people/1" }
```

The name is there so a table renders without a request per row. To-many associations carry ids only —
names would balloon a film's payload, and a caller who wants them asks the other collection.

## Writing

Reads are open. **Writes need the `X-Api-Key` header.** It is not authentication — it does not say
who is writing — but it is the difference between "you have to be told about it" and "a crawler can
empty the database".

```bash
curl -X POST https://swapi.ec1.mateu.io/api/people \
  -H 'Content-Type: application/json' -H 'X-Api-Key: …' \
  -d '{"name":"Mara Jade","height":178,"gender":"female","homeworldId":1,"speciesIds":[1]}'
```

`POST` → 201 with a `Location`; `PUT /{id}` replaces; `DELETE /{id}` → 204. A write pointing at a row
that does not exist (`homeworldId: 9999`) is a **400** — what is missing is inside the body, not the
address.

**The database is in memory and re-seeded on every restart**, so anything written here is demo data
with a short life. That is deliberate: a public, writable demo had better be able to forget.

## Departures from the original data

The source serves every number as a string and uses `"unknown"`, `"n/a"` and `"none"` for missing
values. Here the numeric fields are **real typed columns** and a value that does not parse is `NULL`
— which is what it means, and which is what lets the database answer a range query and a sort. Two
exceptions kept as text on purpose: `crew`, whose values are ranges like `"30-165"`, and `birthYear`,
which is `"19BBY"`.

`climate` and `terrain` are comma-joined lists in the source (`"hot, humid"`), so they filter by
containment; `gender`, `classification` and the colours are closed sets and filter exactly.

## Running it locally

```bash
mvn spring-boot:run -Dspring-boot.run.arguments=--swapi.write-key=localdev
# → http://localhost:8120/swagger-ui.html
```

Without a write key configured **every write is refused with 401** — a deployment that forgot to set
one should be inert, not wide open.

```bash
mvn test        # 17 integration tests over the real seeded dataset
```

## Deploying

No CI: the image is built and the manifests applied from a laptop, like everything else on this
cluster.

```bash
./deploy/build-image.sh 0.1.0    # mvn package + docker buildx --platform linux/amd64 --push
./deploy/deploy.sh               # namespace, secret, deployment, service, ingress
```

`deploy.sh` generates the write key on the first run into `deploy/.secrets/credentials.env` (git-ignored)
and never rotates it afterwards — a rotated key would silently break every client that already has one.

It runs in its own `swapi` namespace, deliberately apart from `ec-demo1`: a public API anybody may
write to should not share a blast radius with the demo platform. It is **not** behind that cluster's
gateway, because the whole point is that a page with no token can call it. The `*.ec1.mateu.io`
wildcard already resolves to the ingress, so there is no DNS step; cert-manager issues the certificate
on the first request.

One replica, `strategy: Recreate` — with the database in memory, two replicas would be two different
databases answering the same host at random.
