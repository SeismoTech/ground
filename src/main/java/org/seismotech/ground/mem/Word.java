package org.seismotech.ground.mem;

import java.util.Random;

/**
 * A signature to manage words of fixed {@link #width()}.
 */
public interface Word {
  int width();
  long clamp(long v);
  long get(ByteArray store, int i);
  void set(ByteArray store, int i, long v);

  long random(Random rnd);

  static final class Width16 implements Word {
    public static final Width16 THE = new Width16();
    @Override public int width() {return 16;}
    @Override public long clamp(long v) {return v & 0xFFFF;}
    @Override public long get(ByteArray store, int i) {
      return clamp(store.pget16(i));
    }
    @Override public void set(ByteArray store, int i, long v) {
      store.cset16(i, (short) v);
    }

    @Override public long random(Random rnd) {return clamp(rnd.nextInt());}
  }

  static final class Width32 implements Word {
    public static final Width32 THE = new Width32();
    @Override public int width() {return 32;}
    @Override public long clamp(long v) {return v & 0xFFFF_FFFF;}
    @Override public long get(ByteArray store, int i) {
      return clamp(store.pget32(i));
    }
    @Override public void set(ByteArray store, int i, long v) {
      store.cset32(i, (int) v);
    }

    @Override public long random(Random rnd) {return clamp(rnd.nextInt());}
  }

  static final class Width64 implements Word {
    public static final Width64 THE = new Width64();
    @Override public int width() {return 64;}
    @Override public long clamp(long v) {return v;}
    @Override public long get(ByteArray store, int i) {
      return store.pget64(i);
    }
    @Override public void set(ByteArray store, int i, long v) {
      store.cset64(i, v);
    }

    @Override public long random(Random rnd) {return rnd.nextLong();}
  }
}
