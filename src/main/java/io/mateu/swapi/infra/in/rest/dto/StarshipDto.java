package io.mateu.swapi.infra.in.rest.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record StarshipDto(
    Long id,
    @NotBlank(message = "must not be blank") String name,
    String model,
    String manufacturer,
    Long costInCredits,
    BigDecimal length,
    Integer maxAtmospheringSpeed,
    String crew,
    Integer passengers,
    Long cargoCapacity,
    String consumables,
    BigDecimal hyperdriveRating,
    Integer mglt,
    String starshipClass,
    List<Long> pilotIds,
    List<Long> filmIds,
    String url) {}
