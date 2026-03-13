package com.ak.rsm2;

import com.ak.util.Metrics;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertAll;

class ElectrodeSystemTest {
  @Nested
  class Relative {
    @ParameterizedTest
    @ValueSource(doubles = {1.0 / 3.0, 3.0, -1.0 / 2.0, -2.0})
    void get(double sToL) {
      assertThat(ElectrodeSystem.of(sToL).sToL()).isCloseTo(Math.abs(sToL), byLessThan(0.001));
    }

    @ParameterizedTest
    @ValueSource(doubles = {1.0 / 3.0, -3.0})
    void toString(double sToL) {
      assertThat(ElectrodeSystem.of(sToL)).hasToString("s / L = %.3f".formatted(Math.abs(Math.min(sToL, 1.0 / sToL))));
    }

    @ParameterizedTest
    @ValueSource(doubles = 0.0)
    void invalidZero(double sToL) {
      assertThatIllegalArgumentException().isThrownBy(() -> ElectrodeSystem.of(sToL)).withMessage("s / L cannot be zero");
    }

    @ParameterizedTest
    @ValueSource(doubles = Double.NaN)
    void invalidNaN(double sToL) {
      assertThatIllegalArgumentException().isThrownBy(() -> ElectrodeSystem.of(sToL)).withMessage("s / L is NaN");
    }

    @ParameterizedTest
    @ValueSource(doubles = {Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY})
    void invalidInfinite(double sToL) {
      assertThatIllegalArgumentException().isThrownBy(() -> ElectrodeSystem.of(sToL)).withMessageEndingWith("Infinity");
    }

    @ParameterizedTest
    @ValueSource(doubles = {1.0, -1.0})
    void invalidEqual(double sToL) {
      assertThatIllegalArgumentException().isThrownBy(() -> ElectrodeSystem.of(sToL)).withMessage("s cannot be equals to L");
    }
  }

  @Nested
  class Tetrapolar {
    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
         -10 | -30
          50 | 30
        """)
    void get(double sPU, double lCC) {
      ElectrodeSystem.Tetrapolar tetrapolar = ElectrodeSystem.ofMilli().tetrapolar(sPU, lCC).build();
      assertAll(tetrapolar.toString(),
          () -> assertThat(tetrapolar.sToL()).isCloseTo(Math.abs(sPU / lCC), byLessThan(0.001)),
          () -> assertThat(tetrapolar.sPU()).isCloseTo(Metrics.MILLI.applyAsDouble(Math.abs(sPU)), byLessThan(0.001)),
          () -> assertThat(tetrapolar.lCC()).isCloseTo(Metrics.MILLI.applyAsDouble(Math.abs(lCC)), byLessThan(0.001))
      );
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
        18 |  6.1 | '18,0 x  6,1 mm'
        10 | 30.1 | '10,0 x 30,1 mm'
        """)
    void toString(double sPU, double lCC, String expected) {
      assertThat(ElectrodeSystem.ofMilli().tetrapolar(sPU, lCC).build()).hasToString(expected);
    }
  }

  @Nested
  class Inexact {
    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
         -10 | -30 | 0.1
          30 |  10 | 0.1
        """)
    void get(double sPU, double lCC, double absError) {
      ElectrodeSystem.Inexact inexact = ElectrodeSystem.ofMilli().tetrapolar(sPU, lCC).absError(absError).build();
      assertAll(inexact.toString(),
          () -> assertThat(inexact.sToL()).isCloseTo(Math.abs(sPU / lCC), byLessThan(0.001)),
          () -> assertThat(inexact.sPU()).isCloseTo(Metrics.MILLI.applyAsDouble(Math.abs(sPU)), byLessThan(0.001)),
          () -> assertThat(inexact.lCC()).isCloseTo(Metrics.MILLI.applyAsDouble(Math.abs(lCC)), byLessThan(0.001)),
          () -> assertThat(inexact.apparentRhoRelativeError()).isCloseTo(Math.abs(6.0 * absError / Math.max(Math.abs(sPU), Math.abs(lCC))),
              byLessThan(0.001)),
          () -> assertThat(inexact.hMax(K.PLUS_ONE)).as(inexact::toString)
              .isCloseTo(Metrics.MILLI.applyAsDouble(0.177 * 30.0 / StrictMath.pow(0.1 / 30.0, 1.0 / 3.0)), byLessThan(0.001)),
          () -> assertThat(inexact.hMin(K.PLUS_ONE)).as(inexact::toString).isZero()
      );
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
        10 | 30 | 0.1 | 1.0 | 2.0
        """)
    void hMinMax(double sPU, double lCC, double absError, double rho1, double rho2) {
      ElectrodeSystem.Inexact inexact = ElectrodeSystem.ofMilli().tetrapolar(sPU, lCC).absError(absError).build();
      assertAll(inexact.toString(),
          () -> assertThat(inexact.hMin(K.of(rho1, rho2))).isCloseTo(Metrics.MILLI.applyAsDouble(0.6), byLessThan(2.0e-5)),
          () -> assertThat(inexact.hMax(K.of(rho1, rho2))).isCloseTo(Metrics.MILLI.applyAsDouble(23.5), byLessThan(1.0e-4))
      );
      assertAll(inexact.toString(),
          () -> assertThat(inexact.hMin(K.PLUS_ONE)).isZero(),
          () -> assertThat(inexact.hMax(K.PLUS_ONE)).isCloseTo(Metrics.MILLI.applyAsDouble(35.6), byLessThan(1.0e-4))
      );
      assertAll(inexact.toString(),
          () -> assertThat(inexact.hMin(K.ZERO)).isInfinite(),
          () -> assertThat(inexact.hMax(K.ZERO)).isZero()
      );
      assertAll(inexact.toString(),
          () -> assertThat(inexact.hMin(K.MINUS_ONE)).isCloseTo(Metrics.MILLI.applyAsDouble(1.0), byLessThan(1.0e-4)),
          () -> assertThat(inexact.hMax(K.MINUS_ONE)).isCloseTo(Metrics.MILLI.applyAsDouble(32.3), byLessThan(1.0e-4))
      );
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
        -10 | -30 |  0.1 | '10,0 x 30,0 mm / 0,1 mm; ↕ 36 mm'
         50 |  30 | 0.01 | '50,0 x 30,0 mm / 0,0 mm; ↕ 131 mm'
        """)
    void toString(double sPU, double lCC, double absError, String expected) {
      assertThat(ElectrodeSystem.ofMilli().tetrapolar(sPU, lCC).absError(absError).build()).hasToString(expected);
    }

    @ParameterizedTest
    @ValueSource(doubles = 0.0)
    void invalidZero(double absError) {
      assertThatIllegalArgumentException()
          .isThrownBy(() -> ElectrodeSystem.ofMilli().tetrapolar(10.0, 20.0).absError(absError).build())
          .withMessage("absError cannot be zero");
    }
  }
}