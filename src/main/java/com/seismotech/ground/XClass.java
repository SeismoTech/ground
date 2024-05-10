package com.seismotech.ground.lang;

public class XClass {
  private XClass() {}

  public static String classResourceName(String classname) {
    return classname.replace(".", "/") + ".class";
  }
}
