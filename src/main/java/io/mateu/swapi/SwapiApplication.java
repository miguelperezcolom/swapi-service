package io.mateu.swapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * A writable clone of the Star Wars API.
 *
 * <p>It exists because the public swapi mirrors are read-only and static — they ignore query
 * parameters and accept no writes — so nothing built on them can demonstrate a complete CRUD. This
 * serves the same data model with search, filters, paging, writes and real references between
 * tables, over an in-memory H2 seeded at boot.
 */
@SpringBootApplication
public class SwapiApplication {

  public static void main(String[] args) {
    SpringApplication.run(SwapiApplication.class, args);
  }
}
