package com.ak.rsm2;

import com.ak.util.Metrics;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.function.ToDoubleFunction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.byLessThan;

class Inverse2Test {
  @ParameterizedTest
  @CsvSource(delimiter = ',', textBlock = """
      30.971, 31.278, -0.05, 5.0
      """)
  void water(double rBefore, double rAfter, double dHmm, double expectedHmm) {
    ElectrodeSystem.Inexact inexact = ElectrodeSystem.ofMilli().tetrapolar(10.0, 30.0).absError(0.1).build();
    double dh = Metrics.Length.MILLI.toSI(dHmm);
    ToDoubleFunction<Model.Layer2Relative> errorLog = inexact.errorLog(rBefore, (rAfter - rBefore) / dh);
    assertThat(errorLog.applyAsDouble(new Model.Layer2Relative(K.PLUS_ONE, Metrics.Length.MILLI.toSI(expectedHmm))))
        .isPositive().isCloseTo(0.0, byLessThan(0.01));
  }

  @ParameterizedTest
  @CsvSource(delimiter = ',', textBlock = """
      30.971, 31.278, 0.05, 5.0
      """)
  void invalidDiff(double rBefore, double rAfter, double dHmm, double expectedHmm) {
    ElectrodeSystem.Inexact inexact = ElectrodeSystem.ofMilli().tetrapolar(10.0, 30.0).absError(0.1).build();
    double dh = Metrics.Length.MILLI.toSI(dHmm);
    ToDoubleFunction<Model.Layer2Relative> errorLog = inexact.errorLog(rBefore, (rAfter - rBefore) / dh);
    assertThat(errorLog.applyAsDouble(new Model.Layer2Relative(K.PLUS_ONE, Metrics.Length.MILLI.toSI(expectedHmm))))
        .isInfinite();
  }
}
