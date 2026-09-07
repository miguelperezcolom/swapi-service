package io.mateu.swapi.infra.in.rest;

import io.mateu.swapi.domain.Person;
import io.mateu.swapi.domain.Vehicle;
import io.mateu.swapi.infra.in.rest.dto.VehicleDto;
import io.mateu.swapi.infra.in.rest.query.FilterKind;
import io.mateu.swapi.infra.in.rest.query.QuerySpec;
import io.mateu.swapi.infra.out.PersonRepository;
import io.mateu.swapi.infra.out.SwapiRepository;
import io.mateu.swapi.infra.out.VehicleRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/vehicles")
@Tag(name = "Vehicles", description = "Craft without hyperdrive")
public class VehiclesController extends CrudController<Vehicle, VehicleDto> {

  private final VehicleRepository vehicles;
  private final PersonRepository people;

  public VehiclesController(VehicleRepository vehicles, PersonRepository people) {
    this.vehicles = vehicles;
    this.people = people;
  }

  @Override
  protected String collection() {
    return "vehicles";
  }

  @Override
  protected SwapiRepository<Vehicle> repository() {
    return vehicles;
  }

  @Override
  protected QuerySpec querySpec() {
    return QuerySpec.builder()
        .filter("name", FilterKind.TEXT)
        .filter("model", FilterKind.TEXT)
        .filter("manufacturer", FilterKind.TEXT)
        // A descriptive multi-word label, not a closed set: the data holds "wheeled walker",
        // "Assault Starfighter" and both casings of "starfighter". Exact equality would make a
        // caller reproduce the whole string; containment is what someone filtering by class means.
        .filter("vehicleClass", FilterKind.TEXT)
        .filter("costInCredits", FilterKind.NUMBER)
        .filter("length", FilterKind.NUMBER)
        .filter("maxAtmospheringSpeed", FilterKind.NUMBER)
        .filter("passengers", FilterKind.NUMBER)
        .filter("cargoCapacity", FilterKind.NUMBER)
        .searchable("name", "model", "manufacturer", "vehicleClass")
        .defaultSort("name")
        .build();
  }

  @Override
  protected Vehicle newEntity() {
    return new Vehicle();
  }

  @Override
  protected Long idOf(VehicleDto dto) {
    return dto.id();
  }

  @Override
  protected VehicleDto toDto(Vehicle v) {
    return new VehicleDto(
        v.getId(),
        v.getName(),
        v.getModel(),
        v.getManufacturer(),
        v.getCostInCredits(),
        v.getLength(),
        v.getMaxAtmospheringSpeed(),
        v.getCrew(),
        v.getPassengers(),
        v.getCargoCapacity(),
        v.getConsumables(),
        v.getVehicleClass(),
        v.getPilots().stream().map(p -> p.getId()).toList(),
        v.getFilms().stream().map(f -> f.getId()).toList(),
        urlOf(v.getId()));
  }

  @Override
  protected void apply(VehicleDto dto, Vehicle v) {
    v.setName(dto.name());
    v.setModel(dto.model());
    v.setManufacturer(dto.manufacturer());
    v.setCostInCredits(dto.costInCredits());
    v.setLength(dto.length());
    v.setMaxAtmospheringSpeed(dto.maxAtmospheringSpeed());
    v.setCrew(dto.crew());
    v.setPassengers(dto.passengers());
    v.setCargoCapacity(dto.cargoCapacity());
    v.setConsumables(dto.consumables());
    v.setVehicleClass(dto.vehicleClass());
    Set<Person> pilots = new LinkedHashSet<>(resolve(dto.pilotIds(), people, "person"));
    v.getPilots().clear();
    v.getPilots().addAll(pilots);
  }
}
