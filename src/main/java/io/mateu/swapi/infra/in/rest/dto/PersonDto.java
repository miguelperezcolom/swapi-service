package io.mateu.swapi.infra.in.rest.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record PersonDto(
    Long id,
    @NotBlank(message = "must not be blank") String name,
    Integer height,
    BigDecimal mass,
    String hairColor,
    String skinColor,
    String eyeColor,
    String birthYear,
    String gender,
    Long homeworldId,
    String homeworldName,
    List<Long> speciesIds,
    List<Long> filmIds,
    List<Long> vehicleIds,
    List<Long> starshipIds,
    String url) {}
