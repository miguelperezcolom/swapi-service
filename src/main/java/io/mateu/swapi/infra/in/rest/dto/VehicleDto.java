package io.mateu.swapi.infra.in.rest.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record VehicleDto(
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
    String vehicleClass,
    List<Long> pilotIds,
    List<Long> filmIds,
    String url,
    /** A generated picture of the record (DiceBear, CC0): read-only, ignored on writes. */
    String image) {}
