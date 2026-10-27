package io.mateu.swapi;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/**
 * The row ceiling, in its own context: it needs a ceiling low enough to hit with the seed alone, and
 * that ceiling would refuse the creates {@link SwapiApiTest} makes.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {"swapi.write-key=testkey", "swapi.max-rows-per-collection=60"})
class RowCeilingTest {

  @Autowired private MockMvc mvc;

  @Test
  @DisplayName("a collection at its row ceiling refuses creates, so writes cannot exhaust the heap")
  void rowCeiling() throws Exception {
    // planets are seeded with exactly 60, which is the ceiling this context runs with
    mvc.perform(
            post("/api/planets")
                .header("X-Api-Key", "testkey")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Kamino II\"}"))
        .andExpect(status().isInsufficientStorage())
        .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("is full")));
    mvc.perform(get("/api/planets")).andExpect(jsonPath("$.totalElements").value(60));

    // a collection still under it takes the write as usual
    mvc.perform(
            post("/api/films")
                .header("X-Api-Key", "testkey")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"The Clone Wars\"}"))
        .andExpect(status().isCreated());
  }
}
