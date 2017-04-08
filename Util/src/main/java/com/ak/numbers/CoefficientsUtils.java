package com.ak.numbers;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.IntSummaryStatistics;
import java.util.function.ToDoubleFunction;

import javax.annotation.Nonnull;

public enum CoefficientsUtils {
  ;

  public static <C extends Enum<C> & Coefficients> IntSummaryStatistics rangeX(@Nonnull Class<C> coeffClass) {
    return range(coeffClass, value -> value[0]);
  }

  public static <C extends Enum<C> & Coefficients> IntSummaryStatistics rangeY(@Nonnull Class<C> coeffClass) {
    return range(coeffClass, value -> value[1]);
  }

  private static <C extends Enum<C> & Coefficients> IntSummaryStatistics range(@Nonnull Class<C> coeffClass,
                                                                               @Nonnull ToDoubleFunction<double[]> selector) {
    return EnumSet.allOf(coeffClass).stream().flatMapToDouble(
        coefficients -> Arrays.stream(coefficients.getPairs()).mapToDouble(selector)).
        mapToInt(value -> (int) Math.floor(value)).summaryStatistics();
  }
}
