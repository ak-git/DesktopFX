package com.ak.util;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static tec.uom.se.unit.Units.METRE;
import static tec.uom.se.unit.Units.OHM;

public enum Strings {
  ;
  public static final String EMPTY = "";
  public static final String SPACE = " ";
  public static final String COMMA = ",";
  public static final String NEW_LINE = String.format("%n");
  public static final String NEW_LINE_2 = String.format("%n%n");
  public static final String TAB = "\t";
  public static final String OHM_METRE = new StringBuilder(OHM.multiply(METRE).toString()).reverse().toString();
  public static final String PLUS_MINUS = "\u00B1";
  public static final String PHI = "\u03C8";
  private static final String RHO = "\u03c1";

  public static String numberSuffix(@Nonnull String s) {
    String ignore = s.replaceFirst("\\d*$", EMPTY);
    return s.replace(ignore, EMPTY);
  }

  public static String dRhoByPhi(double v) {
    return "d%s/d%s = %.3f %s".formatted(RHO, PHI, v, OHM_METRE);
  }

  public static String rho(@Nonnegative double rho) {
    return "%s = %.3f %s".formatted(RHO, rho, OHM_METRE);
  }

  public static char low(int index) {
    return (char) ((int) '\u2080' + index);
  }

  public static String rho(@Nonnegative int index, @Nullable Object rho) {
    if (rho == null) {
      return "%s%s, %s".formatted(RHO, low(index), OHM_METRE);
    }
    else {
      return "%s%s = %s %s".formatted(RHO, low(index), rho, OHM_METRE);
    }
  }
}
