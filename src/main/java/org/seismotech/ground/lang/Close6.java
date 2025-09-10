package org.seismotech.ground.lang;

public class Close6 {
  public static void closeIgnoring(AutoCloseable obj) {
    if (obj != null) try { obj.close(); } catch (Exception ignored) {}
  }
}
