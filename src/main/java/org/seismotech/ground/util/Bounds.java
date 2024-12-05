package org.seismotech.ground.util;

/**
 * Operations on indexes related to the bounds of the indexed data structure.
 */
public class Bounds {
  public static int clamp(int min, int i, int max) {
    return Math.min(Math.max(min,i),max);
  }
}
