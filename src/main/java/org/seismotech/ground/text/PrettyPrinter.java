package org.seismotech.ground.text;

import java.io.OutputStream;
import java.io.Writer;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.util.Deque;
import java.util.ArrayDeque;

public class PrettyPrinter {

  public static final int DEFAULT_INDENT_DELTA = 2;

  private static final int PENDING_INDENT = 1;
  private static final int PENDING_SEP = 2;

  private final PrintWriter out;
  private final StringWriter sw;
  private int delta;
  private boolean omitEmptyLines;
  private Deque<String> sepStack;
  private int indent;
  private String sep;
  private int pending;

  public PrettyPrinter(PrintWriter out) {this(out, null);}

  public PrettyPrinter(StringWriter out) {this(new PrintWriter(out), out);}

  private PrettyPrinter(PrintWriter out, StringWriter sw) {
    this.out = out;
    this.sw = sw;
    this.delta = DEFAULT_INDENT_DELTA;
    this.omitEmptyLines = false;
    this.sepStack = new ArrayDeque<>();
    this.indent = 0;
    this.sep = null;
    this.pending = 0;
  }

  public PrettyPrinter indenting(int delta) {
    this.delta = delta;
    return this;
  }

  public PrettyPrinter omittingEmptyLines(boolean enabled) {
    omitEmptyLines = enabled;
    return this;
  }

  public static PrettyPrinter stdout() {return of(System.out);}
  public static PrettyPrinter stderr() {return of(System.err);}

  public static PrettyPrinter collect() {
    return new PrettyPrinter(new StringWriter());
  }

  public static PrettyPrinter of(PrintWriter out) {
    return new PrettyPrinter(out);
  }

  public static PrettyPrinter of(Writer out) {
    return of(new PrintWriter(out));
  }

  public static PrettyPrinter of(OutputStream out) {
    return of(new PrintWriter(out));
  }

  //----------------------------------------------------------------------
  public String string() {
    if (sw == null) throw new IllegalStateException(
      "Cannot get pretty printed content as an string");
    flush();
    return sw.toString();
  }

  public PrettyPrinter in() {
    indent += delta;
    return this;
  }

  public PrettyPrinter out() {
    indent -= delta;
    return this;
  }

  private void ln() {
    flushPendingLn();
    pending |= PENDING_INDENT;
  }

  public PrettyPrinter open(String sep) {
    if (this.sep != null) {
      flushPendingSep();
      sepStack.addLast(sep);
    }
    this.sep = sep;
    return this;
  }

  public PrettyPrinter close() {
    sep = sepStack.pollLast();
    pending &= ~PENDING_SEP;
    return this;
  }

  public PrettyPrinter sep() {
    pending |= PENDING_SEP;
    return this;
  }

  private void flushPending() {
    if (pending != 0) emitPending();
  }

  private void emitPending() {
    flushPendingSep();
    flushPendingIndent();
    pending = 0;
  }

  private void flushPendingSep() {
    if ((pending & PENDING_SEP) != 0) emitSep();
  }

  private void flushPendingIndent() {
    if ((pending & PENDING_INDENT) != 0) emitIndent();
  }

  private void flushPendingLn() {
    if (!omitEmptyLines && (pending & PENDING_INDENT) != 0) emitLn();
  }

  private void emitSep() {out.print(sep);}

  private void emitIndent() {out.write(whitespace(indent), 0, indent+1);}

  private void emitLn() {out.println();}

  //----------------------------------------------------------------------
  public PrettyPrinter flush() {
    flushPendingLn();
    out.flush();
    return this;
  }

  public PrettyPrinter println() {ln();  return this;}

  public PrettyPrinter println(int n) {print(n);  return println();}
  public PrettyPrinter println(long n) {print(n);  return println();}
  public PrettyPrinter println(float n) {print(n);  return println();}
  public PrettyPrinter println(double n) {print(n);  return println();}

  public PrettyPrinter println(CharSequence cs) {print(cs);  return println();}
  public PrettyPrinter println(char[] cs) {print(cs);  return println();}

  public PrettyPrinter println(CharSequence cs, int init, int end) {
    print(cs, init, end);
    return println();
  }

  public PrettyPrinter println(char[] cs, int init, int end) {
    print(cs, init, end);
    return println();
  }

  public PrettyPrinter print(int n) {
    flushPending();
    out.print(n);
    return this;
  }

  public PrettyPrinter print(long n) {
    flushPending();
    out.print(n);
    return this;
  }

  public PrettyPrinter print(float x) {
    flushPending();
    out.print(x);
    return this;
  }

  public PrettyPrinter print(double x) {
    flushPending();
    out.print(x);
    return this;
  }

  public PrettyPrinter print(CharSequence cs) {
    if (cs != null) print(cs, 0, cs.length());
    return this;
  }

  public PrettyPrinter print(char[] cs) {
    if (cs != null) print(cs, 0, cs.length);
    return this;
  }

  public PrettyPrinter print(CharSequence cs, int init, int end) {
    int off = init, i = init;
    for (;;) {
      while (i < end && cs.charAt(i) != '\n') i++;
      raw(cs, off, i);
      if (i == end) break;
      println();
      off = ++i;
    }
    return this;
  }

  public PrettyPrinter print(char[] cs, int init, int end) {
    int off = init, i = init;
    for (;;) {
      while (i < end && cs[i] != '\n') i++;
      raw(cs, off, i);
      if (i == end) break;
      println();
      off = ++i;
    }
    return this;
  }

  public PrettyPrinter raw(CharSequence cs) {
    if (cs != null) raw(cs, 0, cs.length());
    return this;
  }

  public PrettyPrinter raw(CharSequence cs, int init, int end) {
    if (init < end) {
      flushPending();
      out.append(cs, init, end);
    }
    return this;
  }

  public PrettyPrinter raw(char[] cs) {
    if (cs != null) raw(cs, 0, cs.length);
    return this;
  }

  public PrettyPrinter raw(char[] cs, int init, int end) {
    if (init < end) {
      flushPending();
      out.write(cs, init, end-init);
    }
    return this;
  }

  //----------------------------------------------------------------------
  private static char[] WS = buildWhitespace(256);

  private static char[] whitespace(int atLeast) {
    if (WS.length < atLeast) expandWhiteSpace(atLeast);
    return WS;
  }

  private static char[] buildWhitespace(int n) {
    final char[] cs = new char[n+1];
    cs[0] = '\n';
    for (int i = 1; i <= n; i++) cs[i] = ' ';
    return cs;
  }

  private static void expandWhiteSpace(int n) {
    WS = buildWhitespace(Math.max(2*WS.length, n));
  }
}
