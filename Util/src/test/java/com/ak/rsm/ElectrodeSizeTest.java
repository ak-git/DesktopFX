package com.ak.rsm;

import java.util.stream.DoubleStream;

import com.ak.util.CSVLineFileBuilder;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.testng.Assert;
import org.testng.annotations.Test;

import static java.lang.StrictMath.asin;

public class ElectrodeSizeTest {
  private static final double SQRT_2 = 1.4142135623730951;

  private record RelativeErrorR(double sToL) implements UnivariateFunction {

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
  public void testValue() {
    double sToL = 0.5;
    double dToL = 0.1;
    UnivariateFunction errorR = new RelativeErrorR(sToL);
    Assert.assertEquals(errorR.value(dToL), 0.01, 0.001);
    Assert.assertEquals(errorR.value(0.5), 0.846, 0.001);
    Assert.assertEquals(errorR.value(0.9), 0.846, 0.001);
  }

  @Test(enabled = false)
  public void testErrorsAt() {
    CSVLineFileBuilder.of((dToL, sToL) -> new RelativeErrorR(sToL).value(dToL))
        .xRange(1.0e-2, 1.0, 1.0e-2)
        .yStream(() -> DoubleStream.of(1.0 / 3.0, SQRT_2 - 1, 0.5, 2.0 / 3.0))
        .saveTo("ErrorsAtDtoL", aDouble -> aDouble)
        .generate();
  }
}
