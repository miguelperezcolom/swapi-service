package io.mateu.swapi.infra.in.rest.dto;

import java.time.LocalDate;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record FilmDto(
    Long id,
    @NotBlank(message = "must not be blank") String title,
    Integer episodeId,
    String openingCrawl,
    String director,
    String producer,
    LocalDate releaseDate,
    List<Long> characterIds,
    List<Long> planetIds,
    List<Long> starshipIds,
    List<Long> vehicleIds,
    List<Long> speciesIds,
    String url) {}
