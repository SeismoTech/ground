package org.seismotech.ground.io.watch;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.FileVisitor;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Stack;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.function.Predicate;
import java.util.function.Consumer;

import org.seismotech.ground.lang.ExceptionCollector;
import org.seismotech.ground.io.MultiIOException;
import org.seismotech.ground.util.Tuple2;
import org.seismotech.ground.service.TerminationBarrier;

public class TraversalWatcherMachine {

  private final List<Path> roots;
  private final Predicate<Path> accept;
  private final Consumer<WatchEvent> listener;
  private Forest lastSnapshot;

  public TraversalWatcherMachine(List<Path> roots,
      Predicate<Path> accept, Consumer<WatchEvent> listener) {
    this.roots = new ArrayList<>(roots);
    this.accept = accept;
    this.listener = listener;
    this.lastSnapshot = Forest.empty(roots);
  }

  public void traverse() throws IOException {
    final Forest current = snapshot();
    emitChanges(lastSnapshot, current);
    lastSnapshot = current;
    if (current.errors.has()) throw current.errors.exception();
  }

  private Forest snapshot() throws IOException {
    final Forest current = Forest.empty();
    final ForestBuilder fb = new ForestBuilder();
    for (final Path root: roots) {
      Files.walkFileTree(
        root, Set.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE, fb);
      current.add(root, fb.lastEntry != null ? fb.lastEntry : new Dir(root));
    }
    current.errors = fb.errors;
    return current;
  }

  private void emitChanges(Forest prev, Forest curr) {
    for (final Path root: roots) {
      emitChanges(root, prev.roots.get(root), curr.roots.get(root));
    }
  }

  private void emitChanges(Path path, Entry prev, Entry curr) {
    if (prev == null) emitCreate(path, curr);
    else if (curr == null) emitDelete(path, prev);
    else switch (prev) {
      case File fprev -> {
        switch (curr) {
        case File fcurr -> emitChanges(path, fprev, fcurr);
        case Dir dcurr -> {
          emitDelete(path, fprev);
          emitCreate(path, dcurr);
        }
        }
      }
      case Dir dprev -> {
        switch (curr) {
        case Dir dcurr -> emitChanges(path, dprev, dcurr);
        case File fcurr -> {
          emitDelete(path, dprev);
          emitCreate(path, fcurr);
        }
        }
      }
      }
  }

  private void emitChanges(Path path, File prev, File curr) {
    if (prev.attr.isAnother(curr.attr)) {
      emitDelete(path, prev);
      emitCreate(path, prev);
    } else if (prev.attr.wasModified(curr.attr)) {
      emitModify(path, curr);
    }
  }

  private void emitChanges(Path path, Dir prev, Dir curr) {
    final Iterator<Tuple2<String,Entry>> pentries = prev.entries.iterator();
    final Iterator<Tuple2<String,Entry>> centries = curr.entries.iterator();
    Tuple2<String,Entry> pentry = advance(pentries);
    Tuple2<String,Entry> centry = advance(centries);
    while (pentry != null && centry != null) {
      final int cmp = pentry._1().compareTo(centry._1());
      if (cmp == 0) {
        emitChanges(path.resolve(pentry._1()), pentry._2(), centry._2());
        pentry = advance(pentries);
        centry = advance(centries);
      } else if (cmp < 0) {
        emitDelete(path.resolve(pentry._1()), pentry._2());
        pentry = advance(pentries);
      } else /*if (cmp > 0)*/ {
        emitCreate(path.resolve(centry._1()), centry._2());
        centry = advance(centries);
      }
    }
    while (pentry != null) {
      emitDelete(path.resolve(pentry._1()), pentry._2());
      pentry = advance(pentries);
    }
    while (centry != null) {
      emitCreate(path.resolve(centry._1()), centry._2());
      centry = advance(centries);
    }
  }

  private static <T> T advance(Iterator<T> it) {
    return it.hasNext() ? it.next() : null;
  }

  private void emitCreate(Path path, Entry entry) {
    switch (entry) {
    case File fentry -> emitCreate(path, fentry);
    case Dir dentry -> emitCreate(path, dentry);
    }
  }

  private void emitDelete(Path path, Entry entry) {
    switch (entry) {
    case File fentry -> emitDelete(path, fentry);
    case Dir dentry -> emitDelete(path, dentry);
    }
  }

  private void emitCreate(Path path, Dir dir) {
    for (final Tuple2<String,Entry> entry: dir.entries) {
      emitCreate(path.resolve(entry._1()), entry._2());
    }
  }

  private void emitDelete(Path path, Dir dir) {
    for (final Tuple2<String,Entry> entry: dir.entries) {
      emitDelete(path.resolve(entry._1()), entry._2());
    }
  }

  private void emitCreate(Path path, File file) {
    emit(path, file, WatchCase.CREATE);
  }

  private void emitDelete(Path path, File file) {
    emit(path, file, WatchCase.DELETE);
  }

  private void emitModify(Path path, File file) {
    emit(path, file, WatchCase.MODIFY);
  }

  private void emit(Path path, File file, WatchCase what) {
    listener.accept(new WatchEvent(what, file.attr));
  }

  //----------------------------------------------------------------------
  private static class Forest {
    final Map<Path,Entry> roots;
    ExceptionCollector<IOException> errors;

    Forest() {
      this.roots = new HashMap<>();
      this.errors = null;
    }

    static Forest empty() {return new Forest();}

    static Forest empty(List<Path> roots) {
      return new Forest().emptyRoots(roots);
    }

    Forest emptyRoots(List<Path> roots) {
      for (final Path root: roots) add(root, new Dir(root));
      return this;
    }

    void add(Path root, Entry entry) {
      roots.put(root, entry);
    }

    // void sort() {
    //   Collections.sort(roots, (a,b) -> a._1().compareTo(b._1()));
    // }
  }

  private static sealed abstract class Entry permits File, Dir {
    final String name;

    Entry(String name) {this.name = name;}
  }

  private static final class File extends Entry {
    final WatchAttr attr;

    File(WatchAttr attr) {
      super(attr.name().getFileName().toString());
      this.attr = attr;
    }
  }

  private static final class Dir extends Entry {
    final List<Tuple2<String,Entry>> entries;

    Dir(String name) {
      super(name);
      this.entries = new ArrayList<>();
    }

    Dir(Path name) {
      this(name.getFileName().toString());
    }

    void add(Entry entry) {
      entries.add(Tuple2.of(entry.name, entry));
    }

    void sort() {
      Collections.sort(entries, (a,b) -> a._1().compareTo(b._1()));
    }
  }

  private class ForestBuilder implements FileVisitor<Path> {
    final Instant initTime;
    final ExceptionCollector<IOException> errors;
    final Stack<Dir> outerDirs;
    Entry lastEntry;
    Dir dir;

    ForestBuilder() {
      this.initTime = Instant.now();
      this.errors = new ExceptionCollector<>(MultiIOException::new);
      this.outerDirs = new Stack<>();
      this.lastEntry = null;
      this.dir = null;
    }

    @Override
    public FileVisitResult visitFileFailed(Path name, IOException e) {
      errors.add(e);
      return FileVisitResult.CONTINUE;
    }

    private void found(Entry entry) {
      lastEntry = entry;
      if (entry != null && dir != null) dir.add(entry);
    }

    @Override
    public FileVisitResult visitFile(Path name, BasicFileAttributes attrs) {
      found(
        !accept.test(name) ? null
        : new File(WatchAttr.of(name, initTime, attrs)));
      return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path name,
        BasicFileAttributes attrs) {
      outerDirs.push(dir);
      dir = new Dir(name);
      return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path name, IOException error) {
      errors.add(error);
      final Dir inner = dir;
      inner.sort();
      dir = outerDirs.pop();
      found(inner);
      return FileVisitResult.CONTINUE;
    }
  }
}
