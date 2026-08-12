package org.seismotech.ground.lang;

public class String6 {

  //----------------------------------------------------------------------
  public static final String DEFAULT_PADDING = " ";

  public static String padLeft(String str, int width) {
    return padLeft(str, width, DEFAULT_PADDING);
  }

  public static String padLeft(String str, int width, String pad) {
    return width <= str.length() ? str
      : padLeft(new StringBuilder(width), str, width, pad).toString();
  }

  public static StringBuilder padLeft(StringBuilder trg,
      String src, int width) {
    return padLeft(trg, src, width, DEFAULT_PADDING);
  }

  public static StringBuilder padLeft(StringBuilder trg,
      String src, int width, String pad) {
    if (src.length() < width) {
      final int p = pad.length();
      final int n = width - src.length();
      final int b = n / p, t = n % p;
      trg.append(pad, p - t, p);
      for (int i = 0; i < b; i++) trg.append(pad);
    }
    return trg.append(src);
  }

  public static String padRight(String str, int width) {
    return padRight(str, width, DEFAULT_PADDING);
  }

  public static String padRight(String str, int width, String pad) {
    return width < str.length() ? str
      : padRight(new StringBuilder(width), str, width, pad).toString();
  }

  public static StringBuilder padRight(StringBuilder trg,
      String src, int width) {
    return padRight(trg, src, width, DEFAULT_PADDING);
  }

  public static StringBuilder padRight(StringBuilder trg,
      String src, int width, String pad) {
    trg.append(src);
    if (src.length() < width) {
      final int p = pad.length();
      final int n = width - src.length();
      final int b = n / p, t = n % p;
      for (int i = 0; i < b; i++) trg.append(pad);
      trg.append(pad, 0, t);
    }
    return trg;
  }

  //----------------------------------------------------------------------
  public static String reversed(String str) {
    final StringBuilder sb = new StringBuilder(str.length());
    for (int i = str.length(); i > 0; ) {
      final int cp = str.codePointBefore(i);
      sb.appendCodePoint(cp);
      i -= Character.charCount(cp);
    }
    return sb.toString();
  }
}
