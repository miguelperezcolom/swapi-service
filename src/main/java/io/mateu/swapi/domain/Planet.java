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
 * A planet of the Star Wars galaxy.
 *
 * <p>The numeric fields are TYPED here, where the API we clone serves them as strings carrying
 * "unknown" for a missing value. That is a deliberate departure: a real column is what lets the
 * database answer a range query and a sort, and "unknown" is simply {@code null}. The seeder drops
 * anything that does not parse rather than inventing a zero.
 */
@Entity
@Table(name = "planet")
@BatchSize(size = 100)
@Getter
@Setter
public class Planet {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank private String name;

  private Integer rotationPeriod;
  private Integer orbitalPeriod;
  private Integer diameter;
  private String climate;
  private String gravity;
  private String terrain;
  private BigDecimal surfaceWater;
  private Long population;

  /** Inverse of {@link Person#getHomeworld()} — the planet does not own the association. */
  @OneToMany(mappedBy = "homeworld")
  @BatchSize(size = 100)
  private Set<Person> residents = new LinkedHashSet<>();

  @ManyToMany(mappedBy = "planets")
  @BatchSize(size = 100)
  private Set<Film> films = new LinkedHashSet<>();
}
