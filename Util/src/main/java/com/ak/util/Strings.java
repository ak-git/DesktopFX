package com.ak.util;

import java.util.Arrays;
import java.util.stream.Collectors;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import tec.uom.se.unit.MetricPrefix;

import static tec.uom.se.unit.Units.METRE;
import static tec.uom.se.unit.Units.OHM;

public enum Strings {
  ;
  public static final String EMPTY = "";
  public static final String SPACE = " ";
  public static final String POINT = ".";
  public static final String COMMA = ",";
  public static final String NEW_LINE = String.format("%n");
  public static final String NEW_LINE_2 = String.format("%n%n");
  public static final String TAB = "\t";
  public static final String OHM_METRE = new StringBuilder(OHM.multiply(METRE).toString()).reverse().toString();
  private static final String RHO = "\u03c1";

  public static String numberSuffix(@Nonnull String s) {
    String ignore = s.replaceFirst("\\d*$", EMPTY);
    return s.replace(ignore, EMPTY);
  }

  public static String toString(@Nonnull String format, @Nonnull double[] values) {
    return Arrays.stream(values).mapToObj(x -> String.format(format, x)).collect(Collectors.joining("; ", "{", "}"));
  }

  public static String h(@Nonnegative double h, @Nonnegative int index) {
    return String.format("h%s = %.2f %s", low(index), Metrics.toMilli(h), MetricPrefix.MILLI(METRE));
  }

  public static String dRhoByH(double v) {
    return String.format("d\u03c1/dh = %.0f %s", v, OHM);
  }

  public static String rho(@Nonnegative double rho) {
    return String.format("%s = %.3f %s", RHO, rho, OHM_METRE);
  }

  public static String rho1(@Nonnegative double rho1) {
    return rho(rho1, 1);
  }

  public static String rho2(@Nonnegative double rho2) {
    return rho(rho2, 2);
  }

  public static char low(int index) {
    return (char) ((int) '\u2080' + index);
  }

  private static String rho(@Nonnegative double rho, @Nonnegative int index) {
    return String.format("%s%s = %.3f %s", RHO, low(index), rho, OHM_METRE);
  }
}
