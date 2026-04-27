package com.ak.rsm2;

import com.ak.math.Simplex;
import com.ak.util.Metrics;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Arrays;
import java.util.function.DoubleUnaryOperator;
import java.util.function.ToDoubleFunction;

import static java.lang.StrictMath.log;
import static org.assertj.core.api.Assertions.*;

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
        Resistivity resistivity = Resistivity.of(system).build();
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
        10.0, 30.0, METRE, 35.589
        50.0, 30.0, MILLI, 60.8
        """)
    void bounds(double sPU, double lCC, Metrics.Length units, double expectedH) {
      ParametricFunctional parametricFunctional = ParametricFunctional.builder(units)
          .system(s -> s.tetrapolar(sPU, lCC).absError(0.1))
          .measurements(m -> m.ohms(0.0).thenOhms(0.0).hDiff(0.0, units))
          .build();
      Assertions.assertAll(Arrays.toString(parametricFunctional.bounds()),
          () -> assertThat(parametricFunctional.bounds()).hasSize(2),
          () -> assertThat(parametricFunctional.bounds()[0]).isEqualTo(new Simplex.Bounds(-1.0, Double.NaN, 1.0)),
          () -> assertThat(parametricFunctional.bounds()[1].min()).isZero(),
          () -> assertThat(parametricFunctional.bounds()[1].max()).isCloseTo(units.toSI(expectedH), byLessThan(0.001))
      );
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
          () -> {
            Model layer2 = new Model.Layer2Relative(K.PLUS_ONE, units.toSI(expectedH));
            assertThat(parametricFunctional.misfit().applyAsDouble(layer2))
                .isNotNegative().isCloseTo(0.0, byLessThan(0.01));
          },
          () -> {
            Model layer2 = new Model.Layer2Absolute(1.0, 2.0, units.toSI(expectedH), units.toSI(hDiff));
            assertThatIllegalArgumentException().isThrownBy(() -> parametricFunctional.misfit().applyAsDouble(layer2))
                .withMessageStartingWith("Unexpected value");
          }
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
          },
          () -> assertThatIllegalArgumentException().isThrownBy(() ->
                  regularization.applyAsDouble(new Model.Layer2Absolute(0.0, 0.0, 0.0, 0.0)))
              .withMessageStartingWith("Unexpected value")
      );
    }
  }

  @Nested
  class MaxDiffAbsoluteTest {

    @ParameterizedTest
    @CsvSource(delimiter = ',', textBlock = """
        10.0, 30.0, METRE, 30.971, 31.278, -0.05
        50.0, 30.0, MILLI, 62.479, 61.860,  0.05
        """)
    void dataErrorNorm(double sPU, double lCC, Metrics.Length units, double rBefore, double rAfter, double hDiffMax) {
      DoubleUnaryOperator d = emm -> {
        ElectrodeSystem.Tetrapolar system = ElectrodeSystem.builder(units).tetrapolar(sPU + emm, lCC - emm).build();
        Resistivity resistivity = Resistivity.of(system).build();
        TetrapolarMeasurement.MaxDiffAbsolute measurement = TetrapolarMeasurement.builder()
            .ohms(rBefore).thenOhms(rAfter).hDiffMaxAbsolute(hDiffMax, units).build();
        double apparent = resistivity.apparent(measurement.ohms());
        double derivativeApparentByPhi = Math.abs(resistivity.apparent((measurement.ohmsDiff() / measurement.hDiffMax()) / system.phiFactor()));
        return log(apparent) - log(derivativeApparentByPhi);
      };

      double expected = Math.abs(d.applyAsDouble(0.0) - Math.max(d.applyAsDouble(0.1), d.applyAsDouble(-0.1)));

      ParametricFunctional parametricFunctional = ParametricFunctional.builder(units)
          .system(s -> s.tetrapolar(sPU, lCC).absError(0.1))
          .measurements(m -> m.ohms(rBefore).thenOhms(rAfter).hDiffMaxAbsolute(hDiffMax, units))
          .build();
      assertThat(parametricFunctional.dataErrorNorm()).as(parametricFunctional::toString).isCloseTo(expected, byLessThan(0.001));
    }

    @ParameterizedTest
    @CsvSource(delimiter = ',', textBlock = """
        10.0, 30.0, METRE, -0.150, 35.589
        50.0, 30.0, MILLI,  0.090, 60.8
        """)
    void bounds(double sPU, double lCC, Metrics.Length units, double hDiffMax, double expectedH) {
      ParametricFunctional parametricFunctional = ParametricFunctional.builder(units)
          .system(s -> s.tetrapolar(sPU, lCC).absError(0.1))
          .measurements(m -> m.ohms(0.0).thenOhms(0.0).hDiffMaxAbsolute(hDiffMax, units))
          .build();
      Assertions.assertAll(Arrays.toString(parametricFunctional.bounds()),
          () -> assertThat(parametricFunctional.bounds()).hasSize(4),
          () -> assertThat(parametricFunctional.bounds()[0].min()).isZero(),
          () -> assertThat(parametricFunctional.bounds()[0].max()).isPositive(),
          () -> assertThat(parametricFunctional.bounds()[1].min()).isZero(),
          () -> assertThat(parametricFunctional.bounds()[1].max()).isPositive(),
          () -> assertThat(parametricFunctional.bounds()[2].min()).isZero(),
          () -> assertThat(parametricFunctional.bounds()[2].max()).isCloseTo(units.toSI(expectedH), byLessThan(0.001))
      );
      if (hDiffMax > 0) {
        Assertions.assertAll(Arrays.toString(parametricFunctional.bounds()),
            () -> assertThat(parametricFunctional.bounds()[3].min()).isZero(),
            () -> assertThat(parametricFunctional.bounds()[3].max()).isCloseTo(units.toSI(hDiffMax), byLessThan(0.001))
        );
      }
      else {
        Assertions.assertAll(Arrays.toString(parametricFunctional.bounds()),
            () -> assertThat(parametricFunctional.bounds()[3].min()).isCloseTo(units.toSI(hDiffMax), byLessThan(0.001)),
            () -> assertThat(parametricFunctional.bounds()[3].max()).isZero()
        );
      }
    }

    @ParameterizedTest
    @CsvSource(delimiter = ',', textBlock = """
        10.0, 30.0, MILLI, 30.971, 31.278, -0.05, 0.7, Infinity, 5.0
        50.0, 30.0, MILLI, 62.479, 61.860,  0.05, 0.7, Infinity, 5.0
        """)
    void misfit(double sPU, double lCC, Metrics.Length units, double rBefore, double rAfter, double hDiffMax,
                double expectedRho1, double expectedRho2, double expectedH) {
      ParametricFunctional parametricFunctional = ParametricFunctional.builder(units)
          .system(s -> s.tetrapolar(sPU, lCC).absError(0.1))
          .measurements(m -> m.ohms(rBefore).thenOhms(rAfter).hDiffMaxAbsolute(hDiffMax, units))
          .build();

      Assertions.assertAll(parametricFunctional.toString(),
          () -> {
            Model layer2 = new Model.Layer2Relative(K.PLUS_ONE, units.toSI(expectedH));
            assertThatIllegalArgumentException().isThrownBy(() -> parametricFunctional.misfit().applyAsDouble(layer2))
                .withMessageStartingWith("Unexpected value");
          },
          () -> {
            Model layer2 = new Model.Layer2Absolute(expectedRho1, expectedRho2, units.toSI(expectedH), units.toSI(hDiffMax));
            assertThat(parametricFunctional.misfit().applyAsDouble(layer2))
                .isNotNegative().isCloseTo(0.0, byLessThan(0.02));
          }
      );
    }

    @ParameterizedTest
    @CsvSource(delimiter = ',', textBlock = """
        10.0, 30.0, METRE, 30.971, 31.278,  0.05, 5.0
        50.0, 30.0, MILLI, 62.479, 61.860, -0.05, 5.0
        """)
    void misfitInfinite(double sPU, double lCC, Metrics.Length units, double rBefore, double rAfter, double hDiffMax, double expectedH) {
      ParametricFunctional parametricFunctional = ParametricFunctional.builder(units)
          .system(s -> s.tetrapolar(sPU, lCC).absError(0.1))
          .measurements(m -> m.ohms(rBefore).thenOhms(rAfter).hDiffMaxAbsolute(hDiffMax, units))
          .build();
      Assertions.assertAll(parametricFunctional.toString(),
          () -> assertThat(parametricFunctional.misfit().applyAsDouble(new Model.Layer2Absolute(1.0, Double.POSITIVE_INFINITY,
              units.toSI(expectedH), units.toSI(hDiffMax))))
              .isInfinite(),
          () -> assertThat(parametricFunctional.misfit().applyAsDouble(new Model.Layer2Absolute(1.0, 0.0,
              units.toSI(expectedH), units.toSI(hDiffMax))))
              .isPositive()
      );
    }

    @ParameterizedTest
    @CsvSource(delimiter = ',', textBlock = """
        10.0, 30.0, MILLI, 25.099, 25.294, -0.05, 0.7, 7.0, 5.0
        50.0, 30.0, MILLI, 46.281, 45.945,  0.05, 0.7, 7.0, 5.0
        """)
    void regularization(double sPU, double lCC, Metrics.Length units, double rBefore, double rAfter, double hDiffMax,
                        double expectedRho1, double expectedRho2, double expectedH) {
      ParametricFunctional parametricFunctional = ParametricFunctional.builder(units)
          .system(s -> s.tetrapolar(sPU, lCC).absError(0.1))
          .measurements(m -> m.ohms(rBefore).thenOhms(rAfter).hDiffMaxAbsolute(hDiffMax, units))
          .build();

      ElectrodeSystem.Inexact inexact = ElectrodeSystem.builder(units).tetrapolar(sPU, lCC).absError(0.1).build();
      ToDoubleFunction<Model> regularization = parametricFunctional.regularization(ParametricFunctional.Regularization.ZERO_MAX_LOG);
      Assertions.assertAll(parametricFunctional.toString(),
          () -> assertThat(regularization.applyAsDouble(
              new Model.Layer2Absolute(expectedRho1, expectedRho2, inexact.hMax(K.PLUS_ONE), units.toSI(hDiffMax)))).isInfinite(),
          () -> assertThat(regularization.applyAsDouble(
              new Model.Layer2Absolute(expectedRho1, expectedRho2, inexact.hMin(K.PLUS_ONE), units.toSI(hDiffMax)))).isInfinite(),
          () -> assertThat(regularization.applyAsDouble(
              new Model.Layer2Absolute(expectedRho1, expectedRho2, units.toSI(expectedH), units.toSI(hDiffMax)))).isPositive()
      );

      K k = K.of(expectedRho1, expectedRho2);
      Assertions.assertAll(parametricFunctional.toString(),
          () -> assertThatIllegalArgumentException().isThrownBy(() -> regularization
                  .applyAsDouble(new Model.Layer2Relative(k, Math.sqrt(inexact.hMax(k) * inexact.hMin(k)))))
              .withMessageStartingWith("Unexpected value"),
          () -> assertThat(regularization
              .applyAsDouble(new Model.Layer2Absolute(expectedRho1, expectedRho2,
                  Math.sqrt(inexact.hMax(k) * inexact.hMin(k)), units.toSI(hDiffMax))))
              .isNotNegative().isCloseTo(0.0, byLessThan(1.0e-9))
      );
    }
  }

  @Nested
  class StaticTest {

    @ParameterizedTest
    @CsvSource(delimiter = ',', textBlock = """
        10.0, 30.0, METRE, 30.971, 31.278
        50.0, 30.0, MILLI, 62.479, 61.860
        """)
    void dataErrorNorm(double sPU, double lCC, Metrics.Length units, double rBefore, double rAfter) {
      ParametricFunctional parametricFunctional = ParametricFunctional.builder(units)
          .system(s -> s.tetrapolar(sPU, lCC).absError(0.1))
          .measurements(m -> m.ohms(rBefore).thenOhms(rAfter))
          .build();
      assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(parametricFunctional::dataErrorNorm)
          .withMessage("Not supported yet.");
    }

    @ParameterizedTest
    @CsvSource(delimiter = ',', textBlock = """
        10.0, 30.0, METRE, 35.589
        50.0, 30.0, MILLI, 60.8
        """)
    void bounds(double sPU, double lCC, Metrics.Length units, double expectedH) {
      ParametricFunctional parametricFunctional = ParametricFunctional.builder(units)
          .system(s -> s.tetrapolar(sPU, lCC).absError(0.1))
          .measurements(m -> m.ohms(0.0).thenOhms(0.0))
          .build();
      Assertions.assertAll(Arrays.toString(parametricFunctional.bounds()),
          () -> assertThat(parametricFunctional.bounds()).hasSize(4),
          () -> assertThat(parametricFunctional.bounds()[0].min()).isZero(),
          () -> assertThat(parametricFunctional.bounds()[0].max()).isPositive(),
          () -> assertThat(parametricFunctional.bounds()[1].min()).isZero(),
          () -> assertThat(parametricFunctional.bounds()[1].max()).isPositive(),
          () -> assertThat(parametricFunctional.bounds()[2].min()).isZero(),
          () -> assertThat(parametricFunctional.bounds()[2].max()).isPositive(),
          () -> assertThat(parametricFunctional.bounds()[3].min()).isZero(),
          () -> assertThat(parametricFunctional.bounds()[3].max()).isCloseTo(units.toSI(expectedH), byLessThan(0.001))
      );
    }

    @ParameterizedTest
    @CsvSource(delimiter = ',', textBlock = """
        10.0, 30.0, MILLI, 30.971, 31.278, 5.0
        50.0, 30.0, MILLI, 62.479, 61.860, 5.0
        """)
    void misfit(double sPU, double lCC, Metrics.Length units, double rBefore, double rAfter, double expectedH) {
      ParametricFunctional parametricFunctional = ParametricFunctional.builder(units)
          .system(s -> s.tetrapolar(sPU, lCC).absError(0.1))
          .measurements(m -> m.ohms(rBefore).thenOhms(rAfter))
          .build();

      Assertions.assertAll(parametricFunctional.toString(),
          () -> {
            Model layer2 = new Model.Layer2Relative(K.PLUS_ONE, units.toSI(expectedH));
            assertThatIllegalArgumentException().isThrownBy(() -> parametricFunctional.misfit().applyAsDouble(layer2))
                .withMessageStartingWith("Unexpected value");
          },
          () -> {
            Model layer2 = new Model.Layer2Absolute(1.0, 2.0, units.toSI(expectedH), 0.0);
            assertThatIllegalArgumentException().isThrownBy(() -> parametricFunctional.misfit().applyAsDouble(layer2))
                .withMessageStartingWith("Unexpected value");
          }
      );
    }

    @ParameterizedTest
    @CsvSource(delimiter = ',', textBlock = """
        10.0, 30.0, MILLI, 25.099, 25.294
        50.0, 30.0, MILLI, 46.281, 45.945
        """)
    void regularization(double sPU, double lCC, Metrics.Length units, double rBefore, double rAfter) {
      ParametricFunctional parametricFunctional = ParametricFunctional.builder(units)
          .system(s -> s.tetrapolar(sPU, lCC).absError(0.1))
          .measurements(m -> m.ohms(rBefore).thenOhms(rAfter))
          .build();
      assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(
              () -> parametricFunctional.regularization(ParametricFunctional.Regularization.ZERO_MAX_LOG))
          .withMessage("Not supported yet.");
    }
  }
}