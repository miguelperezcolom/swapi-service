package io.mateu.swapi.infra.in.rest;

import io.mateu.swapi.domain.Person;
import io.mateu.swapi.infra.in.rest.dto.PersonDto;
import io.mateu.swapi.infra.in.rest.query.FilterKind;
import io.mateu.swapi.infra.in.rest.query.QuerySpec;
import io.mateu.swapi.infra.out.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/people")
@Tag(name = "People", description = "Characters of the saga")
public class PeopleController extends CrudController<Person, PersonDto> {

  private final PersonRepository people;
  private final PlanetRepository planets;
  private final SpeciesRepository species;

  public PeopleController(
      PersonRepository people, PlanetRepository planets, SpeciesRepository species) {
    this.people = people;
    this.planets = planets;
    this.species = species;
  }

  @Override
  protected String collection() {
    return "people";
  }

  @Override
  protected SwapiRepository<Person> repository() {
    return people;
  }

  @Override
  protected QuerySpec querySpec() {
    return QuerySpec.builder()
        .filter("name", FilterKind.TEXT)
        // gender and the colours are closed sets: "male" must not also match "female"
        .filter("gender", FilterKind.EXACT)
        .filter("eyeColor", FilterKind.EXACT)
        .filter("hairColor", FilterKind.EXACT)
        .filter("skinColor", FilterKind.EXACT)
        .filter("birthYear", FilterKind.TEXT)
        .filter("height", FilterKind.NUMBER)
        .filter("mass", FilterKind.NUMBER)
        .filter("homeworldId", "homeworld.id", FilterKind.REF)
        .searchable("name", "birthYear")
        .defaultSort("name")
        .build();
  }

  @Override
  protected Person newEntity() {
    return new Person();
  }

  @Override
  protected Long idOf(PersonDto dto) {
    return dto.id();
  }

  @Override
  protected PersonDto toDto(Person p) {
    return new PersonDto(
        p.getId(),
        p.getName(),
        p.getHeight(),
        p.getMass(),
        p.getHairColor(),
        p.getSkinColor(),
        p.getEyeColor(),
        p.getBirthYear(),
        p.getGender(),
        p.getHomeworld() == null ? null : p.getHomeworld().getId(),
        p.getHomeworld() == null ? null : p.getHomeworld().getName(),
        p.getSpecies().stream().map(s -> s.getId()).toList(),
        p.getFilms().stream().map(f -> f.getId()).toList(),
        p.getVehicles().stream().map(v -> v.getId()).toList(),
        p.getStarships().stream().map(s -> s.getId()).toList(),
        urlOf(p.getId()),
        Avatars.person(p.getName(), p.getHairColor(), p.getSkinColor(), p.getEyeColor(), p.getGender()));
  }

  @Override
  protected void apply(PersonDto dto, Person p) {
    p.setName(dto.name());
    p.setHeight(dto.height());
    p.setMass(dto.mass());
    p.setHairColor(dto.hairColor());
    p.setSkinColor(dto.skinColor());
    p.setEyeColor(dto.eyeColor());
    p.setBirthYear(dto.birthYear());
    p.setGender(dto.gender());
    p.setHomeworld(resolveOne(dto.homeworldId(), planets, "planet"));
    // Only the associations this side OWNS are writable here; a person's films are edited on the
    // film, which is where the join table lives.
    Set<io.mateu.swapi.domain.Species> resolved =
        new LinkedHashSet<>(resolve(dto.speciesIds(), species, "species"));
    p.getSpecies().clear();
    p.getSpecies().addAll(resolved);
  }
}
