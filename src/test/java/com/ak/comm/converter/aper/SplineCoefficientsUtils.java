package com.ak.comm.converter.aper;

import java.nio.file.Paths;
import java.util.IntSummaryStatistics;
import java.util.function.Function;
import java.util.function.IntBinaryOperator;
import java.util.function.Supplier;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import javax.annotation.Nonnull;

import com.ak.numbers.Coefficients;
import com.ak.numbers.Interpolators;
import com.ak.numbers.RangeUtils;
import com.ak.numbers.aper.AperSurfaceCoefficientsChannel2;
import com.ak.util.CSVLineFileBuilder;
import com.ak.util.CSVLineFileCollector;
import org.testng.Assert;

public enum SplineCoefficientsUtils {
  ;

  public static <C extends Enum<C> & Coefficients> void testSplineSurface1(Class<C> surfaceCoeffClass) {
    IntBinaryOperator function = Interpolators.interpolator(surfaceCoeffClass).get();
    CSVLineFileBuilder.of((adc, rII) -> function.applyAsInt(adc.intValue(), rII.intValue()))
        .xStream(() -> intRange(surfaceCoeffClass, RangeUtils::rangeX).asDoubleStream())
        .yStream(() -> intRange(surfaceCoeffClass, RangeUtils::rangeY).asDoubleStream())
        .saveTo("z", integer -> integer)
        .generate();

    Supplier<DoubleStream> xVar = () -> intRange(surfaceCoeffClass, RangeUtils::rangeX).asDoubleStream();
    Assert.assertTrue(xVar.get().mapToObj("%.2f"::formatted).collect(
        new CSVLineFileCollector(Paths.get("x-CC-R"))));

    Supplier<DoubleStream> yVar = () -> intRange(surfaceCoeffClass, RangeUtils::rangeY).asDoubleStream();
    Assert.assertTrue(yVar.get().mapToObj("%.2f"::formatted).collect(
        new CSVLineFileCollector(Paths.get("y-ADC-R"))));
  }

  public static <C extends Enum<C> & Coefficients> void testSplineSurface2(Class<C> surfaceCoeffClass) {
    IntBinaryOperator function = Interpolators.interpolator(surfaceCoeffClass).get();
    CSVLineFileBuilder.of((adc, rII) -> function.applyAsInt(adc.intValue(), rII.intValue()))
        .xStream(() -> intRange(AperSurfaceCoefficientsChannel2.class, RangeUtils::rangeX).asDoubleStream())
        .yStream(() -> intRange(AperSurfaceCoefficientsChannel2.class, RangeUtils::rangeY).asDoubleStream())
        .saveTo("z", integer -> integer)
        .generate();
  }

  private static <C extends Enum<C> & Coefficients> IntStream intRange(@Nonnull Class<C> coeffClass,
                                                                       @Nonnull Function<Class<C>, IntSummaryStatistics> selector) {
    int countValues = 100;
    IntSummaryStatistics statistics = selector.apply(coeffClass);
    int step = Math.max(1, (statistics.getMax() - statistics.getMin()) / countValues);
    return IntStream.rangeClosed(0, countValues).map(i -> statistics.getMin() + i * step);
  }
}