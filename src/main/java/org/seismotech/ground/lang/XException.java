package org.seismotech.ground.lang;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Constructor;

public class XException {

  public static <E extends Throwable> E as(Class<E> eclass, Throwable e) {
    return eclass.isInstance(e) ? eclass.cast(e) : wrap(eclass, e);
  }

  public static <E extends Throwable> E wrap(Class<E> eclass, Throwable e) {
    try {
      Throwable w = null;
      final String msg = e.getMessage();
      if (msg != null) {
        try {
          w = eclass.getConstructor(String.class).newInstance(msg);
        } catch (Exception ignored) {}
      }
      if (w == null) w = eclass.getConstructor().newInstance();
      return eclass.cast(w);
    } catch (InstantiationException
        | IllegalAccessException
        | InvocationTargetException
        | NoSuchMethodException re) {
      throw new IllegalArgumentException(
        "Cannot wrap " + e.getClass() + " with " + eclass
        + ": " + re.getMessage(), e);
    }
  }
}
