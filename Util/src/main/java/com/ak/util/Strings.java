package com.ak.util;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import static tec.uom.se.unit.Units.METRE;
import static tec.uom.se.unit.Units.OHM;

public enum Strings {
  ;
  public static final String EMPTY = "";
  public static final String SPACE = " ";
  public static final String NEW_LINE = String.format("%n");
  public static final String NEW_LINE_2 = String.format("%n%n");
  public static final String TAB = "\t";

  public static final String CAP_DELTA = "\u0394";
  public static final String DELTA = "\u03b4";

  private static final String RHO = "\u03c1";
  private static final String LOW_1 = "\u2081";
  private static final String LOW_2 = "\u2082";
  private static final String LOW_3 = "\u2083";
  private static final String LOW_12 = LOW_1 + LOW_2;
  private static final String LOW_23 = LOW_2 + LOW_3;

  public static final String K_12 = "k" + LOW_12;
  public static final String K_23 = "k" + LOW_23;

  public static final String OHM_METRE = new StringBuilder(OHM.multiply(METRE).toString()).reverse().toString();

  public static String numberSuffix(@Nonnull String s) {
    String ignore = s.replaceFirst("\\d*$", EMPTY);
    return s.replace(ignore, EMPTY);
  }

  public static String rho1(@Nonnegative double rho1) {
    return rho(rho1, 1);
  }

  public static String rho2(@Nonnegative double rho2) {
    return rho(rho2, 2);
  }

  private static String rho(@Nonnegative double rho, @Nonnegative int index) {
    return String.format("%s%s = %.3f %s", RHO, (char) ((int) '\u2080' + index), rho, OHM_METRE);
  }
}
