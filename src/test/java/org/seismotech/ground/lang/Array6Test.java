package org.seismotech.ground.lang;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Array6Test {

  @Test void testAllocAs() {
    int[] is = Array6.newAs(new int[0], 10);
    assertEquals(10, is.length);
    is = Array6.newAs((int[]) null, 11);
    assertEquals(11, is.length);

    String[] ss = Array6.newAs(new String[0], 10);
    assertEquals(10, ss.length);
    assertThrows(NullPointerException.class,
        () -> Array6.newAs((String[]) null, 11));
  }
}
