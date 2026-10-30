package io.mateu.swapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/**
 * The API's behaviour, pinned against the real seeded dataset. These are the claims a client is
 * entitled to rely on: what the numbers are, what filters mean, what a write does to a reference,
 * and which failures are the caller's fault rather than the server's.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "swapi.write-key=testkey")
class SwapiApiTest {

  private static final String KEY = "X-Api-Key";

  @Autowired private MockMvc mvc;

  @Test
  @DisplayName("the whole dataset is seeded at boot, references included")
  void seeded() throws Exception {
    mvc.perform(get("/api/people")).andExpect(jsonPath("$.totalElements").value(82));
    mvc.perform(get("/api/planets")).andExpect(jsonPath("$.totalElements").value(60));
    mvc.perform(get("/api/films")).andExpect(jsonPath("$.totalElements").value(6));
    mvc.perform(get("/api/species")).andExpect(jsonPath("$.totalElements").value(37));
    mvc.perform(get("/api/vehicles")).andExpect(jsonPath("$.totalElements").value(39));
    mvc.perform(get("/api/starships")).andExpect(jsonPath("$.totalElements").value(36));

    // A New Hope keeps the cast the source data gave it — the two-pass link ran.
    mvc.perform(get("/api/films").param("episodeId", "4"))
        .andExpect(jsonPath("$.content[0].title").value("A New Hope"))
        .andExpect(jsonPath("$.content[0].characterIds.length()").value(18))
        .andExpect(jsonPath("$.content[0].planetIds.length()").value(3));
  }

  @Test
  @DisplayName("every record carries a generated picture (DiceBear, CC0), drawn from its own data")
  void images() throws Exception {
    mvc.perform(get("/api/people/1"))
        .andExpect(jsonPath("$.image").value(org.hamcrest.Matchers.allOf(
            org.hamcrest.Matchers.startsWith("https://api.dicebear.com/9.x/pixel-art/png?size=256&seed=Luke+Skywalker"),
            org.hamcrest.Matchers.containsString("skinColor=ffdbac"), // fair
            org.hamcrest.Matchers.containsString("hairColor=e8c872"), // blond
            org.hamcrest.Matchers.containsString("eyesColor=2f6fd8")))); // blue
    // the data draws the character: Yoda is green, R2-D2 white and blue
    mvc.perform(get("/api/people").param("search", "Yoda"))
        .andExpect(jsonPath("$.content[0].image").value(org.hamcrest.Matchers.containsString("skinColor=7fae5b")));
    mvc.perform(get("/api/people").param("search", "R2-D2"))
        .andExpect(jsonPath("$.content[0].image").value(org.hamcrest.Matchers.allOf(
            org.hamcrest.Matchers.containsString("skinColor=f2f2f2"), org.hamcrest.Matchers.containsString("clothingColor=5b84d6"))));
    mvc.perform(get("/api/planets/1")).andExpect(jsonPath("$.image").value(org.hamcrest.Matchers.startsWith("https://api.dicebear.com/9.x/rings/png")));
    mvc.perform(get("/api/films").param("size", "1")).andExpect(jsonPath("$.content[0].image").exists());
  }

  @Test
  @DisplayName("a page carries the total, so a client can render a pager without fetching everything")
  void pagedEnvelope() throws Exception {
    mvc.perform(get("/api/people").param("size", "3"))
        .andExpect(jsonPath("$.content.length()").value(3))
        .andExpect(jsonPath("$.totalElements").value(82))
        .andExpect(jsonPath("$.totalPages").value(28))
        .andExpect(jsonPath("$.page").value(0));
  }

  @Test
  @DisplayName("a single-valued reference travels with its display name, so a table needs no second call")
  void referenceCarriesItsName() throws Exception {
    mvc.perform(get("/api/people").param("search", "Luke"))
        .andExpect(jsonPath("$.content[0].name").value("Luke Skywalker"))
        .andExpect(jsonPath("$.content[0].homeworldName").value("Tatooine"))
        .andExpect(jsonPath("$.content[0].url").value("/api/people/1"));
  }

  @Test
  @DisplayName("search scans the collection's text fields")
  void search() throws Exception {
    mvc.perform(get("/api/people").param("search", "sky"))
        .andExpect(jsonPath("$.totalElements").value(3));
  }

  @Test
  @DisplayName("an EXACT filter is a pick, not a prefix: male must not also take female")
  void exactFilterDoesNotMatchBySubstring() throws Exception {
    mvc.perform(get("/api/people").param("gender", "male"))
        .andExpect(jsonPath("$.totalElements").value(60));
    mvc.perform(get("/api/people").param("gender", "female"))
        .andExpect(jsonPath("$.totalElements").value(17));
  }

  @Test
  @DisplayName("a repeated parameter is an OR — what a multi-select means")
  void repeatedParameterIsAnOr() throws Exception {
    mvc.perform(get("/api/people").param("gender", "male").param("gender", "female"))
        .andExpect(jsonPath("$.totalElements").value(77));
  }

  @Test
  @DisplayName("a comma-joined value is also an OR — how a browser sends one multi-select")
  void commaJoinedValuesAreAnOr() throws Exception {
    mvc.perform(get("/api/people").param("gender", "male,female"))
        .andExpect(jsonPath("$.totalElements").value(77));
    // …but a TEXT filter is not split: "hot, humid" is one honest value, not two loose ones
    mvc.perform(get("/api/planets").param("climate", "arid"))
        .andExpect(jsonPath("$.totalElements").value(9));
  }

  @Test
  @DisplayName("ranges are inclusive and compare as numbers, not as text")
  void ranges() throws Exception {
    mvc.perform(get("/api/people").param("height_from", "200"))
        .andExpect(jsonPath("$.totalElements").value(11));
    mvc.perform(get("/api/people").param("height_from", "150").param("height_to", "175"))
        .andExpect(jsonPath("$.totalElements").value(22));
    mvc.perform(get("/api/films").param("releaseDate_from", "1990-01-01"))
        .andExpect(jsonPath("$.totalElements").value(3));
  }

  @Test
  @DisplayName("a class filter matches by containment, across the data's own casing")
  void classFiltersAreText() throws Exception {
    // "starfighter" has to find "Starfighter" and "Assault Starfighter" alike
    mvc.perform(get("/api/starships").param("starshipClass", "starfighter"))
        .andExpect(jsonPath("$.totalElements").value(11));
    mvc.perform(get("/api/vehicles").param("vehicleClass", "walker"))
        .andExpect(jsonPath("$.totalElements").value(7));
  }

  @Test
  @DisplayName("a reference is filterable by id — the residents of a world")
  void filterByReference() throws Exception {
    mvc.perform(get("/api/people").param("homeworldId", "1"))
        .andExpect(jsonPath("$.totalElements").value(10));
  }

  @Test
  @DisplayName("an undeclared filter is a 400, never a silently unfiltered page")
  void unknownFilterIsRejected() throws Exception {
    mvc.perform(get("/api/people").param("planet", "Tatooine"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Unknown filter")));
  }

  @Test
  @DisplayName("an unparseable filter value is the caller's fault, not a server error")
  void unparseableFilterValue() throws Exception {
    mvc.perform(get("/api/people").param("height_from", "tall")).andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("writing without the key is refused")
  void writeNeedsTheKey() throws Exception {
    mvc.perform(post("/api/people").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"X\"}"))
        .andExpect(status().isUnauthorized());
    mvc.perform(
            post("/api/people")
                .header(KEY, "wrong")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"X\"}"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("reading never needs the key")
  void readsAreOpen() throws Exception {
    mvc.perform(get("/api/planets")).andExpect(status().isOk());
  }

  @Test
  @DisplayName("a full write cycle: create, read back, re-point a reference, delete")
  void writeCycle() throws Exception {
    String created =
        mvc.perform(
                post("/api/people")
                    .header(KEY, "testkey")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {"name":"Mara Jade","height":178,"mass":56,"gender":"female",
                         "homeworldId":1,"speciesIds":[1]}
                        """))
            .andExpect(status().isCreated())
            .andExpect(header().exists("Location"))
            .andExpect(jsonPath("$.homeworldName").value("Tatooine"))
            .andExpect(jsonPath("$.speciesIds.length()").value(1))
            .andReturn()
            .getResponse()
            .getContentAsString();

    String id = created.replaceAll("^\\{\"id\":(\\d+).*$", "$1");

    // it is a row like any other: readable, searchable, and counted
    mvc.perform(get("/api/people/" + id)).andExpect(jsonPath("$.name").value("Mara Jade"));
    mvc.perform(get("/api/people").param("search", "Mara"))
        .andExpect(jsonPath("$.totalElements").value(1));

    // a replace re-points the reference and clears the association
    mvc.perform(
            put("/api/people/" + id)
                .header(KEY, "testkey")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Mara Jade Skywalker\",\"homeworldId\":8,\"speciesIds\":[]}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.homeworldName").value("Naboo"))
        .andExpect(jsonPath("$.speciesIds.length()").value(0));

    mvc.perform(delete("/api/people/" + id).header(KEY, "testkey")).andExpect(status().isNoContent());
    mvc.perform(get("/api/people/" + id)).andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("pointing at a row that does not exist is a 400 — the body is wrong, not the address")
  void badReference() throws Exception {
    mvc.perform(
            post("/api/people")
                .header(KEY, "testkey")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Nobody\",\"homeworldId\":9999}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("No planet with id 9999"));
  }

  @Test
  @DisplayName("a blank name is rejected by the body validation, not by a flush 500")
  void validation() throws Exception {
    mvc.perform(
            post("/api/people")
                .header(KEY, "testkey")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"  \"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("must not be blank")));
  }

  @Test
  @DisplayName("a film owns its cast: one write re-points the whole web of references")
  void filmOwnsItsAssociations() throws Exception {
    String body =
        mvc.perform(get("/api/films/1")).andReturn().getResponse().getContentAsString();
    assertThat(body).contains("characterIds");

    mvc.perform(
            put("/api/films/1")
                .header(KEY, "testkey")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"title":"A New Hope","episodeId":4,"director":"George Lucas",
                     "releaseDate":"1977-05-25","characterIds":[1,2],"planetIds":[1]}
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.characterIds.length()").value(2))
        .andExpect(jsonPath("$.planetIds.length()").value(1));
  }

  @Test
  @DisplayName("a filter with more values than any multi-select sends is a 400")
  void tooManyFilterValues() throws Exception {
    String many = String.join(",", java.util.Collections.nCopies(51, "male"));
    mvc.perform(get("/api/people").param("gender", many))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("at most 50")));
  }

  @Test
  @DisplayName("the contract is published, and it declares the write key")
  void openApiIsPublished() throws Exception {
    mvc.perform(get("/v3/api-docs"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.info.title").value("SWAPI service"))
        .andExpect(jsonPath("$.components.securitySchemes.writeKey.name").value("X-Api-Key"))
        .andExpect(jsonPath("$.paths['/api/people']").exists());
  }
}
