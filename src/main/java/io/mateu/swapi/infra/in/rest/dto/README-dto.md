The wire shape. Two rules, both there so a client can render a table without a second round trip:

- a reference travels as its ID **and** its display name (`homeworldId` + `homeworldName`), because a
  listing has to put something readable in a cell and cannot fetch a planet per row;
- a to-many association travels as a list of IDs only. Names would balloon the payload of a film
  carrying its whole cast, and a caller that wants them asks the other collection with `?id=`.

On a write only the IDs are read; the names are output-only.
