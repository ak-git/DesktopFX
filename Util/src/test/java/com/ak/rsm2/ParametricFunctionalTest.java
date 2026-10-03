package com.ak.rsm2;

import com.ak.util.Metrics;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.function.DoubleUnaryOperator;
import java.util.function.ToDoubleFunction;

import static java.lang.StrictMath.log;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.byLessThan;

class ParametricFunctionalTest {

  @Nested
  class DiffTest {

    @ParameterizedTest
    @CsvSource(delimiter = ',', textBlock = """
        10.0, 30.0, METRE, 30.971, 31.278, -0.05
        50.0, 30.0, MILLI, 62.479, 61.860,  0.05
        """)
    void dataErrorNorm(double sPU, double lCC, Metrics.Length units, double rBefore, double rAfter, double hDiff) {
      DoubleUnaryOperator d = emm -> {
        ElectrodeSystem.Tetrapolar system = ElectrodeSystem.builder(units).tetrapolar(sPU + emm, lCC - emm).build();
        Resistivity.Step1 resistivity = Resistivity.of(system);
        TetrapolarMeasurement.Diff measurement = TetrapolarMeasurement.builder().ohms(rBefore).thenOhms(rAfter).hDiff(hDiff, units).build();
        double apparent = resistivity.apparent(measurement.ohms());
        double derivativeApparentByPhi = Math.abs(resistivity.apparent((measurement.ohmsDiff() / measurement.hDiff()) / system.phiFactor()));
        return log(apparent) - log(derivativeApparentByPhi);
      };

      double expected = Math.abs(d.applyAsDouble(0.0) - Math.max(d.applyAsDouble(0.1), d.applyAsDouble(-0.1)));

      ParametricFunctional parametricFunctional = ParametricFunctional.builder(units)
          .system(s -> s.tetrapolar(sPU, lCC).absError(0.1))
          .measurements(m -> m.ohms(rBefore).thenOhms(rAfter).hDiff(hDiff, units))
          .build();
      assertThat(parametricFunctional.dataErrorNorm()).as(parametricFunctional::toString).isCloseTo(expected, byLessThan(0.001));
    }

    @ParameterizedTest
    @CsvSource(delimiter = ',', textBlock = """
        10.0, 30.0, MILLI, 30.971, 31.278, -0.05, 5.0
        50.0, 30.0, MILLI, 62.479, 61.860,  0.05, 5.0
        """)
    void misfit(double sPU, double lCC, Metrics.Length units, double rBefore, double rAfter, double hDiff, double expectedH) {
      ParametricFunctional parametricFunctional = ParametricFunctional.builder(units)
          .system(s -> s.tetrapolar(sPU, lCC).absError(0.1))
          .measurements(m -> m.ohms(rBefore).thenOhms(rAfter).hDiff(hDiff, units))
          .build();

      Assertions.assertAll(parametricFunctional.toString(),
          () -> assertThat(parametricFunctional.misfit()
              .applyAsDouble(new Model.Layer2Relative(K.PLUS_ONE, units.toSI(expectedH))))
              .isNotNegative().isCloseTo(0.0, byLessThan(0.01))
      );
    }

    @ParameterizedTest
    @CsvSource(delimiter = ',', textBlock = """
        10.0, 30.0, METRE, 30.971, 31.278,  0.05, 5.0
        50.0, 30.0, MILLI, 62.479, 61.860, -0.05, 5.0
        """)
    void misfitInfinite(double sPU, double lCC, Metrics.Length units, double rBefore, double rAfter, double hDiff, double expectedH) {
      ParametricFunctional parametricFunctional = ParametricFunctional.builder(units)
          .system(s -> s.tetrapolar(sPU, lCC).absError(0.1))
          .measurements(m -> m.ohms(rBefore).thenOhms(rAfter).hDiff(hDiff, units))
          .build();
      Assertions.assertAll(parametricFunctional.toString(),
          () -> assertThat(parametricFunctional.misfit().applyAsDouble(new Model.Layer2Relative(K.PLUS_ONE, units.toSI(expectedH))))
              .isInfinite(),
          () -> assertThat(parametricFunctional.misfit().applyAsDouble(new Model.Layer2Relative(K.MINUS_ONE, units.toSI(expectedH))))
              .isPositive()
      );
    }

    @ParameterizedTest
    @CsvSource(delimiter = ',', textBlock = """
        10.0, 30.0, MILLI, 30.971, 31.278, -0.05
        50.0, 30.0, MILLI, 62.479, 61.860,  0.05
        """)
    void regularization(double sPU, double lCC, Metrics.Length units, double rBefore, double rAfter, double hDiff) {
      ParametricFunctional parametricFunctional = ParametricFunctional.builder(units)
          .system(s -> s.tetrapolar(sPU, lCC).absError(0.1))
          .measurements(m -> m.ohms(rBefore).thenOhms(rAfter).hDiff(hDiff, units))
          .build();

      ElectrodeSystem.Inexact inexact = ElectrodeSystem.builder(units).tetrapolar(sPU, lCC).absError(0.1).build();
      ToDoubleFunction<Model> regularization = parametricFunctional.regularization(ParametricFunctional.Regularization.ZERO_MAX_LOG);
      Assertions.assertAll(parametricFunctional.toString(),
          () -> assertThat(regularization.applyAsDouble(new Model.Layer2Relative(K.PLUS_ONE, inexact.hMax(K.PLUS_ONE)))).isInfinite(),
          () -> assertThat(regularization.applyAsDouble(new Model.Layer2Relative(K.PLUS_ONE, inexact.hMax(K.PLUS_ONE) / 2.0))).isInfinite(),
          () -> assertThat(regularization.applyAsDouble(new Model.Layer2Relative(K.PLUS_ONE, inexact.hMin(K.PLUS_ONE)))).isInfinite()
      );

      Assertions.assertAll(parametricFunctional.toString(),
          () -> {
            K k = K.of(0.5);
            assertThat(regularization.applyAsDouble(new Model.Layer2Relative(k, Math.sqrt(inexact.hMax(k) * inexact.hMin(k)))))
                .isNotNegative().isCloseTo(0.0, byLessThan(1.0e-9));
          }
      );
    }
  }

  @Nested
  class MaxDiffRelativeTest {

    @ParameterizedTest
    @CsvSource(delimiter = ',', textBlock = """
        10.0, 30.0, METRE, 30.971, 31.278, -0.05
        50.0, 30.0, MILLI, 62.479, 61.860,  0.05
        """)
    void dataErrorNorm(double sPU, double lCC, Metrics.Length units, double rBefore, double rAfter, double hDiff) {
      DoubleUnaryOperator d = emm -> {
        ElectrodeSystem.Tetrapolar system = ElectrodeSystem.builder(units).tetrapolar(sPU + emm, lCC - emm).build();
        Resistivity.Step1 resistivity = Resistivity.of(system);
        TetrapolarMeasurement.MaxDiff measurement = TetrapolarMeasurement.builder().ohms(rBefore).thenOhms(rAfter).hDiffMax(hDiff, units).build();
        double apparent = resistivity.apparent(measurement.ohms());
        double derivativeApparentByPhi = Math.abs(resistivity.apparent((measurement.ohmsDiff() / measurement.hDiffMax()) / system.phiFactor()));
        return log(apparent) - log(derivativeApparentByPhi);
      };

      double expected = Math.abs(d.applyAsDouble(0.0) - Math.max(d.applyAsDouble(0.1), d.applyAsDouble(-0.1)));

      ParametricFunctional parametricFunctional = ParametricFunctional.builder(units)
          .system(s -> s.tetrapolar(sPU, lCC).absError(0.1))
          .measurements(m -> m.ohms(rBefore).thenOhms(rAfter).hDiff(hDiff, units))
          .build();
      assertThat(parametricFunctional.dataErrorNorm()).as(parametricFunctional::toString).isCloseTo(expected, byLessThan(0.001));
    }

    @ParameterizedTest
    @CsvSource(delimiter = ',', textBlock = """
        10.0, 30.0, MILLI, 30.971, 31.278, -0.05, 5.0
        50.0, 30.0, MILLI, 62.479, 61.860,  0.05, 5.0
        """)
    void misfit(double sPU, double lCC, Metrics.Length units, double rBefore, double rAfter, double hDiff, double expectedH) {
      ParametricFunctional parametricFunctional = ParametricFunctional.builder(units)
          .system(s -> s.tetrapolar(sPU, lCC).absError(0.1))
          .measurements(m -> m.ohms(rBefore).thenOhms(rAfter).hDiffMax(hDiff, units))
          .build();

      Assertions.assertAll(parametricFunctional.toString(),
          () -> assertThat(parametricFunctional.misfit()
              .applyAsDouble(new Model.Layer2RelativeDh(K.PLUS_ONE, units.toSI(expectedH), units.toSI(hDiff))))
              .isNotNegative().isCloseTo(0.0, byLessThan(0.01))
      );
    }

    @ParameterizedTest
    @CsvSource(delimiter = ',', textBlock = """
        10.0, 30.0, METRE, 30.971, 31.278,  0.05, 5.0
        50.0, 30.0, MILLI, 62.479, 61.860, -0.05, 5.0
        """)
    void misfitInfinite(double sPU, double lCC, Metrics.Length units, double rBefore, double rAfter, double hDiff, double expectedH) {
      ParametricFunctional parametricFunctional = ParametricFunctional.builder(units)
          .system(s -> s.tetrapolar(sPU, lCC).absError(0.1))
          .measurements(m -> m.ohms(rBefore).thenOhms(rAfter).hDiffMax(hDiff, units))
          .build();
      Assertions.assertAll(parametricFunctional.toString(),
          () -> assertThat(parametricFunctional.misfit()
              .applyAsDouble(new Model.Layer2RelativeDh(K.PLUS_ONE, units.toSI(expectedH), hDiff)))
              .isInfinite(),
          () -> assertThat(parametricFunctional.misfit()
              .applyAsDouble(new Model.Layer2RelativeDh(K.MINUS_ONE, units.toSI(expectedH), hDiff)))
              .isPositive()
      );
    }

    @ParameterizedTest
    @CsvSource(delimiter = ',', textBlock = """
        10.0, 30.0, MILLI, 30.971, 31.278, -0.05
        50.0, 30.0, MILLI, 62.479, 61.860,  0.05
        """)
    void regularization(double sPU, double lCC, Metrics.Length units, double rBefore, double rAfter, double hDiff) {
      ParametricFunctional parametricFunctional = ParametricFunctional.builder(units)
          .system(s -> s.tetrapolar(sPU, lCC).absError(0.1))
          .measurements(m -> m.ohms(rBefore).thenOhms(rAfter).hDiffMax(hDiff, units))
          .build();

      ElectrodeSystem.Inexact inexact = ElectrodeSystem.builder(units).tetrapolar(sPU, lCC).absError(0.1).build();
      ToDoubleFunction<Model> regularization = parametricFunctional.regularization(ParametricFunctional.Regularization.ZERO_MAX_LOG);
      Assertions.assertAll(parametricFunctional.toString(),
          () -> assertThat(regularization.applyAsDouble(new Model.Layer2RelativeDh(K.PLUS_ONE, inexact.hMax(K.PLUS_ONE), units.toSI(hDiff)))).isInfinite(),
          () -> assertThat(regularization.applyAsDouble(new Model.Layer2RelativeDh(K.PLUS_ONE, inexact.hMax(K.PLUS_ONE) / 2.0, units.toSI(hDiff)))).isInfinite(),
          () -> assertThat(regularization.applyAsDouble(new Model.Layer2RelativeDh(K.PLUS_ONE, inexact.hMin(K.PLUS_ONE), units.toSI(hDiff)))).isInfinite()
      );

      Assertions.assertAll(parametricFunctional.toString(),
          () -> {
            K k = K.of(0.5);
            assertThat(regularization.applyAsDouble(new Model.Layer2RelativeDh(k, Math.sqrt(inexact.hMax(k) * inexact.hMin(k)), units.toSI(hDiff))))
                .isNotNegative().isCloseTo(0.0, byLessThan(1.0e-9));
          }
      );
    }
  }

  @Nested
  class TwoMaxDiffRelativeTest {

    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
        124.634 | 0.457 | 0.180 | 125.213 | 0.307 | 0.180
        """)
    void misfit(double r1, double r1Diff, double hDiffMax1,
                double r2, double r2DiffF, double hDiffMax2) {
      Metrics.Length units = Metrics.Length.MILLI;
      double hStep = 0.01;
      ParametricFunctional parametricFunctional = ParametricFunctional.builder(units)
          .system(s -> s.tetrapolar(6.0, 18.0).absError(0.1))
          .measurements(m -> m.ohms(r1).thenOhms(r1 + r1Diff).hDiffMax(hDiffMax1, units)
              .add(m2 -> m2.ohms(r2).thenOhms(r2 + r2DiffF).hDiffMax(hDiffMax2, units)))
          .build();

      Assertions.assertAll(parametricFunctional.toString(),
          () -> assertThat(parametricFunctional.misfit()
              .applyAsDouble(new Model.Layer3AbsoluteDRho2(
                      new Model.Layer3Absolute(2.0, 8.0, 4.0, units.toSI(hStep), new Model.P(100, 200)),
                      new Model.P(hDiffMax1 / hStep * 4 / 18, hDiffMax1 / hStep * 14 / 18), 0.02
                  )
              )
          ).isNotNegative().isCloseTo(0.0, byLessThan(0.01))
      );
    }

    @DisplayName("Модель 2 (1 мм) - 8 (2 мм) - 4 : 4/14 мкм + (8 + 0.02) + 4/14 мкм")
    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
        124.634 | 0.457 | 125.213 | 0.307 | 0.180
        """)
    void regularization(double r1, double r1Diff,
                        double r2, double r2DiffF, double hDiffMax) {
      Metrics.Length units = Metrics.Length.MILLI;
      double hStep = 0.01;
      ParametricFunctional parametricFunctional = ParametricFunctional.builder(units)
          .system(s -> s.tetrapolar(6.0, 18.0).absError(0.1))
          .measurements(m -> m.ohms(r1).thenOhms(r1 + r1Diff).hDiffMax(hDiffMax, units)
              .add(m2 -> m2.ohms(r2).thenOhms(r2 + r2DiffF).hDiffMax(hDiffMax, units)))
          .build();

      ToDoubleFunction<Model> regularization = parametricFunctional.regularization(ParametricFunctional.Regularization.ZERO_MAX_LOG);
      Assertions.assertAll(parametricFunctional.toString(),
          () -> assertThat(regularization.applyAsDouble(
              new Model.Layer3AbsoluteDRho2(
                  new Model.Layer3Absolute(2.0, 8.0, 4.0, units.toSI(hStep), new Model.P(100, 200)),
                  new Model.P((hDiffMax / hStep) * 4 / 18, (hDiffMax / hStep) * 14 / 18), 0.02
              ))
          ).isNotNegative().isCloseTo(0.336, byLessThan(1.0e-3))
      );
    }
  }
}