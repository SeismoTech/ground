package org.seismotech.ground.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.seismotech.ground.util.XArray.*;

class ArrayUtilTest {

  @Test
  void canAllocateMaxByteArray() {
    byte[] xs = new byte[MAX_SIZE];
  }

  @Test
  void canAllocateMaxIntArray() {
    int[] xs = new int[MAX_SIZE];
  }

  @Test
  void cannotAllocateBiggerByteArray() {
    assertThrows(
      OutOfMemoryError.class,
      () -> {byte xs[] = new byte[MAX_SIZE+1];});
  }

  @Test
  void cannotAllocateBiggerIntArray() {
    assertThrows(
      OutOfMemoryError.class,
      () -> {int xs[] = new int[MAX_SIZE+1];});
  }

  @Test
  void growSizeTest() {
    assertEquals(32, growSize(16, 15, 2));
    assertEquals(MAX_SIZE, growSize(MAX_SIZE-1000, MAX_SIZE-1000, 1000));
    assertThrows(
      IllegalStateException.class,
      () -> growSize(MAX_SIZE-1000, MAX_SIZE-1000, 1001));
  }
}
