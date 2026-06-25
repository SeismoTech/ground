package org.seismotech.ground.text;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PrettyPrinterTest {

  @Test void simpleIndented() {
    assertEquals(
      "Block[\n  Nested[\n    Field\n  ]\n]",
      PrettyPrinter.collect()
      .print("Block[")
      .  in()
      .  print("\nNested[")
      .  in()
      .    print("\nField")
      .  out()
      .  print("\n]")
      .  out()
      .print("\n]")
      .string());
  }

  @Test void doubleIndented() {
    assertEquals(
      "Block[\n    Nested[\n      Field\n    ]\n]",
      PrettyPrinter.collect()
      .print("Block[")
      .  in().in()
      .  print("\nNested[")
      .  in()
      .    print("\nField")
      .  out()
      .  print("\n]")
      .  out().out()
      .print("\n]")
      .string());
  }

  @Test void separatorOnALine() {
    assertEquals(
      "f(a, b, c)",
      PrettyPrinter.collect()
      .print("f(")
      .open(", ")
      .  print("a").sep().print("b").sep().print("c").sep()
      .close()
      .print(")")
      .string());
  }

  @Test void separatorByLines() {
    assertEquals(
      "f(\n  a,\n  b,\n  c\n)",
      PrettyPrinter.collect()
      .print("f(")
      .in()
      .open(",")
      .  print("\na").sep().print("\nb").sep().print("\nc").sep()
      .close()
      .out()
      .print("\n)")
      .string());
  }

  @Test void separatorByLines2() {
    assertEquals(
      "f(a,\n  b,\n  c\n)",
      PrettyPrinter.collect()
      .print("f(")
      .in()
      .open(",")
      .  println("a").sep().println("b").sep().println("c").sep()
      .close()
      .out()
      .print(")")
      .string());
  }

  @Test void keepsEmptyLines() {
    assertEquals(
      "f(\n\n)",
      PrettyPrinter.collect().print("f(\n\n)").string());
  }

  @Test void omitEmptyLinesIfConfigured() {
    assertEquals(
      "f(\n)",
      PrettyPrinter.collect().omittingEmptyLines(true)
      .print("f(\n\n)")
      .string());
  }

  @Test void emptyLinesAreEmpty() {
    assertEquals(
      "Block[\n\n  Field1\n\n  Field2\n]",
      PrettyPrinter.collect()
      .print("Block[")
      .in()
      .  println()
      .  print("\nField1")
      .  println()
      .  print("\nField2")
      .out()
      .print("\n]")
      .string());
  }

  @Test void trailingEolnAreKept() {
    assertEquals(
      "Block[]\n",
      PrettyPrinter.collect()
      .println("Block[]")
      .string());
    assertEquals(
      "Block[]\n\n",
      PrettyPrinter.collect()
      .println("Block[]\n")
      .string());
  }
}
