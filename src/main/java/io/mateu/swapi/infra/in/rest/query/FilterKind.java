package io.mateu.swapi.infra.in.rest.query;

/**
 * How one declared filter compares. Making this explicit per field, instead of inferring it from the
 * column type, is what keeps {@code ?gender=male} from also matching "female" — an enum-like column
 * is a pick, not a prefix, and only the API author knows which is which.
 */
public enum FilterKind {

  /** Case-insensitive containment. For prose-ish columns: a name, a model, a terrain. */
  TEXT,

  /** Case-insensitive equality. For a closed set of values: gender, classification, climate. */
  EXACT,

  /** Equality, plus the inclusive {@code <field>_from} / {@code <field>_to} range. */
  NUMBER,

  /** Same as NUMBER over an ISO-8601 date. */
  DATE,

  /** Equality on the id of a referenced row — {@code ?homeworldId=1}. */
  REF
}
