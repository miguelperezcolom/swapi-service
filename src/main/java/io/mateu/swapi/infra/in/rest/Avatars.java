package io.mateu.swapi.infra.in.rest;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * A picture for every record, so a UI has something to show beside a name: a DiceBear avatar
 * (https://www.dicebear.com) generated from the record's name. Only styles under CC0 1.0 are used,
 * so the pictures carry no licence obligations, and the same name always draws the same picture.
 * PNG rather than SVG: a native client (React Native) cannot draw an SVG with its image component.
 */
final class Avatars {

  static final String PEOPLE = "notionists";
  static final String PLANETS = "rings";
  static final String SPECIES = "thumbs";
  static final String THINGS = "shapes";

  private Avatars() {}

  static String of(String style, String seed) {
    return "https://api.dicebear.com/9.x/" + style + "/png?size=256&seed="
        + URLEncoder.encode(seed == null ? "" : seed, StandardCharsets.UTF_8);
  }
}
