package com.ak.rsm2;

import com.ak.math.Simplex;
import com.ak.util.Metrics;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Arrays;
import java.util.function.DoubleUnaryOperator;

import static java.lang.StrictMath.log;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.byLessThan;

class ParametricOperatorTest {
  @ParameterizedTest
  @CsvSource(delimiter = ',', textBlock = """
      10.0, 30.0, METRE, 30.971, 31.278, -0.05, 5.0
      50.0, 30.0, MILLI, 62.479, 61.860,  0.05, 5.0
      """)
  void misfit(double sPU, double lCC, Metrics.Length units, double rBefore, double rAfter, double hDiff, double expectedH) {
    ParametricOperator parametricOperator = ParametricOperator.builder(units)
        .system(s -> s.tetrapolar(sPU, lCC).absError(0.1))
        .measurements(m -> m.ohms(rBefore).thenOhms(rAfter).hDiff(hDiff, units))
        .build();

    Model layer2 = new Model.Layer2Relative(K.PLUS_ONE, units.toSI(expectedH));
    assertThat(parametricOperator.misfit().applyAsDouble(layer2)).as(parametricOperator::toString).isPositive().isCloseTo(0.0, byLessThan(0.01));
  }

  @ParameterizedTest
  @CsvSource(delimiter = ',', textBlock = """
      10.0, 30.0, METRE, 30.971, 31.278, -0.05
      50.0, 30.0, MILLI, 62.479, 61.860,  0.05
      """)
  void regularization(double sPU, double lCC, Metrics.Length units, double rBefore, double rAfter, double hDiff) {
    ParametricOperator parametricOperator = ParametricOperator.builder(units)
        .system(s -> s.tetrapolar(sPU, lCC).absError(0.1))
        .measurements(m -> m.ohms(rBefore).thenOhms(rAfter).hDiff(hDiff, units))
        .build();

    ElectrodeSystem.Inexact inexact = ElectrodeSystem.builder(units).tetrapolar(sPU, lCC).absError(0.1).build();
    Assertions.assertAll(parametricOperator.toString(),
        () -> assertThat(parametricOperator.regularization(ParametricOperator.Regularization.ZERO_MAX_LOG).applyAsDouble(new Model.Layer2Relative(K.PLUS_ONE, inexact.hMax(K.PLUS_ONE)))).isInfinite(),
        () -> assertThat(parametricOperator.regularization(ParametricOperator.Regularization.ZERO_MAX_LOG).applyAsDouble(new Model.Layer2Relative(K.PLUS_ONE, inexact.hMax(K.PLUS_ONE) / 2.0))).isInfinite(),
        () -> assertThat(parametricOperator.regularization(ParametricOperator.Regularization.ZERO_MAX_LOG).applyAsDouble(new Model.Layer2Relative(K.PLUS_ONE, inexact.hMin(K.PLUS_ONE)))).isInfinite()
    );
    K k = K.of(0.5);
    assertThat(parametricOperator.regularization(ParametricOperator.Regularization.ZERO_MAX_LOG).applyAsDouble(new Model.Layer2Relative(k, Math.sqrt(inexact.hMax(k) * inexact.hMin(k)))))
        .isPositive().isCloseTo(0.0, byLessThan(1.0e-9));
  }

  @ParameterizedTest
  @CsvSource(delimiter = ',', textBlock = """
      10.0, 30.0, METRE, 30.971, 31.278, -0.05
      50.0, 30.0, MILLI, 62.479, 61.860,  0.05
      """)
  void dataErrorNorm(double sPU, double lCC, Metrics.Length units, double rBefore, double rAfter, double hDiff) {
    DoubleUnaryOperator d = emm -> {
      ElectrodeSystem.Tetrapolar system = ElectrodeSystem.builder(units).tetrapolar(sPU + emm, lCC - emm).build();
      Resistivity resistivity = Resistivity.of(system).build();
      TetrapolarMeasurement.TetrapolarDiffMeasurement measurement = TetrapolarMeasurement.builder().ohms(rBefore).thenOhms(rAfter).hDiff(hDiff, units).build();
      double apparent = resistivity.apparent(measurement.ohms());
      double derivativeApparentByPhi = Math.abs(resistivity.apparent((measurement.ohmsDiff() / measurement.hDiff()) / system.phiFactor()));
      return log(apparent) - log(derivativeApparentByPhi);
    };

    double expected = Math.max(d.applyAsDouble(0.1), d.applyAsDouble(-0.1)) - d.applyAsDouble(0.0);

    ParametricOperator parametricOperator = ParametricOperator.builder(Metrics.Length.MILLI)
        .system(s -> s.tetrapolar(sPU, lCC).absError(0.1))
        .measurements(m -> m.ohms(rBefore).thenOhms(rAfter).hDiff(hDiff, units))
        .build();
    assertThat(parametricOperator.dataErrorNorm()).as(parametricOperator::toString).isCloseTo(expected, byLessThan(0.001));
  }

  @ParameterizedTest
  @CsvSource(delimiter = ',', textBlock = """
      10.0, 30.0, METRE, 35.589
      50.0, 30.0, MILLI, 60.8
      """)
  void bounds(double sPU, double lCC, Metrics.Length units, double expectedH) {
    ParametricOperator parametricOperator = ParametricOperator.builder(units)
        .system(s -> s.tetrapolar(sPU, lCC).absError(0.1))
        .measurements(m -> m.ohms(0.0).thenOhms(0.0).hDiff(0.0, units))
        .build();
    Assertions.assertAll(Arrays.toString(parametricOperator.bounds()),
        () -> assertThat(parametricOperator.bounds()).hasSize(2),
        () -> assertThat(parametricOperator.bounds()[0]).isEqualTo(new Simplex.Bounds(-1.0, Double.NaN, 1.0)),
        () -> assertThat(parametricOperator.bounds()[1].max()).isCloseTo(units.toSI(expectedH), byLessThan(0.001))
    );
  }

  @ParameterizedTest
  @CsvSource(delimiter = ',', textBlock = """
      30.971, 31.278, METRE, 0.05, 5.0
      30.971, 31.278, MILLI, 0.05, 5.0
      """)
  void invalidDiff(double rBefore, double rAfter, Metrics.Length units, double hDiff, double expectedH) {
    ParametricOperator parametricOperator = ParametricOperator.builder(units)
        .system(s -> s.tetrapolar(10.0, 30.0).absError(0.1))
        .measurements(m -> m.ohms(rBefore).thenOhms(rAfter).hDiff(hDiff, units))
        .build();
    assertThat(parametricOperator.misfit().applyAsDouble(new Model.Layer2Relative(K.PLUS_ONE, units.toSI(expectedH))))
        .as(parametricOperator::toString).isInfinite();
  }
}