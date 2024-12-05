package org.seismotech.ground.lang;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.seismotech.ground.lang.Comparisons.*;

class ComparisonsTest {

  @Test void lltOnExtremes() {
    assertTrue(llt(MIN_INT, 0));
    assertTrue(llt(MIN_INT+1, 0));
    assertTrue(llt(MIN_INT+1, MAX_INT-1));
    assertTrue(llt(MIN_INT+1, MAX_INT));
    assertTrue(llt(MIN_INT, MAX_INT-1));
    assertTrue(llt(MIN_INT, MAX_INT));
    assertTrue(llt(0, MAX_INT-1));
    assertTrue(llt(0, MAX_INT));

    assertFalse(llt(MAX_INT, MIN_INT));
    assertFalse(llt(MAX_INT, MIN_INT+1));
    assertFalse(llt(MAX_INT-1, MIN_INT));
    assertFalse(llt(MAX_INT-1, MIN_INT+1));
  }
}
