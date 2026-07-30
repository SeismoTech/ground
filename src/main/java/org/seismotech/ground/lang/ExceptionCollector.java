package org.seismotech.ground.lang;

import java.util.List;
import java.util.ArrayList;
import java.util.function.Function;

public class ExceptionCollector<E extends Exception> {

  private final Function<ExceptionBundle<E>, ? extends E> newMulti;
  private List<E> causes;

  public ExceptionCollector(Function<ExceptionBundle<E>, ? extends E> newMulti){
    this.newMulti = newMulti;
    this.causes = null;
  }

  public boolean has() {return causes != null;}

  public E exception() {
    return causes == null ? null
      : causes.size() == 1 ? causes.get(0)
      : newMulti.apply(new ExceptionBundle<>(causes));
  }

  public void add(E cause) {
    if (cause == null) return;
    if (causes == null) causes = new ArrayList<>();
    causes.add(cause);
  }
}
