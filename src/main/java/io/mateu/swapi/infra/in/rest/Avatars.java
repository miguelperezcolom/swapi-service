package io.mateu.swapi.infra.in.rest;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * A picture for every record, so a UI has something to show beside a name: a DiceBear avatar
 * (https://www.dicebear.com) generated from the record itself. Only styles under CC0 1.0 are used,
 * so the pictures carry no licence obligations, and the same record always draws the same picture.
 * PNG rather than SVG: a native client (React Native) cannot draw an SVG with its image component.
 *
 * <p>People and species are drawn from what the data says about them — skin, hair and eye colours,
 * gender — so a character is recognisable without copying anybody's artwork: Yoda comes out green
 * with white hair, C-3PO gold, R2-D2 white and blue. Planets take their colours from the terrain.
 */
final class Avatars {

  static final String PEOPLE = "pixel-art";
  static final String PLANETS = "rings";
  static final String THINGS = "shapes";

  private static final Map<String, String> SKIN = Map.ofEntries(
      Map.entry("fair", "ffdbac"), Map.entry("light", "f5cfa0"), Map.entry("pale", "f8e5d8"),
      Map.entry("tan", "cb9e6e"), Map.entry("brown", "8d5524"), Map.entry("dark", "5c3a1e"),
      Map.entry("white", "f2f2f2"), Map.entry("gold", "d4af37"), Map.entry("metal", "b8bcc2"),
      Map.entry("silver", "c0c4ca"), Map.entry("green", "7fae5b"), Map.entry("green-tan", "9aa66b"),
      Map.entry("grey", "9a9a9a"), Map.entry("gray", "9a9a9a"), Map.entry("blue", "5b84d6"),
      Map.entry("red", "c0392b"), Map.entry("orange", "e67e22"), Map.entry("yellow", "e6c84a"),
      Map.entry("mottled green", "6e8b3d"), Map.entry("brown mottle", "7a5a3a"),
      Map.entry("purple", "8e5bb5"), Map.entry("pink", "e8a0b0"), Map.entry("peach", "f2c4a0"),
      Map.entry("caucasian", "ffdbac"), Map.entry("black", "3a2a20"), Map.entry("asian", "eac393"),
      Map.entry("hispanic", "cb9e6e"), Map.entry("magenta", "b5407a"), Map.entry("turquoise", "40b5a8"));

  private static final Map<String, String> HAIR = Map.of(
      "blond", "e8c872", "blonde", "e8c872", "brown", "603a14", "black", "28150a",
      "auburn", "8a3324", "grey", "a0a0a0", "gray", "a0a0a0", "white", "eeeeee", "red", "bd1700");

  private static final Map<String, String> EYES = Map.ofEntries(
      Map.entry("blue", "2f6fd8"), Map.entry("blue-gray", "6a7f99"), Map.entry("yellow", "e0b400"),
      Map.entry("red", "d01f1f"), Map.entry("brown", "5a3a1e"), Map.entry("black", "111111"),
      Map.entry("orange", "e67e22"), Map.entry("hazel", "8e7618"), Map.entry("pink", "e87a9a"),
      Map.entry("gold", "d4af37"), Map.entry("white", "eeeeee"), Map.entry("green", "2e8b57"),
      Map.entry("dark", "222222"), Map.entry("amber", "ffbf00"), Map.entry("silver", "c0c4ca"));

  private static final Map<String, String> TERRAIN = Map.ofEntries(
      Map.entry("desert", "e0c068"), Map.entry("dunes", "e0c068"), Map.entry("ocean", "3a7bd5"),
      Map.entry("oceans", "3a7bd5"), Map.entry("seas", "3a7bd5"), Map.entry("lakes", "4f9fd8"),
      Map.entry("ice", "cfe8f5"), Map.entry("tundra", "cfe8f5"), Map.entry("glaciers", "cfe8f5"),
      Map.entry("forests", "2e8b57"), Map.entry("jungle", "1f7a3f"), Map.entry("jungles", "1f7a3f"),
      Map.entry("rainforests", "1f7a3f"), Map.entry("grasslands", "7fbf4d"),
      Map.entry("grassy hills", "7fbf4d"), Map.entry("plains", "a8c66c"),
      Map.entry("mountains", "8a7f72"), Map.entry("hills", "9c8c6e"), Map.entry("swamp", "556b2f"),
      Map.entry("swamps", "556b2f"), Map.entry("gas giant", "e08a3c"),
      Map.entry("cityscape", "9a9aa8"), Map.entry("urban", "9a9aa8"), Map.entry("lava", "c0392b"),
      Map.entry("volcanoes", "c0392b"), Map.entry("rock", "8a8a8a"), Map.entry("rocky", "8a8a8a"),
      Map.entry("barren", "b5a48a"), Map.entry("savannas", "c9b458"), Map.entry("canyons", "b86b3c"), Map.entry("mountain", "8a7f72"), Map.entry("forest", "2e8b57"),
      Map.entry("lake", "4f9fd8"), Map.entry("sea", "3a7bd5"), Map.entry("river", "4f9fd8"),
      Map.entry("cave", "6b6258"), Map.entry("caves", "6b6258"), Map.entry("reef", "40b5a8"),
      Map.entry("islands", "3a7bd5"), Map.entry("fungus", "8e5bb5"), Map.entry("bogs", "556b2f"),
      Map.entry("scrublands", "a89a5a"), Map.entry("verdant", "4caf50"), Map.entry("airless asteroid", "8a8a8a"));

  private static final String HAIR_SHORT = numbered("short", 24);
  private static final String HAIR_LONG = numbered("long", 21);

  private Avatars() {}

  static String of(String style, String seed) {
    return url(style, Map.of("seed", seed == null ? "" : seed));
  }

  /** A character drawn from its colours and gender. */
  static String person(String name, String hair, String skin, String eyes, String gender) {
    var q = new LinkedHashMap<String, String>();
    q.put("seed", name == null ? "" : name);
    String skinHex = first(skin, SKIN);
    String hairHex = first(hair, HAIR);
    // no skin colour but hair (a Wookiee): the face takes the fur's colour
    String face = skinHex != null ? skinHex : hairHex != null ? hairHex : "d9d9d9";
    q.put("skinColor", face);
    String second = second(skin, SKIN);
    if (second != null) q.put("clothingColor", second); // R2-D2: white and blue
    q.put("hairColor", hairHex != null ? hairHex : face); // bald / droid: hair the colour of the head
    String eyesHex = first(eyes, EYES);
    if (eyesHex != null) q.put("eyesColor", eyesHex);
    boolean female = "female".equalsIgnoreCase(gender == null ? "" : gender.trim());
    q.put("hair", female ? HAIR_LONG : HAIR_SHORT);
    if (female) q.put("beardProbability", "0");
    // random extras (glasses, hats, earrings) say things the data does not
    q.put("glassesProbability", "0");
    q.put("hatProbability", "0");
    q.put("accessoriesProbability", "0");
    return url(PEOPLE, q);
  }

  /** A species drawn like a typical member: its first skin, hair and eye colours. */
  static String species(String name, String skinColors, String hairColors, String eyeColors) {
    return person(name, hairColors, skinColors, eyeColors, null);
  }

  /** A planet coloured by its main terrain (the style paints one colour, so the first one wins). */
  static String planet(String name, String terrain) {
    var q = new LinkedHashMap<String, String>();
    q.put("seed", name == null ? "" : name);
    String colours = tokens(terrain).map(Avatars::terrainColour).filter(c -> c != null).limit(1)
        .collect(Collectors.joining(","));
    if (!colours.isEmpty()) q.put("ringColor", colours);
    return url(PLANETS, q);
  }

  /** "lava rivers", "ice caves", "mountain ranges": the terrain word inside the phrase decides. */
  private static String terrainColour(String token) {
    String exact = TERRAIN.get(token);
    if (exact != null) return exact;
    for (String word : token.split("[ -]+")) {
      String c = TERRAIN.get(word);
      if (c == null && word.endsWith("s")) c = TERRAIN.get(word.substring(0, word.length() - 1));
      if (c != null) return c;
    }
    return null;
  }

  private static String url(String style, Map<String, String> query) {
    return "https://api.dicebear.com/9.x/" + style + "/png?size=256&" + query.entrySet().stream()
        .map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
        .collect(Collectors.joining("&"));
  }

  private static String first(String value, Map<String, String> palette) {
    return tokens(value).map(palette::get).filter(c -> c != null).findFirst().orElse(null);
  }

  private static String second(String value, Map<String, String> palette) {
    var known = tokens(value).map(palette::get).filter(c -> c != null).toList();
    return known.size() > 1 && !known.get(1).equals(known.get(0)) ? known.get(1) : null;
  }

  private static java.util.stream.Stream<String> tokens(String value) {
    if (value == null) return java.util.stream.Stream.empty();
    return java.util.Arrays.stream(value.split(","))
        .map(t -> t.trim().toLowerCase(Locale.ROOT))
        .filter(t -> !t.isEmpty());
  }

  private static String numbered(String prefix, int count) {
    return IntStream.rangeClosed(1, count).mapToObj(i -> String.format("%s%02d", prefix, i))
        .collect(Collectors.joining(","));
  }
}
