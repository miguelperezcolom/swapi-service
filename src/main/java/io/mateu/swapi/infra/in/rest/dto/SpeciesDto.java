package io.mateu.swapi.infra.in.rest.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record SpeciesDto(
    Long id,
    @NotBlank(message = "must not be blank") String name,
    String classification,
    String designation,
    Integer averageHeight,
    String skinColors,
    String hairColors,
    String eyeColors,
    Integer averageLifespan,
    String language,
    Long homeworldId,
    String homeworldName,
    List<Long> peopleIds,
    List<Long> filmIds,
    String url) {}
