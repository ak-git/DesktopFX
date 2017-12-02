package com.ak.comm.converter.aper;

import java.io.IOException;
import java.util.IntSummaryStatistics;
import java.util.function.Function;
import java.util.function.IntBinaryOperator;
import java.util.stream.IntStream;

import javax.annotation.Nonnull;

import com.ak.comm.converter.ADCVariable;
import com.ak.comm.converter.AbstractSplineCoefficientsChartApp;
import com.ak.numbers.Coefficients;
import com.ak.numbers.CoefficientsUtils;
import com.ak.numbers.Interpolators;
import com.ak.numbers.aper.AperCoefficients;
import com.ak.numbers.aper.AperSurfaceCoefficients;
import com.ak.util.LineFileBuilder;
import org.testng.annotations.Test;

/**
 * x = ADC, y = R(I-I)
 */
public final class AperItoOhmChartApp extends AbstractSplineCoefficientsChartApp<ADCVariable, AperInVariable> {
  public AperItoOhmChartApp() {
    super(AperCoefficients.ADC_TO_OHM_1, ADCVariable.ADC, AperInVariable.RI1);
  }

  public static void main(String[] args) {
    launch(args);
  }

  @Test(enabled = false)
  public static void testSplineSurface() throws IOException {
    IntBinaryOperator function = Interpolators.interpolator(AperSurfaceCoefficients.class).get();
    LineFileBuilder.of("%.0f %.0f %.0f").
        xStream(() -> intRange(AperSurfaceCoefficients.class, CoefficientsUtils::rangeX).asDoubleStream()).
        yStream(() -> intRange(AperSurfaceCoefficients.class, CoefficientsUtils::rangeY).asDoubleStream()).
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
