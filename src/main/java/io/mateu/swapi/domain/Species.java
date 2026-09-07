package io.mateu.swapi.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Getter;
import org.hibernate.annotations.BatchSize;
import lombok.Setter;

/** A species. Its homeworld is a reference; its members are the inverse of {@code Person.species}. */
@Entity
@Table(name = "species")
@Getter
@Setter
public class Species {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank private String name;

  private String classification;
  private String designation;
  private Integer averageHeight;
  private String skinColors;
  private String hairColors;
  private String eyeColors;
  private Integer averageLifespan;
  private String language;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "homeworld_id")
  private Planet homeworld;

  @ManyToMany(mappedBy = "species")
  @BatchSize(size = 100)
  private Set<Person> people = new LinkedHashSet<>();

  @ManyToMany(mappedBy = "species")
  @BatchSize(size = 100)
  private Set<Film> films = new LinkedHashSet<>();
}
