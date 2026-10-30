package io.mateu.swapi.infra.in.rest;

import io.mateu.swapi.domain.Planet;
import io.mateu.swapi.infra.in.rest.dto.PlanetDto;
import io.mateu.swapi.infra.in.rest.query.FilterKind;
import io.mateu.swapi.infra.in.rest.query.QuerySpec;
import io.mateu.swapi.infra.out.PlanetRepository;
import io.mateu.swapi.infra.out.SwapiRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/planets")
@Tag(name = "Planets", description = "Worlds of the galaxy")
public class PlanetsController extends CrudController<Planet, PlanetDto> {

  private final PlanetRepository planets;

  public PlanetsController(PlanetRepository planets) {
    this.planets = planets;
  }

  @Override
  protected String collection() {
    return "planets";
  }

  @Override
  protected SwapiRepository<Planet> repository() {
    return planets;
  }

  @Override
  protected QuerySpec querySpec() {
    return QuerySpec.builder()
        .filter("name", FilterKind.TEXT)
        // climate and terrain are comma-joined lists in the source data ("arid", "hot, humid"), so
        // containment is the honest comparison: EXACT would never match a multi-climate world.
        .filter("climate", FilterKind.TEXT)
        .filter("terrain", FilterKind.TEXT)
        .filter("gravity", FilterKind.TEXT)
        .filter("diameter", FilterKind.NUMBER)
        .filter("population", FilterKind.NUMBER)
        .filter("rotationPeriod", FilterKind.NUMBER)
        .filter("orbitalPeriod", FilterKind.NUMBER)
        .filter("surfaceWater", FilterKind.NUMBER)
        .searchable("name", "climate", "terrain")
        .defaultSort("name")
        .build();
  }

  @Override
  protected Planet newEntity() {
    return new Planet();
  }

  @Override
  protected Long idOf(PlanetDto dto) {
    return dto.id();
  }

  @Override
  protected PlanetDto toDto(Planet p) {
    return new PlanetDto(
        p.getId(),
        p.getName(),
        p.getRotationPeriod(),
        p.getOrbitalPeriod(),
        p.getDiameter(),
        p.getClimate(),
        p.getGravity(),
        p.getTerrain(),
        p.getSurfaceWater(),
        p.getPopulation(),
        p.getResidents().stream().map(r -> r.getId()).toList(),
        p.getFilms().stream().map(f -> f.getId()).toList(),
        urlOf(p.getId()),
        Avatars.planet(p.getName(), p.getTerrain()));
  }

  @Override
  protected void apply(PlanetDto dto, Planet p) {
    p.setName(dto.name());
    p.setRotationPeriod(dto.rotationPeriod());
    p.setOrbitalPeriod(dto.orbitalPeriod());
    p.setDiameter(dto.diameter());
    p.setClimate(dto.climate());
    p.setGravity(dto.gravity());
    p.setTerrain(dto.terrain());
    p.setSurfaceWater(dto.surfaceWater());
    p.setPopulation(dto.population());
    // residents and films are owned by the other side (Person.homeworld, Film.planets).
  }
}
