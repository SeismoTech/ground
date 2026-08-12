package org.seismotech.ground.resource;

public interface Pinned<T> extends AutoCloseable {
  T get();
  void unpin();
  default void close() {unpin();}
}
