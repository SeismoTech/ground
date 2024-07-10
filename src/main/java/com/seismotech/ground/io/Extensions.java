package com.seismotech.ground.io;

import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.Iterator;

public class Extensions {

  public static Iterable<String> of(Path name) {
    return of(name.getFileName().toString());
  }

  public static Iterable<String> of(String name) {
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
}
