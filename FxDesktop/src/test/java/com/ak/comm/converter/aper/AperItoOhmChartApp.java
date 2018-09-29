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

import com.ak.comm.converter.ADCVariable;
import com.ak.comm.converter.AbstractSplineCoefficientsChartApp;
import com.ak.comm.converter.aper.sincos.AperOutVariable;
import com.ak.numbers.Coefficients;
import com.ak.numbers.CoefficientsUtils;
import com.ak.numbers.Interpolators;
import com.ak.numbers.aper.sincos.AperCoefficients;
import com.ak.numbers.aper.sincos.AperSurfaceCoefficientsChannel1;
import com.ak.numbers.aper.sincos.AperSurfaceCoefficientsChannel2;
import com.ak.util.LineFileBuilder;
import com.ak.util.LineFileCollector;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * x = ADC, y = R(I-I)
 */
public final class AperItoOhmChartApp extends AbstractSplineCoefficientsChartApp<ADCVariable, AperOutVariable> {
  public AperItoOhmChartApp() {
    super(AperCoefficients.ADC_TO_OHM_1, ADCVariable.ADC, AperOutVariable.CCR1);
  }

  public static void main(String[] args) {
    launch(args);
  }

  @Test(enabled = false)
  public static void testSplineSurface1() throws IOException {
    IntBinaryOperator function = Interpolators.interpolator(AperSurfaceCoefficientsChannel1.class).get();
    LineFileBuilder.of("%.0f %.0f %.0f").
        xStream(() -> intRange(AperSurfaceCoefficientsChannel1.class, CoefficientsUtils::rangeX).asDoubleStream()).
        yStream(() -> intRange(AperSurfaceCoefficientsChannel1.class, CoefficientsUtils::rangeY).asDoubleStream()).
        generate("z.txt", (adc, rII) -> function.applyAsInt(Double.valueOf(adc).intValue(), Double.valueOf(rII).intValue()));

    Supplier<DoubleStream> xVar = () -> intRange(AperSurfaceCoefficientsChannel1.class, CoefficientsUtils::rangeX).asDoubleStream();
    Assert.assertTrue(xVar.get().mapToObj(sToL -> String.format("%.2f", sToL)).collect(
        new LineFileCollector(Paths.get("x.txt"), LineFileCollector.Direction.HORIZONTAL)));

    Supplier<DoubleStream> yVar = () -> intRange(AperSurfaceCoefficientsChannel1.class, CoefficientsUtils::rangeY).asDoubleStream();
    Assert.assertTrue(yVar.get().mapToObj(sToL -> String.format("%.2f", sToL)).collect(
        new LineFileCollector(Paths.get("y.txt"), LineFileCollector.Direction.VERTICAL)));
  }

  @Test(enabled = false)
  public static void testSplineSurface2() throws IOException {
    IntBinaryOperator function = Interpolators.interpolator(AperSurfaceCoefficientsChannel2.class).get();
    LineFileBuilder.of("%.0f %.0f %.0f").
        xStream(() -> intRange(AperSurfaceCoefficientsChannel2.class, CoefficientsUtils::rangeX).asDoubleStream()).
        yStream(() -> intRange(AperSurfaceCoefficientsChannel2.class, CoefficientsUtils::rangeY).asDoubleStream()).
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
