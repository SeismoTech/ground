package com.seismotech.ground.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.net.URL;

public class XStream {
  private XStream() {}

  public static byte[] read(final InputStream in)
  throws IOException {
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    in.transferTo(out);
    return out.toByteArray();
  }

  public static byte[] read(final URL url)
  throws IOException {
    try (final InputStream in = url.openStream()) {return read(in);}
  }
}
