package org.seismotech.ground.util;

/**
 * An small utility class to help with *standard* responses from searching
 * algorithms.
 *
 * <p>Some notation.
 * An *index* is an int with a direct location in a data structure.
 * Letters {@code i}, {@code j}, {@code k} are used for indeces.
 * A *position* is an int with an indirect location in a data structure.
 * Letters {@code p}, and {@code q} are used for positions.
 * A non-negative position is the location containing a value.
 * A negative position {@code p} is the encoding of the location where
 * an absent value should be inserted; the effective location is computed
 * as {@code -(p+1)}.
 * <p>
 * Indeces are always <i>in-of-range</i> of a data structure;
 * the data structure can be indexed without errors in that location
 * to get a value.
 * On the other side, the location extracted from a position could be
 * <i>out-of-range</i>, particularly if the insertion position is at the end
 * of the data structure.
 */
public class Index {

  public static int done(int i, boolean found) {
    return found ? found(i) : insertAt(i);
  }
  public static int done(int i, int cmp) {
    return cmp == 0 ? found(i) : insertAt(cmp < 0 ? i : i+1);
  }
  public static int found(int i) {return i;}
  public static int insertAt(int i) {return flip(i);}
  public static int continueAt(int i) {return flip(i);}

  public static boolean wasFound(int p) {return 0 <= p;}
  public static int effective(int p) {return wasFound(p) ? p : flip(p);}

  private static int flip(int a) {return -(a+1);}
}
