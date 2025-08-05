package com.ak.util;

import static javax.measure.MetricPrefix.MILLI;
import static tech.units.indriya.unit.Units.METRE;
import static tech.units.indriya.unit.Units.OHM;

public enum Strings {
  ;
  public static final String EMPTY = "";
  public static final String SPACE = " ";
  public static final String SEMICOLON = "; ";
  public static final String NEW_LINE = String.format("%n");
  public static final String NEW_LINE_2 = String.format("%n%n");
  public static final String TAB = "\t";
  public static final String OHM_METRE = OHM.multiply(METRE).toString();
  public static final String PLUS_MINUS = "±";
  public static final String ALPHA = "α";
  public static final String EPSILON = "ε";
  public static final String PHI = "ψ";
  public static final String CAP_DELTA = "Δ";
  public static final String ANGLE = "°";
  private static final String RHO = "ρ";

  public static String numberSuffix(String s) {
    int numCount = Math.toIntExact(new StringBuilder(s).reverse().chars().takeWhile(Character::isDigit).count());
    return s.substring(s.length() - numCount);
  }

  public static String dRhoByPhi(double v) {
    return "d%s/d%s = %.3f %s".formatted(RHO, PHI, v, OHM_METRE);
  }

  public static String rho(double rho) {
    return "%s = %.3f %s".formatted(RHO, rho, OHM_METRE);
  }

  public static String rho(Object rho) {
    return "%s = %s %s".formatted(RHO, rho, OHM_METRE);
  }

  public static char low(int index) {
    int i = '₀';
    return (char) (i + index);
  }

  public static String rho(int index, Object rho) {
    return "%s%s = %s %s".formatted(RHO, low(index), rho, OHM_METRE);
  }

  public static String h(int index, Object h) {
    return "h%s = %s %s".formatted(low(index), h, MILLI(METRE));
  }
}
