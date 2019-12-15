package com.ak.comm.converter.aper;

import java.io.IOException;
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
import com.ak.util.LineFileBuilder;
import com.ak.util.LineFileCollector;
import org.testng.Assert;

public class SplineCoefficientsTest {
  private SplineCoefficientsTest() {
  }

  public static <C extends Enum<C> & Coefficients> void testSplineSurface1(Class<C> surfaceCoeffClass) throws IOException {
    IntBinaryOperator function = Interpolators.interpolator(surfaceCoeffClass).get();
    LineFileBuilder.of("%.0f %.0f %.0f").
        xStream(() -> intRange(surfaceCoeffClass, RangeUtils::rangeX).asDoubleStream()).
        yStream(() -> intRange(surfaceCoeffClass, RangeUtils::rangeY).asDoubleStream()).
        generate("z.txt", (adc, rII) -> function.applyAsInt(Double.valueOf(adc).intValue(), Double.valueOf(rII).intValue()));

    Supplier<DoubleStream> xVar = () -> intRange(surfaceCoeffClass, RangeUtils::rangeX).asDoubleStream();
    Assert.assertTrue(xVar.get().mapToObj(sToL -> String.format("%.2f", sToL)).collect(
        new LineFileCollector(Paths.get("x-CC-R.txt"), LineFileCollector.Direction.HORIZONTAL)));

    Supplier<DoubleStream> yVar = () -> intRange(surfaceCoeffClass, RangeUtils::rangeY).asDoubleStream();
    Assert.assertTrue(yVar.get().mapToObj(sToL -> String.format("%.2f", sToL)).collect(
        new LineFileCollector(Paths.get("y-ADC-R.txt"), LineFileCollector.Direction.VERTICAL)));
  }

  public static <C extends Enum<C> & Coefficients> void testSplineSurface2(Class<C> surfaceCoeffClass) throws IOException {
    IntBinaryOperator function = Interpolators.interpolator(surfaceCoeffClass).get();
    LineFileBuilder.of("%.0f %.0f %.0f").
        xStream(() -> intRange(AperSurfaceCoefficientsChannel2.class, RangeUtils::rangeX).asDoubleStream()).
        yStream(() -> intRange(AperSurfaceCoefficientsChannel2.class, RangeUtils::rangeY).asDoubleStream()).
        generate("z.txt", (adc, rII) -> function.applyAsInt(Double.valueOf(adc).intValue(), Double.valueOf(rII).intValue()));
  }

  private static <C extends Enum<C> & Coefficients> IntStream intRange(@Nonnull Class<C> coeffClass,
                                                                       @Nonnull Function<Class<C>, IntSummaryStatistics> selector) {
    int countValues = 100;
    IntSummaryStatistics statistics = selector.apply(coeffClass);
    int step = Math.max(1, (statistics.getMax() - statistics.getMin()) / countValues);
    return IntStream.rangeClosed(0, countValues).map(i -> statistics.getMin() + i * step);
  }
}