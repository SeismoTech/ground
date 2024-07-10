package com.seismotech.ground.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.nio.file.Path;

public interface OpenDriver {

  String name();

  boolean managesExtension(String ext);

  InputStream inputStream(String ext, Path path) throws IOException;

  InputStream inputStream(String ext, InputStream in) throws IOException;

  //----------------------------------------------------------------------
  static class Util {

    public static InputStream inputStream(OpenDriver driver,
        String ext, Path path)
    throws IOException {
      InputStream base = null;
      InputStream filtered = null;
      try {
        base = new FileInputStream(path.toFile());
        filtered = driver.inputStream(ext, path);
      } finally {
        if (filtered == null && base != null) base.close();
      }
      return filtered;
    }
  }
}
