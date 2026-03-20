package com.ak.rsm2;

import com.ak.util.Metrics;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.byLessThan;

class MisfitTest {
  @ParameterizedTest
  @CsvSource(delimiter = ',', textBlock = """
      10.0, 30.0, 30.971, 31.278, -0.05, 5.0
      50.0, 30.0, 62.479, 61.860,  0.05, 5.0
      """)
  void water(double smm, double lmm, double rBefore, double rAfter, double dHmm, double expectedHmm) {
    Misfit misfit = Misfit.builder()
        .ofMilli(s -> s.tetrapolar(smm, lmm).absError(0.1))
        .measurements(m -> m.ohms(rBefore).dhMilli(dHmm).thenOhms(rAfter))
        .build();

    assertThat(misfit.errorLog().applyAsDouble(new Model.Layer2Relative(K.PLUS_ONE, Metrics.Length.MILLI.toSI(expectedHmm))))
        .as(misfit::toString)
        .isPositive().isCloseTo(0.0, byLessThan(0.01));
  }

  @ParameterizedTest
  @CsvSource(delimiter = ',', textBlock = """
      30.971, 31.278, 0.05, 5.0
      """)
  void invalidDiff(double rBefore, double rAfter, double dHmm, double expectedHmm) {
    Misfit misfit = Misfit.builder()
        .ofMilli(s -> s.tetrapolar(10.0, 30.0).absError(0.1))
        .measurements(m -> m.ohms(rBefore).dhMilli(dHmm).thenOhms(rAfter))
        .build();
    assertThat(misfit.errorLog().applyAsDouble(new Model.Layer2Relative(K.PLUS_ONE, Metrics.Length.MILLI.toSI(expectedHmm))))
        .isInfinite();
  }
}