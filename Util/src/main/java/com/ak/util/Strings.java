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
  public static final String OHM_METRE = new StringBuilder(OHM.multiply(METRE).toString()).reverse().toString();
  public static final String PLUS_MINUS = "±";
  public static final String ALPHA = "α";
  public static final String PHI = "ψ";
  public static final String CAP_DELTA = "Δ";
  public static final String ANGLE = "°";
  private static final String RHO = "ρ";

  @Nonnull
  public static String numberSuffix(@Nonnull String s) {
    int numCount = Math.toIntExact(new StringBuilder(s).reverse().chars().takeWhile(Character::isDigit).count());
    return s.substring(s.length() - numCount);
  }

  @Nonnull
  public static String dRhoByPhi(double v) {
    return "d%s/d%s = %.3f %s".formatted(RHO, PHI, v, OHM_METRE);
  }

  @Nonnull
  public static String rho(@Nonnegative double rho) {
    return "%s = %.3f %s".formatted(RHO, rho, OHM_METRE);
  }

  @Nonnull
  public static String rho(@Nonnull Object rho) {
    return "%s = %s %s".formatted(RHO, rho, OHM_METRE);
  }

  public static char low(int index) {
    int i = '₀';
    return (char) (i + index);
  }

  @Nonnull
  public static String rho(@Nonnegative int index, @Nonnull Object rho) {
    return "%s%s = %s %s".formatted(RHO, low(index), rho, OHM_METRE);
  }
}
