package org.seismotech.ground.io.drivers;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;

import org.seismotech.ground.io.OpenDriver;

public class GzipOpenDriver implements OpenDriver {

  @Override
  public String name() {
    return ":compression:gzip:";
  }

  @Override
  public boolean managesExtension(String eXt) {
    final String ext = eXt.toLowerCase();
    return ext.equals("gz") || ext.equals("gzip") || ext.equals("tgz");
  }

  @Override
  public InputStream inputStream(String ext, Path path)
  throws IOException {
    return Util.inputStream(this, ext, path);
  }

  @Override
  public InputStream inputStream(String ext, InputStream in)
  throws IOException {
    return new GZIPInputStream(in);
  }
}
