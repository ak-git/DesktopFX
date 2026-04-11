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
      10.0, 30.0, METRE, 30.971, 31.278, -0.05, 5.0
      50.0, 30.0, MILLI, 62.479, 61.860,  0.05, 5.0
      """)
  void misfit(double sPU, double lCC, Metrics.Length units, double rBefore, double rAfter, double dH, double expectedHmm) {
    Misfit misfit = Misfit.builder(units)
        .system(s -> s.tetrapolar(sPU, lCC).absError(0.1))
        .measurements(m -> m.ohms(rBefore).dh(dH).thenOhms(rAfter))
        .build();

    Model.Layer2Relative layer2 = new Model.Layer2Relative(K.PLUS_ONE, units.toSI(expectedHmm));
    assertThat(misfit.misfit().applyAsDouble(layer2)).as(misfit::toString).isPositive().isCloseTo(0.0, byLessThan(0.01));
  }

  @ParameterizedTest
  @CsvSource(delimiter = ',', textBlock = """
      10.0, 30.0, METRE, 30.971, 31.278, -0.05
      50.0, 30.0, MILLI, 62.479, 61.860,  0.05
      """)
  void regularization(double sPU, double lCC, Metrics.Length units, double rBefore, double rAfter, double dH) {
    Misfit misfit = Misfit.builder(units)
        .system(s -> s.tetrapolar(sPU, lCC).absError(0.1))
        .measurements(m -> m.ohms(rBefore).dh(dH).thenOhms(rAfter))
        .build();

    ElectrodeSystem.Inexact inexact = ElectrodeSystem.builder(units).tetrapolar(sPU, lCC).absError(0.1).build();
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
      10.0, 30.0, METRE, 30.971, 31.278, -0.05
      50.0, 30.0, MILLI, 62.479, 61.860,  0.05
      """)
  void dataErrorNorm(double sPU, double lCC, Metrics.Length units, double rBefore, double rAfter, double dH) {
    DoubleUnaryOperator d = emm -> {
      ElectrodeSystem.Tetrapolar system = ElectrodeSystem.builder(units).tetrapolar(sPU + emm, lCC - emm).build();
      Resistivity resistivity = Resistivity.of(system);
      TetrapolarMeasurement measurement = TetrapolarMeasurement.builder(units).ohms(rBefore).dh(dH).thenOhms(rAfter).build();
      double apparent = resistivity.apparent(measurement.ohms());
      double derivativeApparentByPhi = Math.abs(resistivity.apparent((measurement.dOhms() / measurement.dh()) / system.phiFactor()));
      return log(apparent) - log(derivativeApparentByPhi);
    };

    double expected = Math.max(d.applyAsDouble(0.1), d.applyAsDouble(-0.1)) - d.applyAsDouble(0.0);

    Misfit misfit = Misfit.builder(Metrics.Length.MILLI)
        .system(s -> s.tetrapolar(sPU, lCC).absError(0.1))
        .measurements(m -> m.ohms(rBefore).dh(dH).thenOhms(rAfter))
        .build();
    assertThat(misfit.dataErrorNorm()).as(misfit::toString).isCloseTo(expected, byLessThan(0.001));
  }

  @ParameterizedTest
  @CsvSource(delimiter = ',', textBlock = """
      30.971, 31.278, METRE, 0.05, 5.0
      30.971, 31.278, MILLI, 0.05, 5.0
      """)
  void invalidDiff(double rBefore, double rAfter, Metrics.Length units, double dH, double expectedH) {
    Misfit misfit = Misfit.builder(units)
        .system(s -> s.tetrapolar(10.0, 30.0).absError(0.1))
        .measurements(m -> m.ohms(rBefore).dh(dH).thenOhms(rAfter))
        .build();
    assertThat(misfit.misfit().applyAsDouble(new Model.Layer2Relative(K.PLUS_ONE, units.toSI(expectedH))))
        .as(misfit::toString).isInfinite();
  }
}