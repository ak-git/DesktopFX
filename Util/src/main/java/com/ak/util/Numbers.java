package com.ak.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.DoubleUnaryOperator;
import java.util.stream.DoubleStream;

import javax.annotation.Nonnegative;

import static java.lang.StrictMath.exp;
import static java.lang.StrictMath.log;
import static java.lang.StrictMath.log10;

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
}
