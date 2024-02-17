package com.ak.numbers;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.IntSummaryStatistics;
import java.util.Objects;
import java.util.function.ToDoubleFunction;

public enum RangeUtils {
  ;

  public static <C extends Enum<C> & Coefficients> IntSummaryStatistics rangeX(Class<C> coeffClass) {
    return range(coeffClass, value -> value[0]);
  }

  public static <C extends Enum<C> & Coefficients> IntSummaryStatistics rangeY(Class<C> coeffClass) {
    return range(coeffClass, value -> value[1]);
  }

  private static <C extends Enum<C> & Coefficients> IntSummaryStatistics range(Class<C> coeffClass,
                                                                               ToDoubleFunction<double[]> selector) {
    return EnumSet.allOf(Objects.requireNonNull(coeffClass)).stream().flatMapToDouble(
            coefficients -> Arrays.stream(coefficients.getPairs()).mapToDouble(Objects.requireNonNull(selector))).
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
