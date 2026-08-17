package org.seismotech.ground.lang;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

public class ExceptionBundle<E extends Throwable> {
  private final List<E> causes;

  public ExceptionBundle(List<E> causes) {
    this.causes = new ArrayList<>(causes);
  }

  public String simpleMessage() {
    return title() + ". First one message: "
      + causes.get(0).getMessage();
  }

  public String fullMessage() {
    final StringBuilder sb = new StringBuilder(title());
    int i = 0;
    String sep = ": ";
    for (final E e: causes) {
      sb.append(sep).append('(').append(i).append(") ").append(e.getMessage());
      sep = ", ";
    }
    return sb.toString();
  }

  private String title() {return "Collecting " + causes.size() + " exceptions";}

  public E firstCause() {return causes.get(0);}

  public Iterable causes() {return Collections.unmodifiableList(causes);}
}
