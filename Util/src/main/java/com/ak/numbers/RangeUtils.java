package com.ak.numbers;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.IntSummaryStatistics;
import java.util.function.ToDoubleFunction;

import javax.annotation.Nonnull;

public enum RangeUtils {
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

  public static double[] reverseOrder(double[] array) {
    var reverse = new double[array.length];
    for (var i = 0; i < reverse.length; i++) {
      reverse[i] = array[array.length - i - 1];
    }
    return reverse;
  }
}
