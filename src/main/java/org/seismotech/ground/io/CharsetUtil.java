package org.seismotech.ground.io;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class CharsetUtil {
  private CharsetUtil() {}

  public static final Charset UTF8 = StandardCharsets.UTF_8;

  public static String utf8(byte[] bs) {
    return utf8(bs, 0, bs.length);
  }

  public static String utf8(byte[] bs, int init, int end) {
    return new String(bs, init, end-init, UTF8);
  }

  public static byte[] utf8(String txt) {
    return txt.getBytes(UTF8);
  }
}
