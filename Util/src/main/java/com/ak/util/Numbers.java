package com.ak.util;

import javax.annotation.Nonnegative;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.DoubleUnaryOperator;
import java.util.stream.DoubleStream;

import static java.lang.StrictMath.*;

public enum Numbers {
  ;

  public static DoubleStream rangeLog(@Nonnegative double start, @Nonnegative double end, @Nonnegative long len) {
    double from = log(Math.min(start, end));
    double to = log(Math.max(start, end));
    double step = (to - from) / (len - 1);
    return DoubleStream
        .concat(
            DoubleStream.iterate(from + step, x -> x + step).limit(len - 2).map(StrictMath::exp),
            DoubleStream.of(start, end)
        )
        .sorted()
        .map(x -> round((x - exp(log(x) - step)) / 5.0).applyAsDouble(x));
  }

  public static DoubleUnaryOperator round(@Nonnegative double step) {
    int afterZero = (int) -Math.floor(log10(step));
    return x -> BigDecimal.valueOf(x).setScale(afterZero, RoundingMode.HALF_EVEN).doubleValue();
  }

  public static int toInt(double value) {
    return Math.toIntExact(Math.round(value));
  }

  public static int log10ToInt(@Nonnegative double value) {
    return toInt(log10(value));
  }
}
