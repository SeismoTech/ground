package org.seismotech.ground.lang;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class String6Test {

  @Test void padLeftTest() {
    assertEquals("   X", String6.padLeft("X", 4));
    assertEquals("---X", String6.padLeft("X", 4, "-"));
    assertEquals("+-+X", String6.padLeft("X", 4, "-+"));
  }

  @Test void padRightTest() {
    assertEquals("X   ", String6.padRight("X", 4));
    assertEquals("X---", String6.padRight("X", 4, "-"));
    assertEquals("X-+-", String6.padRight("X", 4, "-+"));
  }
}
