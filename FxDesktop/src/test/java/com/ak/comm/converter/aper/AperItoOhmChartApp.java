package com.ak.comm.converter.aper;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.IntSummaryStatistics;
import java.util.function.Function;
import java.util.function.IntBinaryOperator;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.Nonnull;

import com.ak.comm.converter.ADCVariable;
import com.ak.comm.converter.AbstractSplineCoefficientsChartApp;
import com.ak.numbers.Coefficients;
import com.ak.numbers.CoefficientsUtils;
import com.ak.numbers.Interpolators;
import com.ak.numbers.aper.AperCoefficients;
import com.ak.numbers.aper.AperSurfaceCoefficients;
import com.ak.util.LineFileCollector;
import com.ak.util.Strings;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public final class AperItoOhmChartApp extends AbstractSplineCoefficientsChartApp {
  public AperItoOhmChartApp() {
    super(AperCoefficients.I_ADC_TO_OHM, ADCVariable.ADC, AperInVariable.RI1);
  }

  public static void main(String[] args) {
    launch(args);
  }

  @DataProvider(name = "x = ADC, y = R(I-I)")
  public static Object[][] adcAndR() throws IOException {
    Supplier<IntStream> xVarADC = () -> intRange(AperSurfaceCoefficients.class, CoefficientsUtils::rangeX);
    Assert.assertNull(xVarADC.get().mapToObj(value -> String.format("%d", value)).collect(
        new LineFileCollector(Paths.get("x.txt"), LineFileCollector.Direction.HORIZONTAL)));

    Supplier<IntStream> yVarR = () -> intRange(AperSurfaceCoefficients.class, CoefficientsUtils::rangeY);
    Assert.assertNull(yVarR.get().mapToObj(value -> String.format("%d", value)).collect(
        new LineFileCollector(Paths.get("y.txt"), LineFileCollector.Direction.VERTICAL)));

    return new Object[][] {{xVarADC, yVarR}};
  }

  @Test(dataProvider = "x = ADC, y = R(I-I)", enabled = false)
  public static void testSplineSurface(@Nonnull Supplier<IntStream> xVar, @Nonnull Supplier<IntStream> yVar) throws IOException {
    IntBinaryOperator function = Interpolators.interpolator(AperSurfaceCoefficients.class).get();
    Assert.assertNull(yVar.get().mapToObj(y -> xVar.get().map(x -> function.applyAsInt(x, y))).
        map(stream -> stream.mapToObj(value -> String.format("%d", value)).collect(Collectors.joining(Strings.TAB))).
        collect(new LineFileCollector(Paths.get("out.txt"), LineFileCollector.Direction.VERTICAL)));
  }

  private static <C extends Enum<C> & Coefficients> IntStream intRange(@Nonnull Class<C> coeffClass,
                                                                       @Nonnull Function<Class<C>, IntSummaryStatistics> selector) {
    int countValues = 100;
    IntSummaryStatistics statistics = selector.apply(coeffClass);
    int step = Math.max(1, (statistics.getMax() - statistics.getMin()) / countValues);
    return IntStream.rangeClosed(0, countValues).map(i -> statistics.getMin() + i * step);
  }

}
