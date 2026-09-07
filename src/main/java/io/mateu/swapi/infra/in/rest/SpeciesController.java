package io.mateu.swapi.infra.in.rest;

import io.mateu.swapi.domain.Species;
import io.mateu.swapi.infra.in.rest.dto.SpeciesDto;
import io.mateu.swapi.infra.in.rest.query.FilterKind;
import io.mateu.swapi.infra.in.rest.query.QuerySpec;
import io.mateu.swapi.infra.out.PlanetRepository;
import io.mateu.swapi.infra.out.SpeciesRepository;
import io.mateu.swapi.infra.out.SwapiRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/species")
@Tag(name = "Species", description = "Sentient and non-sentient species")
public class SpeciesController extends CrudController<Species, SpeciesDto> {

  private final SpeciesRepository species;
  private final PlanetRepository planets;

  public SpeciesController(SpeciesRepository species, PlanetRepository planets) {
    this.species = species;
    this.planets = planets;
  }

  @Override
  protected String collection() {
    return "species";
  }

  @Override
  protected SwapiRepository<Species> repository() {
    return species;
  }

  @Override
  protected QuerySpec querySpec() {
    return QuerySpec.builder()
        .filter("name", FilterKind.TEXT)
        .filter("classification", FilterKind.EXACT)
        .filter("designation", FilterKind.EXACT)
        .filter("language", FilterKind.TEXT)
        .filter("averageHeight", FilterKind.NUMBER)
        .filter("averageLifespan", FilterKind.NUMBER)
        .filter("homeworldId", "homeworld.id", FilterKind.REF)
        .searchable("name", "classification", "language")
        .defaultSort("name")
        .build();
  }

  @Override
  protected Species newEntity() {
    return new Species();
  }

  @Override
  protected Long idOf(SpeciesDto dto) {
    return dto.id();
  }

  @Override
  protected SpeciesDto toDto(Species s) {
    return new SpeciesDto(
        s.getId(),
        s.getName(),
        s.getClassification(),
        s.getDesignation(),
        s.getAverageHeight(),
        s.getSkinColors(),
        s.getHairColors(),
        s.getEyeColors(),
        s.getAverageLifespan(),
        s.getLanguage(),
        s.getHomeworld() == null ? null : s.getHomeworld().getId(),
        s.getHomeworld() == null ? null : s.getHomeworld().getName(),
        s.getPeople().stream().map(p -> p.getId()).toList(),
        s.getFilms().stream().map(f -> f.getId()).toList(),
        urlOf(s.getId()));
  }

  @Override
  protected void apply(SpeciesDto dto, Species s) {
    s.setName(dto.name());
    s.setClassification(dto.classification());
    s.setDesignation(dto.designation());
    s.setAverageHeight(dto.averageHeight());
    s.setSkinColors(dto.skinColors());
    s.setHairColors(dto.hairColors());
    s.setEyeColors(dto.eyeColors());
    s.setAverageLifespan(dto.averageLifespan());
    s.setLanguage(dto.language());
    s.setHomeworld(resolveOne(dto.homeworldId(), planets, "planet"));
    // people and films are owned by Person.species and Film.species.
  }
}
