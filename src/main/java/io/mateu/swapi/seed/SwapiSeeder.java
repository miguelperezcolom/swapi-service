package io.mateu.swapi.seed;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.mateu.swapi.domain.*;
import io.mateu.swapi.infra.out.*;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.ApplicationArguments;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

/**
 * Loads the bundled Star Wars dataset into the database at boot.
 *
 * <p>The database is H2 in memory, so this runs on every start and every restart is a clean slate —
 * which is the point: a public, writable demo that anybody can scribble on had better be able to
 * forget.
 *
 * <p>Two passes, and the reason is the shape of the source data: every reference in it is a URL
 * (<code>"homeworld": "https://swapi.info/api/planets/1"</code>), and a URL cannot be resolved into
 * a row that has not been inserted yet — the graph has cycles (a person names their films, a film
 * names its characters). So pass one inserts every row and remembers which URL became which row;
 * pass two walks the references and links them up.
 */
@Component
public class SwapiSeeder implements ApplicationRunner {

  private static final Logger log = LoggerFactory.getLogger(SwapiSeeder.class);

  private final ObjectMapper json = new ObjectMapper();

  private final PersonRepository people;
  private final PlanetRepository planets;
  private final FilmRepository films;
  private final SpeciesRepository species;
  private final VehicleRepository vehicles;
  private final StarshipRepository starships;

  /** Source URL → the row it became. The whole reason the second pass can work. */
  private final Map<String, Person> personByUrl = new HashMap<>();
  private final Map<String, Planet> planetByUrl = new HashMap<>();
  private final Map<String, Film> filmByUrl = new HashMap<>();
  private final Map<String, Species> speciesByUrl = new HashMap<>();
  private final Map<String, Vehicle> vehicleByUrl = new HashMap<>();
  private final Map<String, Starship> starshipByUrl = new HashMap<>();

  public SwapiSeeder(
      PersonRepository people,
      PlanetRepository planets,
      FilmRepository films,
      SpeciesRepository species,
      VehicleRepository vehicles,
      StarshipRepository starships) {
    this.people = people;
    this.planets = planets;
    this.films = films;
    this.species = species;
    this.vehicles = vehicles;
    this.starships = starships;
  }

  @Override
  @Transactional
  public void run(ApplicationArguments args) throws IOException {
    if (planets.count() > 0) {
      log.info("Database already seeded; skipping");
      return;
    }
    long started = System.currentTimeMillis();

    // ── pass 1: the rows ──────────────────────────────────────────────────────
    forEach("planets", node -> planetByUrl.put(url(node), planets.save(planet(node))));
    forEach("species", node -> speciesByUrl.put(url(node), species.save(species(node))));
    forEach("people", node -> personByUrl.put(url(node), people.save(person(node))));
    forEach("vehicles", node -> vehicleByUrl.put(url(node), vehicles.save(vehicle(node))));
    forEach("starships", node -> starshipByUrl.put(url(node), starships.save(starship(node))));
    forEach("films", node -> filmByUrl.put(url(node), films.save(film(node))));

    // ── pass 2: the references ────────────────────────────────────────────────
    forEach("people", this::linkPerson);
    forEach("species", this::linkSpecies);
    forEach("vehicles", this::linkVehicle);
    forEach("starships", this::linkStarship);
    forEach("films", this::linkFilm);

    log.info(
        "Seeded {} planets, {} species, {} people, {} vehicles, {} starships, {} films in {} ms",
        planets.count(), species.count(), people.count(),
        vehicles.count(), starships.count(), films.count(),
        System.currentTimeMillis() - started);
  }

  // ── pass 1 builders ─────────────────────────────────────────────────────────

  private Planet planet(JsonNode n) {
    Planet p = new Planet();
    p.setName(text(n, "name"));
    p.setRotationPeriod(integer(n, "rotation_period"));
    p.setOrbitalPeriod(integer(n, "orbital_period"));
    p.setDiameter(integer(n, "diameter"));
    p.setClimate(text(n, "climate"));
    p.setGravity(text(n, "gravity"));
    p.setTerrain(text(n, "terrain"));
    p.setSurfaceWater(decimal(n, "surface_water"));
    p.setPopulation(number(n, "population"));
    return p;
  }

  private Species species(JsonNode n) {
    Species s = new Species();
    s.setName(text(n, "name"));
    s.setClassification(text(n, "classification"));
    s.setDesignation(text(n, "designation"));
    s.setAverageHeight(integer(n, "average_height"));
    s.setSkinColors(text(n, "skin_colors"));
    s.setHairColors(text(n, "hair_colors"));
    s.setEyeColors(text(n, "eye_colors"));
    s.setAverageLifespan(integer(n, "average_lifespan"));
    s.setLanguage(text(n, "language"));
    return s;
  }

  private Person person(JsonNode n) {
    Person p = new Person();
    p.setName(text(n, "name"));
    p.setHeight(integer(n, "height"));
    p.setMass(decimal(n, "mass"));
    p.setHairColor(text(n, "hair_color"));
    p.setSkinColor(text(n, "skin_color"));
    p.setEyeColor(text(n, "eye_color"));
    p.setBirthYear(text(n, "birth_year"));
    p.setGender(text(n, "gender"));
    return p;
  }

  private Vehicle vehicle(JsonNode n) {
    Vehicle v = new Vehicle();
    v.setName(text(n, "name"));
    v.setModel(text(n, "model"));
    v.setManufacturer(text(n, "manufacturer"));
    v.setCostInCredits(number(n, "cost_in_credits"));
    v.setLength(decimal(n, "length"));
    v.setMaxAtmospheringSpeed(integer(n, "max_atmosphering_speed"));
    v.setCrew(text(n, "crew"));
    v.setPassengers(integer(n, "passengers"));
    v.setCargoCapacity(number(n, "cargo_capacity"));
    v.setConsumables(text(n, "consumables"));
    v.setVehicleClass(text(n, "vehicle_class"));
    return v;
  }

  private Starship starship(JsonNode n) {
    Starship s = new Starship();
    s.setName(text(n, "name"));
    s.setModel(text(n, "model"));
    s.setManufacturer(text(n, "manufacturer"));
    s.setCostInCredits(number(n, "cost_in_credits"));
    s.setLength(decimal(n, "length"));
    s.setMaxAtmospheringSpeed(integer(n, "max_atmosphering_speed"));
    s.setCrew(text(n, "crew"));
    s.setPassengers(integer(n, "passengers"));
    s.setCargoCapacity(number(n, "cargo_capacity"));
    s.setConsumables(text(n, "consumables"));
    s.setHyperdriveRating(decimal(n, "hyperdrive_rating"));
    s.setMglt(integer(n, "MGLT"));
    s.setStarshipClass(text(n, "starship_class"));
    return s;
  }

  private Film film(JsonNode n) {
    Film f = new Film();
    f.setTitle(text(n, "title"));
    f.setEpisodeId(integer(n, "episode_id"));
    f.setOpeningCrawl(text(n, "opening_crawl"));
    f.setDirector(text(n, "director"));
    f.setProducer(text(n, "producer"));
    String released = text(n, "release_date");
    if (released != null) f.setReleaseDate(LocalDate.parse(released));
    return f;
  }

  // ── pass 2: the references ──────────────────────────────────────────────────

  private void linkPerson(JsonNode n) {
    Person p = personByUrl.get(url(n));
    p.setHomeworld(planetByUrl.get(text(n, "homeworld")));
    each(n, "species", ref -> p.getSpecies().add(speciesByUrl.get(ref)));
    people.save(p);
  }

  private void linkSpecies(JsonNode n) {
    Species s = speciesByUrl.get(url(n));
    s.setHomeworld(planetByUrl.get(text(n, "homeworld")));
    species.save(s);
  }

  private void linkVehicle(JsonNode n) {
    Vehicle v = vehicleByUrl.get(url(n));
    each(n, "pilots", ref -> v.getPilots().add(personByUrl.get(ref)));
    vehicles.save(v);
  }

  private void linkStarship(JsonNode n) {
    Starship s = starshipByUrl.get(url(n));
    each(n, "pilots", ref -> s.getPilots().add(personByUrl.get(ref)));
    starships.save(s);
  }

  private void linkFilm(JsonNode n) {
    Film f = filmByUrl.get(url(n));
    each(n, "characters", ref -> f.getCharacters().add(personByUrl.get(ref)));
    each(n, "planets", ref -> f.getPlanets().add(planetByUrl.get(ref)));
    each(n, "starships", ref -> f.getStarships().add(starshipByUrl.get(ref)));
    each(n, "vehicles", ref -> f.getVehicles().add(vehicleByUrl.get(ref)));
    each(n, "species", ref -> f.getSpecies().add(speciesByUrl.get(ref)));
    films.save(f);
  }

  // ── reading ─────────────────────────────────────────────────────────────────

  private void forEach(String resource, Consumer<JsonNode> visitor) throws IOException {
    try (InputStream in = new ClassPathResource("seed/" + resource + ".json").getInputStream()) {
      json.readTree(in).forEach(visitor);
    }
  }

  private static String url(JsonNode node) {
    return node.path("url").asText(null);
  }

  /** Runs the body for each non-null reference in an array field, skipping any that did not resolve. */
  private static void each(JsonNode node, String field, Consumer<String> body) {
    JsonNode array = node.path(field);
    if (!array.isArray()) return;
    array.forEach(item -> {
      String ref = item.asText(null);
      if (ref != null && !ref.isBlank()) body.accept(ref);
    });
  }

  private static String text(JsonNode node, String field) {
    String value = node.path(field).asText(null);
    return value == null || value.isBlank() ? null : value;
  }

  /**
   * The source serves every number as a string, and "unknown", "n/a", "none" and "1,000" all appear
   * in the numeric columns. A value that is not a number becomes NULL — which is what it means —
   * rather than a zero that would quietly join every range query.
   */
  private static String numeric(JsonNode node, String field) {
    String raw = text(node, field);
    if (raw == null) return null;
    String cleaned = raw.replace(",", "").trim();
    return cleaned.matches("-?\\d+(\\.\\d+)?") ? cleaned : null;
  }

  private static Integer integer(JsonNode node, String field) {
    String value = numeric(node, field);
    return value == null ? null : (int) Double.parseDouble(value);
  }

  private static Long number(JsonNode node, String field) {
    String value = numeric(node, field);
    return value == null ? null : (long) Double.parseDouble(value);
  }

  private static BigDecimal decimal(JsonNode node, String field) {
    String value = numeric(node, field);
    return value == null ? null : new BigDecimal(value);
  }
}
