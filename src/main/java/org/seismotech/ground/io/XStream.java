package org.seismotech.ground.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.Reader;
import java.io.CharArrayWriter;
import java.io.StringWriter;
import java.net.URL;

public class XStream {
  private XStream() {}

  public static int readMax(InputStream in, byte[] data)
  throws IOException {
    return readMax(in, data, 0, data.length);
  }

  public static int readMax(InputStream in, byte[] data, int off, int len)
  throws IOException {
    return readMin(in, data, off, len, len);
  }

  public static int readMin(InputStream in,
      byte[] data, int off, int min, int max)
  throws IOException {
    final int end = off + max;
    final int threshold = off + min;
    int i = off;
    while (i < threshold) {
      final int r = in.read(data, i, end-i);
      if (r < 0) break;
      i += r;
    }
    return i - off;

  }

  public static byte[] read(InputStream in)
  throws IOException {
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    in.transferTo(out);
    return out.toByteArray();
  }

  public static byte[] read(URL url)
  throws IOException {
    try (final InputStream in = url.openStream()) {return read(in);}
  }

  public static char[] read(Reader in)
  throws IOException {
    final CharArrayWriter out = new CharArrayWriter();
    in.transferTo(out);
    return out.toCharArray();
  }

  public static String readAsString(Reader in)
  throws IOException {
    final StringWriter out = new StringWriter();
    in.transferTo(out);
    return out.toString();
  }
}
