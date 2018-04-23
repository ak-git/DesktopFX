package com.ak.util;

import javax.annotation.Nonnull;

public enum Strings {
  ;
  public static final String EMPTY = "";
  public static final String SPACE = " ";
  public static final String NEW_LINE = String.format("%n");
  public static final String NEW_LINE_2 = String.format("%n%n");
  public static final String TAB = "\t";
  public static final String DELTA = "\u03b4";
  public static final String RHO = "\u03c1";
  public static final String LOW_1 = "\u2081";
  public static final String LOW_2 = "\u2082";

  public static String numberSuffix(@Nonnull String s) {
    String ignore = s.replaceFirst("\\d*$", EMPTY);
    return s.replace(ignore, EMPTY);
  }
}
