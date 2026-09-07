package io.mateu.swapi.infra.in.rest;

import io.mateu.swapi.domain.Person;
import io.mateu.swapi.domain.Starship;
import io.mateu.swapi.infra.in.rest.dto.StarshipDto;
import io.mateu.swapi.infra.in.rest.query.FilterKind;
import io.mateu.swapi.infra.in.rest.query.QuerySpec;
import io.mateu.swapi.infra.out.PersonRepository;
import io.mateu.swapi.infra.out.StarshipRepository;
import io.mateu.swapi.infra.out.SwapiRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/starships")
@Tag(name = "Starships", description = "Craft with hyperdrive")
public class StarshipsController extends CrudController<Starship, StarshipDto> {

  private final StarshipRepository starships;
  private final PersonRepository people;

  public StarshipsController(StarshipRepository starships, PersonRepository people) {
    this.starships = starships;
    this.people = people;
  }

  @Override
  protected String collection() {
    return "starships";
  }

  @Override
  protected SwapiRepository<Starship> repository() {
    return starships;
  }

  @Override
  protected QuerySpec querySpec() {
    return QuerySpec.builder()
        .filter("name", FilterKind.TEXT)
        .filter("model", FilterKind.TEXT)
        .filter("manufacturer", FilterKind.TEXT)
        .filter("starshipClass", FilterKind.EXACT)
        .filter("costInCredits", FilterKind.NUMBER)
        .filter("length", FilterKind.NUMBER)
        .filter("maxAtmospheringSpeed", FilterKind.NUMBER)
        .filter("passengers", FilterKind.NUMBER)
        .filter("cargoCapacity", FilterKind.NUMBER)
        .filter("hyperdriveRating", FilterKind.NUMBER)
        .filter("mglt", FilterKind.NUMBER)
        .searchable("name", "model", "manufacturer", "starshipClass")
        .defaultSort("name")
        .build();
  }

  @Override
  protected Starship newEntity() {
    return new Starship();
  }

  @Override
  protected Long idOf(StarshipDto dto) {
    return dto.id();
  }

  @Override
  protected StarshipDto toDto(Starship s) {
    return new StarshipDto(
        s.getId(),
        s.getName(),
        s.getModel(),
        s.getManufacturer(),
        s.getCostInCredits(),
        s.getLength(),
        s.getMaxAtmospheringSpeed(),
        s.getCrew(),
        s.getPassengers(),
        s.getCargoCapacity(),
        s.getConsumables(),
        s.getHyperdriveRating(),
        s.getMglt(),
        s.getStarshipClass(),
        s.getPilots().stream().map(p -> p.getId()).toList(),
        s.getFilms().stream().map(f -> f.getId()).toList(),
        urlOf(s.getId()));
  }

  @Override
  protected void apply(StarshipDto dto, Starship s) {
    s.setName(dto.name());
    s.setModel(dto.model());
    s.setManufacturer(dto.manufacturer());
    s.setCostInCredits(dto.costInCredits());
    s.setLength(dto.length());
    s.setMaxAtmospheringSpeed(dto.maxAtmospheringSpeed());
    s.setCrew(dto.crew());
    s.setPassengers(dto.passengers());
    s.setCargoCapacity(dto.cargoCapacity());
    s.setConsumables(dto.consumables());
    s.setHyperdriveRating(dto.hyperdriveRating());
    s.setMglt(dto.mglt());
    s.setStarshipClass(dto.starshipClass());
    Set<Person> pilots = new LinkedHashSet<>(resolve(dto.pilotIds(), people, "person"));
    s.getPilots().clear();
    s.getPilots().addAll(pilots);
  }
}
