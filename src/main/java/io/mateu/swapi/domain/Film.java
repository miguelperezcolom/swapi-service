package io.mateu.swapi.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Getter;
import org.hibernate.annotations.BatchSize;
import lombok.Setter;

/**
 * A film. It OWNS every association it takes part in (characters, planets, starships, vehicles,
 * species): the cast of a film is edited as a property of the film, so putting the join tables here
 * is what makes a single PUT able to re-point them.
 */
@Entity
@Table(name = "film")
@Getter
@Setter
public class Film {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank private String title;

  private Integer episodeId;

  @Column(length = 2000)
  private String openingCrawl;

  private String director;
  private String producer;
  private LocalDate releaseDate;

  @ManyToMany
  @JoinTable(
      name = "film_characters",
      joinColumns = @JoinColumn(name = "film_id"),
      inverseJoinColumns = @JoinColumn(name = "person_id"))
  @BatchSize(size = 100)
  private Set<Person> characters = new LinkedHashSet<>();

  @ManyToMany
  @JoinTable(
      name = "film_planets",
      joinColumns = @JoinColumn(name = "film_id"),
      inverseJoinColumns = @JoinColumn(name = "planet_id"))
  @BatchSize(size = 100)
  private Set<Planet> planets = new LinkedHashSet<>();

  @ManyToMany
  @JoinTable(
      name = "film_starships",
      joinColumns = @JoinColumn(name = "film_id"),
      inverseJoinColumns = @JoinColumn(name = "starship_id"))
  @BatchSize(size = 100)
  private Set<Starship> starships = new LinkedHashSet<>();

  @ManyToMany
  @JoinTable(
      name = "film_vehicles",
      joinColumns = @JoinColumn(name = "film_id"),
      inverseJoinColumns = @JoinColumn(name = "vehicle_id"))
  @BatchSize(size = 100)
  private Set<Vehicle> vehicles = new LinkedHashSet<>();

  @ManyToMany
  @JoinTable(
      name = "film_species",
      joinColumns = @JoinColumn(name = "film_id"),
      inverseJoinColumns = @JoinColumn(name = "species_id"))
  @BatchSize(size = 100)
  private Set<Species> species = new LinkedHashSet<>();
}
