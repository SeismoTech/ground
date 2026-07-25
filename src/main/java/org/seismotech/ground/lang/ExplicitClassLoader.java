package org.seismotech.ground.lang;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class ExplicitClassLoader extends ClassLoader {

  protected ExplicitClassLoader() {super();}

  protected ExplicitClassLoader(ClassLoader parent) {super(parent);}

  public abstract Class<?> loadClass(String name, byte[] code);

  //----------------------------------------------------------------------
  public static class Immediate extends ExplicitClassLoader {

    public Immediate() {super();}

    public Immediate(ClassLoader parent) {super(parent);}

    public Class<?> loadClass(String name, byte[] code) {
      return defineClass(name, code, 0, code.length);
    }
  }

  //----------------------------------------------------------------------
  public static class Delayed extends ExplicitClassLoader {

    private final Map<String,byte[]> pending = new ConcurrentHashMap<>();

    public Delayed() {super();}

    public Delayed(ClassLoader parent) {super(parent);}

    @Override
    public Class<?> loadClass(String name, byte[] code) {
      installClass(name, code);
      try {
        return loadClass(name);
      } catch (ClassNotFoundException shouldNotHappen) {
        throw new RuntimeException(
          "While loading installed class " + name, shouldNotHappen);
      }
    }

    public void installClass(String name, byte[] code) {
      pending.put(name, code);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
      final byte[] code = pending.get(name);
      if (code == null) throw new ClassNotFoundException(name);
      final Class<?> klass;
      try {
        klass = defineClass(name, code, 0, code.length);
      } catch (ClassFormatError e) {
        throw new ClassNotFoundException(name, e);
      }
      pending.remove(name);
      return klass;
    }
  }
}
