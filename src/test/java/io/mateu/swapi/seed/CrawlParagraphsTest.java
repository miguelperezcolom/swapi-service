package io.mateu.swapi.seed;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CrawlParagraphsTest {

  @Test
  @DisplayName("the crawl's screen-wrapped lines are joined into paragraphs, the blank line between them kept")
  void joinsWrappedLines() {
    String source = "Turmoil has engulfed the\r\nGalactic Republic.\r\n\r\nHoping to resolve\r\nthe matter.\r\n";
    assertThat(SwapiSeeder.paragraphs(source))
        .isEqualTo("Turmoil has engulfed the Galactic Republic.\n\nHoping to resolve the matter.");
  }

  @Test
  @DisplayName("no crawl stays no crawl")
  void nullStaysNull() {
    assertThat(SwapiSeeder.paragraphs(null)).isNull();
  }
}
