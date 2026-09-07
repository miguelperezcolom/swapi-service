package io.mateu.swapi.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Getter;
import org.hibernate.annotations.BatchSize;
import lombok.Setter;

/**
 * A vehicle — a craft without hyperdrive. {@code crew} stays a String on purpose: the source data
 * carries ranges like "30-165", which no numeric column can hold without losing what it says.
 */
@Entity
@Table(name = "vehicle")
@Getter
@Setter
public class Vehicle {

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
  private String vehicleClass;

  @ManyToMany
  @JoinTable(
      name = "vehicle_pilots",
      joinColumns = @JoinColumn(name = "vehicle_id"),
      inverseJoinColumns = @JoinColumn(name = "person_id"))
  @BatchSize(size = 100)
  private Set<Person> pilots = new LinkedHashSet<>();

  @ManyToMany(mappedBy = "vehicles")
  @BatchSize(size = 100)
  private Set<Film> films = new LinkedHashSet<>();
}
