package io.mateu.swapi.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** The published contract: /v3/api-docs, rendered at /swagger-ui.html. */
@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI swapiOpenApi(@Value("${swapi.write-key-header}") String writeKeyHeader) {
    return new OpenAPI()
        .info(
            new Info()
                .title("SWAPI service")
                .version("0.1.0")
                .description(
                    """
                    A writable clone of the Star Wars API: the full data model over an in-memory H2, \
                    seeded at boot from the original dataset.

                    **Reads are open. Writes need the `%s` header.**

                    Every collection answers `GET /api/<collection>` with a paged envelope \
                    (`content` + `totalElements`) and supports `search`, its own declared filters, \
                    `<field>_from`/`<field>_to` ranges, `sort` and `page`/`size`. A filter the \
                    collection does not declare is a 400, never a silently unfiltered page.

                    References travel as ids, plus the display name for the single-valued ones \
                    (`homeworldId` + `homeworldName`), so a table renders without a second request. \
                    A to-many association is written on the side that owns it: a film's cast on the \
                    film, a person's species on the person.

                    The database is in memory and re-seeded on every restart, so anything written \
                    here is demo data with a short life.
                    """
                        .formatted(writeKeyHeader))
                .license(new License().name("MIT")))
        .components(
            new Components()
                .addSecuritySchemes(
                    "writeKey",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.HEADER)
                        .name(writeKeyHeader)
                        .description("Required on POST, PUT, PATCH and DELETE.")));
  }
}
