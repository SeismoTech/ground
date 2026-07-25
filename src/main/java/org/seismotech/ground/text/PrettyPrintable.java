package org.seismotech.ground.text;

public interface PrettyPrintable {
  PrettyPrinter prettyPrint(PrettyPrinter pp);

  interface Enumerated {
    PrettyPrinter prettyPrint(PrettyPrinter pp, int i);
  }
}
