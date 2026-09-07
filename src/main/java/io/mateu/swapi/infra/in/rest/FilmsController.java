package io.mateu.swapi.infra.in.rest;

import io.mateu.swapi.domain.*;
import io.mateu.swapi.infra.in.rest.dto.FilmDto;
import io.mateu.swapi.infra.in.rest.query.FilterKind;
import io.mateu.swapi.infra.in.rest.query.QuerySpec;
import io.mateu.swapi.infra.out.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.LinkedHashSet;
import java.util.Collection;
import java.util.Set;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Films own every association they take part in, so this is the one place where a single write
 * re-points a whole web of references at once — the cast, the worlds, the craft and the species.
 */
@RestController
@RequestMapping("/api/films")
@Tag(name = "Films", description = "The films of the saga")
public class FilmsController extends CrudController<Film, FilmDto> {

  private final FilmRepository films;
  private final PersonRepository people;
  private final PlanetRepository planets;
  private final StarshipRepository starships;
  private final VehicleRepository vehicles;
  private final SpeciesRepository species;

  public FilmsController(
      FilmRepository films,
      PersonRepository people,
      PlanetRepository planets,
      StarshipRepository starships,
      VehicleRepository vehicles,
      SpeciesRepository species) {
    this.films = films;
    this.people = people;
    this.planets = planets;
    this.starships = starships;
    this.vehicles = vehicles;
    this.species = species;
  }

  @Override
  protected String collection() {
    return "films";
  }

  @Override
  protected SwapiRepository<Film> repository() {
    return films;
  }

  @Override
  protected QuerySpec querySpec() {
    return QuerySpec.builder()
        .filter("title", FilterKind.TEXT)
        .filter("director", FilterKind.EXACT)
        .filter("producer", FilterKind.TEXT)
        .filter("episodeId", FilterKind.NUMBER)
        .filter("releaseDate", FilterKind.DATE)
        .searchable("title", "director", "producer", "openingCrawl")
        .defaultSort("episodeId")
        .build();
  }

  @Override
  protected Film newEntity() {
    return new Film();
  }

  @Override
  protected Long idOf(FilmDto dto) {
    return dto.id();
  }

  @Override
  protected FilmDto toDto(Film f) {
    return new FilmDto(
        f.getId(),
        f.getTitle(),
        f.getEpisodeId(),
        f.getOpeningCrawl(),
        f.getDirector(),
        f.getProducer(),
        f.getReleaseDate(),
        ids(f.getCharacters()),
        ids(f.getPlanets()),
        ids(f.getStarships()),
        ids(f.getVehicles()),
        ids(f.getSpecies()),
        urlOf(f.getId()));
  }

  @Override
  protected void apply(FilmDto dto, Film f) {
    f.setTitle(dto.title());
    f.setEpisodeId(dto.episodeId());
    f.setOpeningCrawl(dto.openingCrawl());
    f.setDirector(dto.director());
    f.setProducer(dto.producer());
    f.setReleaseDate(dto.releaseDate());
    replace(f.getCharacters(), resolve(dto.characterIds(), people, "person"));
    replace(f.getPlanets(), resolve(dto.planetIds(), planets, "planet"));
    replace(f.getStarships(), resolve(dto.starshipIds(), starships, "starship"));
    replace(f.getVehicles(), resolve(dto.vehicleIds(), vehicles, "vehicle"));
    replace(f.getSpecies(), resolve(dto.speciesIds(), species, "species"));
  }

  /** Replaces the contents in place: Hibernate tracks the collection, so it must not be swapped. */
  private <T> void replace(Set<T> target, Collection<T> resolved) {
    target.clear();
    target.addAll(new LinkedHashSet<>(resolved));
  }

  private static java.util.List<Long> ids(Set<?> rows) {
    return rows.stream().map(FilmsController::idOfRow).toList();
  }

  private static Long idOfRow(Object row) {
    return switch (row) {
      case Person p -> p.getId();
      case Planet p -> p.getId();
      case Starship s -> s.getId();
      case Vehicle v -> v.getId();
      case Species s -> s.getId();
      default -> throw new IllegalStateException("Unexpected row: " + row.getClass());
    };
  }
}
