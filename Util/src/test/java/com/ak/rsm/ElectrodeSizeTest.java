package com.ak.rsm;

import java.io.IOException;
import java.util.stream.DoubleStream;

import com.ak.util.LineFileBuilder;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.testng.Assert;
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

  @Test
  public static void testValue() {
    double sToL = 0.5;
    double dToL = 0.1;
    UnivariateFunction errorR = new RelativeErrorR(sToL);
    Assert.assertEquals(0.01, errorR.value(dToL), 0.001);
    Assert.assertEquals(0.846, errorR.value(0.5), 0.001);
    Assert.assertEquals(0.846, errorR.value(0.9), 0.001);
  }

  @Test(enabled = false)
  public static void testErrorsAt() throws IOException {
    LineFileBuilder.of("%.3f %.3f %.6f").
        xRange(1.0e-2, 1.0, 1.0e-2).
        yStream(() -> DoubleStream.of(1.0 / 3.0, SQRT_2 - 1, 0.5, 2.0 / 3.0)).
        generate("ErrorsAtDtoL.txt", (dToL, sToL) -> new RelativeErrorR(sToL).value(dToL));
  }
}
