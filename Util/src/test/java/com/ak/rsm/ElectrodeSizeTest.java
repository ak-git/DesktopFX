package com.ak.rsm;

import java.util.stream.DoubleStream;

import com.ak.util.CSVLineFileBuilder;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static java.lang.StrictMath.asin;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.byLessThan;

class ElectrodeSizeTest {
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
  void testValue() {
    double sToL = 0.5;
    double dToL = 0.1;
    UnivariateFunction errorR = new RelativeErrorR(sToL);
    assertThat(errorR.value(dToL)).isCloseTo(0.01, byLessThan(0.001));
    assertThat(errorR.value(0.5)).isCloseTo(0.846, byLessThan(0.001));
    assertThat(errorR.value(0.9)).isCloseTo(0.846, byLessThan(0.001));
  }

  @Test
  @Disabled("generate ErrorsAtDtoL.csv")
  void testErrorsAt() {
    Assertions.assertDoesNotThrow(() -> CSVLineFileBuilder.of((dToL, sToL) -> new RelativeErrorR(sToL).value(dToL))
        .xRange(1.0e-2, 1.0, 1.0e-2)
        .yStream(() -> DoubleStream.of(1.0 / 3.0, SQRT_2 - 1, 0.5, 2.0 / 3.0))
        .saveTo("ErrorsAtDtoL", aDouble -> aDouble)
        .generate());
  }
}

