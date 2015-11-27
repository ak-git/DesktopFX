package com.ak.rsm;

import java.math.BigDecimal;
import java.math.MathContext;
import java.nio.file.Paths;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

import com.ak.util.LineFileCollector;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static java.lang.StrictMath.asin;

public class ElectrodeSizeTest {
  private static final double SQRT_2 = 1.4142135623730951;

  private ElectrodeSizeTest() {
  }

  private static class RelativeErrorR implements UnivariateFunction {
    final double sToL;

    private RelativeErrorR(double sToL) {
      this.sToL = sToL;
    }

    @Override
    public double value(double dToL) {
      if (1.0 - sToL < dToL) {
        dToL = 1.0 - sToL;
      }
      return ((asin(m(dToL)) - asin(p(dToL))) / (m(dToL) - p(dToL))) - 1.0;
    }

    double m(double dToL) {
      return dToL / (1.0 - sToL);
    }

    double p(double dToL) {
      return dToL / (1.0 + sToL);
    }
  }

  @DataProvider(name = "dToL")
  public static Object[][] dl2dRL2rho() {
    Supplier<DoubleStream> xVar = ElectrodeSizeTest::doubleRange;
    xVar.get().mapToObj(value -> String.format("%.3f", value)).collect(
        new LineFileCollector<>(Paths.get("x.txt"), LineFileCollector.Direction.VERTICAL));
    return new Object[][] {{xVar}};
  }

  @Test
  public static void testValue() {
    double sToL = 0.5;
    double dToL = 0.1;
    UnivariateFunction errorR = new RelativeErrorR(sToL);
    Assert.assertEquals(0.01, errorR.value(dToL), 0.001);
    Assert.assertEquals(0.846, errorR.value(0.5), 0.001);
    Assert.assertEquals(0.846, errorR.value(0.9), 0.001);
  }

  @Test(dataProvider = "dToL", enabled = false)
  public static void testErrorsAt(Supplier<DoubleStream> xVar) {
    DoubleStream.of(1.0 / 3.0, SQRT_2 - 1, 0.5, 2.0 / 3.0).
        mapToObj(sToL -> xVar.get().map(dToL -> new RelativeErrorR(sToL).value(dToL))).
        map(stream -> stream.mapToObj(value -> String.format("%.6f", value)).collect(Collectors.joining("\t"))).
        collect(new LineFileCollector<>(Paths.get("ErrorsAtDtoL.txt"), LineFileCollector.Direction.VERTICAL));
  }

  private static DoubleStream doubleRange() {
    double step = 1.0e-2;
    double end = 1.0;
    return DoubleStream.iterate(step, dl2L -> dl2L + step).
        limit(BigDecimal.valueOf(end / step).round(MathContext.UNLIMITED).intValue()).sequential();
  }
}
