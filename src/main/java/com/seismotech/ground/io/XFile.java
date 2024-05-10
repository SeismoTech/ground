package com.seismotech.ground.io;

import java.io.IOException;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.FileVisitResult;
import static java.nio.file.FileVisitResult.CONTINUE;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * File utility functions.
 */
public class XFile {
  private XFile() {}

  public static long deleteTree(String root)
  throws IOException {
    return deleteTree(Paths.get(root));
  }

  public static long deleteTree(File root)
  throws IOException {
    return deleteTree(root.toPath());
  }

  public static long deleteTree(Path root)
  throws IOException {
    if (!Files.exists(root)) return 0;
    final TreeDeleter deleter = new TreeDeleter();
    Files.walkFileTree(root, deleter);
    return deleter.totalDeleted();
  }

  public static class TreeDeleter extends SimpleFileVisitor<Path> {
    private long dirs = 0;
    private long files = 0;

    public long totalDeleted() {return dirs + files;}
    public long dirsDeleted() {return dirs;}
    public long filesDeleted() {return files;}

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
    throws IOException {
      if (Files.deleteIfExists(file)) files++;
      return CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path file, IOException err)
    throws IOException {
      if (err != null) throw err;
      if (Files.deleteIfExists(file)) dirs++;
      return CONTINUE;
    }
  }
}
