package io.mateu.swapi.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Getter;
import org.hibernate.annotations.BatchSize;
import lombok.Setter;

/** A starship — a vehicle with hyperdrive, which is why it is its own table and not a flag. */
@Entity
@Table(name = "starship")
@Getter
@Setter
public class Starship {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank private String name;

  private String model;
  private String manufacturer;
  private Long costInCredits;
  private BigDecimal length;
  private Integer maxAtmospheringSpeed;
  private String crew;
  private Integer passengers;
  private Long cargoCapacity;
  private String consumables;
  private BigDecimal hyperdriveRating;
  private Integer mglt;
  private String starshipClass;

  @ManyToMany
  @JoinTable(
      name = "starship_pilots",
      joinColumns = @JoinColumn(name = "starship_id"),
      inverseJoinColumns = @JoinColumn(name = "person_id"))
  @BatchSize(size = 100)
  private Set<Person> pilots = new LinkedHashSet<>();

  @ManyToMany(mappedBy = "starships")
  @BatchSize(size = 100)
  private Set<Film> films = new LinkedHashSet<>();
}
