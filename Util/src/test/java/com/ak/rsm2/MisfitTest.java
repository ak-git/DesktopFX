package com.ak.rsm2;

import com.ak.util.Metrics;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.function.DoubleUnaryOperator;

import static java.lang.StrictMath.log;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.byLessThan;

class MisfitTest {
  @ParameterizedTest
  @CsvSource(delimiter = ',', textBlock = """
      10.0, 30.0, 30.971, 31.278, -0.05, 5.0
      50.0, 30.0, 62.479, 61.860,  0.05, 5.0
      """)
  void misfit(double smm, double lmm, double rBefore, double rAfter, double dHmm, double expectedHmm) {
    Misfit misfit = Misfit.builder()
        .ofMilli(s -> s.tetrapolar(smm, lmm).absError(0.1))
        .measurements(m -> m.ohms(rBefore).dhMilli(dHmm).thenOhms(rAfter))
        .build();

    Model.Layer2Relative layer2 = new Model.Layer2Relative(K.PLUS_ONE, Metrics.Length.MILLI.toSI(expectedHmm));
    assertThat(misfit.misfit().applyAsDouble(layer2)).as(misfit::toString).isPositive().isCloseTo(0.0, byLessThan(0.01));
  }

  @ParameterizedTest
  @CsvSource(delimiter = ',', textBlock = """
      10.0, 30.0, 30.971, 31.278, -0.05
      50.0, 30.0, 62.479, 61.860,  0.05
      """)
  void regularization(double smm, double lmm, double rBefore, double rAfter, double dHmm) {
    Misfit misfit = Misfit.builder()
        .ofMilli(s -> s.tetrapolar(smm, lmm).absError(0.1))
        .measurements(m -> m.ohms(rBefore).dhMilli(dHmm).thenOhms(rAfter))
        .build();

    ElectrodeSystem.Inexact inexact = ElectrodeSystem.ofMilli().tetrapolar(smm, lmm).absError(0.1).build();
    Assertions.assertAll(misfit.toString(),
        () -> assertThat(misfit.regularization(Misfit.Regularization.ZERO_MAX_LOG).applyAsDouble(new Model.Layer2Relative(K.PLUS_ONE, inexact.hMax(K.PLUS_ONE)))).isInfinite(),
        () -> assertThat(misfit.regularization(Misfit.Regularization.ZERO_MAX_LOG).applyAsDouble(new Model.Layer2Relative(K.PLUS_ONE, inexact.hMax(K.PLUS_ONE) / 2.0))).isInfinite(),
        () -> assertThat(misfit.regularization(Misfit.Regularization.ZERO_MAX_LOG).applyAsDouble(new Model.Layer2Relative(K.PLUS_ONE, inexact.hMin(K.PLUS_ONE)))).isInfinite()
    );
    K k = K.of(0.5);
    assertThat(misfit.regularization(Misfit.Regularization.ZERO_MAX_LOG).applyAsDouble(new Model.Layer2Relative(k, Math.sqrt(inexact.hMax(k) * inexact.hMin(k)))))
        .isPositive().isCloseTo(0.0, byLessThan(1.0e-9));
  }

  @ParameterizedTest
  @CsvSource(delimiter = ',', textBlock = """
      10.0, 30.0, 30.971, 31.278, -0.05
      50.0, 30.0, 62.479, 61.860,  0.05
      """)
  void dataErrorNorm(double smm, double lmm, double rBefore, double rAfter, double dHmm) {
    DoubleUnaryOperator d = emm -> {
      ElectrodeSystem.Tetrapolar system = ElectrodeSystem.ofMilli().tetrapolar(smm + emm, lmm - emm).build();
      Resistivity resistivity = Resistivity.of(system);
      TetrapolarMeasurement measurement = TetrapolarMeasurement.builder().ohms(rBefore).dhMilli(dHmm).thenOhms(rAfter).build();
      double apparent = resistivity.apparent(measurement.ohms());
      double derivativeApparentByPhi = Math.abs(resistivity.apparent((measurement.dOhms() / measurement.dh()) / system.phiFactor()));
      return log(apparent) - log(derivativeApparentByPhi);
    };

    double expected = Math.max(d.applyAsDouble(0.1), d.applyAsDouble(-0.1)) - d.applyAsDouble(0.0);

    Misfit misfit = Misfit.builder()
        .ofMilli(s -> s.tetrapolar(smm, lmm).absError(0.1))
        .measurements(m -> m.ohms(rBefore).dhMilli(dHmm).thenOhms(rAfter))
        .build();
    assertThat(misfit.dataErrorNorm()).as(misfit::toString).isCloseTo(expected, byLessThan(0.001));
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
    assertThat(misfit.misfit().applyAsDouble(new Model.Layer2Relative(K.PLUS_ONE, Metrics.Length.MILLI.toSI(expectedHmm))))
        .as(misfit::toString).isInfinite();
  }
}