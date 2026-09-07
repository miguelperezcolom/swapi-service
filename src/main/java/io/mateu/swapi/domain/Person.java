package io.mateu.swapi.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Getter;
import org.hibernate.annotations.BatchSize;
import lombok.Setter;

/** A character of the saga. Owns its homeworld reference and its species. */
@Entity
@Table(name = "person")
@Getter
@Setter
public class Person {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank private String name;

  private Integer height;
  private BigDecimal mass;
  private String hairColor;
  private String skinColor;
  private String eyeColor;
  private String birthYear;
  private String gender;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "homeworld_id")
  private Planet homeworld;

  @ManyToMany
  @JoinTable(
      name = "person_species",
      joinColumns = @JoinColumn(name = "person_id"),
      inverseJoinColumns = @JoinColumn(name = "species_id"))
  @BatchSize(size = 100)
  private Set<Species> species = new LinkedHashSet<>();

  @ManyToMany(mappedBy = "characters")
  @BatchSize(size = 100)
  private Set<Film> films = new LinkedHashSet<>();

  @ManyToMany(mappedBy = "pilots")
  @BatchSize(size = 100)
  private Set<Vehicle> vehicles = new LinkedHashSet<>();

  @ManyToMany(mappedBy = "pilots")
  @BatchSize(size = 100)
  private Set<Starship> starships = new LinkedHashSet<>();
}
