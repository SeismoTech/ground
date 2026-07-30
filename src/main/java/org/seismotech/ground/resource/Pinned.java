package org.seismotech.ground.resource;

public interface Pinned<T> {
  T get();
  void unpin();
}
