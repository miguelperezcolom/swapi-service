package io.mateu.swapi.infra.in.rest.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record PlanetDto(
    Long id,
    @NotBlank(message = "must not be blank") String name,
    Integer rotationPeriod,
    Integer orbitalPeriod,
    Integer diameter,
    String climate,
    String gravity,
    String terrain,
    BigDecimal surfaceWater,
    Long population,
    List<Long> residentIds,
    List<Long> filmIds,
    String url,
    /** A generated picture of the record (DiceBear, CC0): read-only, ignored on writes. */
    String image) {}
