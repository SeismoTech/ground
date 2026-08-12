package org.seismotech.ground.cursor;

import java.util.Iterator;

public abstract class IteratorCursor<T> implements CursorOf<T> {
  private final Iterator<T> it;
  private Status status;
  private T last;

  public IteratorCursor(Iterator<T> it) {
    this.it = it;
    this.status = Status.PRISTINE;
    this.last = null;
  }

  @Override
  public Status status() {return status;}

  @Override
  public boolean advance() {
    if (it.hasNext()) {
      status = Status.LOADED;
      last = it.next();
      return true;
    } else {
      status = Status.ENDED;
      last = null;
      return false;
    }
  }

  @Override
  public T value() {return last;}
}
