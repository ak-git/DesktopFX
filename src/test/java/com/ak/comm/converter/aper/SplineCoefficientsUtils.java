package com.ak.comm.converter.aper;

import java.util.IntSummaryStatistics;
import java.util.function.Function;
import java.util.function.IntBinaryOperator;
import java.util.stream.IntStream;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.numbers.Coefficients;
import com.ak.numbers.Interpolators;
import com.ak.numbers.RangeUtils;
import com.ak.util.CSVLineFileBuilder;

public enum SplineCoefficientsUtils {
  ;

  public static <C extends Enum<C> & Coefficients> void testSplineSurface(@Nonnull Class<C> surfaceCoeffClass) {
    IntBinaryOperator function = Interpolators.interpolator(surfaceCoeffClass).get();
    CSVLineFileBuilder.of((adc, rII) -> function.applyAsInt(adc.intValue(), rII.intValue()))
        .xStream(() -> intRange(surfaceCoeffClass, RangeUtils::rangeX).asDoubleStream())
        .yStream(() -> intRange(surfaceCoeffClass, RangeUtils::rangeY).asDoubleStream())
        .saveTo(surfaceCoeffClass.getName(), integer -> integer)
        .generate();
  }

  @ParametersAreNonnullByDefault
  private static <C extends Enum<C> & Coefficients> IntStream intRange(Class<C> coeffClass,
                                                                       Function<Class<C>, IntSummaryStatistics> selector) {
    int countValues = 100;
    IntSummaryStatistics statistics = selector.apply(coeffClass);
    int step = Math.max(1, (statistics.getMax() - statistics.getMin()) / countValues);
    return IntStream.rangeClosed(0, countValues).map(i -> statistics.getMin() + i * step);
  }
}