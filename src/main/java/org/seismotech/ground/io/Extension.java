package org.seismotech.ground.io;

import java.nio.file.Path;
import java.nio.file.Files;
import java.util.NoSuchElementException;
import java.util.Iterator;

public class Extension {

  //----------------------------------------------------------------------
  public static String lastOf(Path path) {
    return lastOf(path.getFileName().toString());
  }

  public static String lastOf(String name) {
    final int dot = name.lastIndexOf('.');
    return (dot == -1) ? null : name.substring(dot+1);
  }

  public static Iterable<String> allOf(Path path) {
    return allOf(path.getFileName().toString());
  }

  public static Iterable<String> allOf(String name) {
    return () -> new ExtensionsIterator(name);
  }

  public static class ExtensionsIterator implements Iterator<String> {
    private final String name;
    private int dot;
    private int end;

    public ExtensionsIterator(String name) {
      this.name = name;
      this.dot = this.end = name.length();
    }

    public boolean hasNext() {
      return ensureNext();
    }

    public String next() {
      if (!ensureNext()) throw new NoSuchElementException();
      final String ext = name.substring(dot+1, end);
      end = dot;
      return ext;
    }

    private boolean ensureNext() {return (dot < end) || findNext();}

    private boolean findNext() {
      final int cand = name.lastIndexOf('.', end-1);
      final boolean ok = cand != -1;
      if (ok) dot = cand;
      return ok;
    }
  }

  //----------------------------------------------------------------------
  public static int whichLast(Path path, String... exts) {
    return whichLast(path.getFileName().toString(), exts);
  }

  public static int whichLast(String name, String... exts) {
    for (int i = 0; i < exts.length; i++) {
      final String ext = exts[i];
      if (name.endsWith(ext)
          && name.charAt(name.length()-ext.length()-1) == '.') return i;
    }
    return -1;
  }

  public static boolean isLast(Path path, String... exts) {
    return whichLast(path, exts) != -1;
  }

  public static boolean isLast(String name, String... exts) {
    return whichLast(name, exts) != -1;
  }

  //----------------------------------------------------------------------
  public static Path removeLast(Path path) {
    return path.resolveSibling(removeLast(path.getFileName().toString()));
  }

  public static String removeLast(String name) {
    final int dot = name.lastIndexOf('.');
    return (dot == -1) ? name : name.substring(0, dot);
  }

  public static Path removeAll(Path path) {
    return path.resolveSibling(removeAll(path.getFileName().toString()));
  }

  public static String removeAll(String name) {
    final int dot = name.indexOf('.');
    return (dot == -1) ? name : name.substring(0, dot);
  }

  public static Path removeLastWhen(Path path, String... exts) {
    final String name = path.getFileName().toString();
    final String rname = removeLastWhen(name, exts);
    return rname == name ? path : path.resolveSibling(rname);
  }

  public static String removeLastWhen(String name, String... exts) {
    final int i = whichLast(name, exts);
    return (i == -1) ? name
      : name.substring(0, name.length() - exts[i].length() - 1);
  }

  //----------------------------------------------------------------------
  public static Path with(Path path, String ext) {
    return path.resolveSibling(path.getFileName().toString() + "." + ext);
  }

  //----------------------------------------------------------------------
  public static Path find(Path path, String... exts) {
    if (Files.exists(path)) return path;
    final Path rpath = removeLastWhen(path, exts);
    for (final String ext: exts) {
      final Path cpath = with(rpath, ext);
      if (Files.exists(cpath)) return cpath;
    }
    return null;
  }
}
