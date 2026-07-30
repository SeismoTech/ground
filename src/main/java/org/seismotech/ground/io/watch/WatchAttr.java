package org.seismotech.ground.io.watch;

import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;

public record WatchAttr(
  Path name,
  Object key,
  Instant creationTime,
  Instant lastModifiedTime,
  Instant lastAccessTime,
  Instant watchTime,
  long size) {

  public static WatchAttr of(Path name, Instant now, BasicFileAttributes attr) {
    return new WatchAttr(
      name,
      attr.fileKey(),
      attr.creationTime().toInstant(),
      attr.lastModifiedTime().toInstant(),
      attr.lastAccessTime().toInstant(),
      now,
      attr.size());
  }

  public boolean isAnother(WatchAttr that) {
    return !this.creationTime.equals(that.creationTime)
      || this.key != null && that.key != null && !this.key.equals(that.key);
  }

  public boolean wasModified(WatchAttr that) {
    return !this.lastModifiedTime.equals(that.lastModifiedTime)
      || this.size != that.size;
  }
}
