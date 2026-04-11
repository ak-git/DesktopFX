package com.ak.rsm2;

import com.ak.util.Metrics;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
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
         -10 | -30 | METRE
          50 |  30 | MILLI
        """)
    void get(double sPU, double lCC, Metrics.Length units) {
      ElectrodeSystem.Tetrapolar tetrapolar = ElectrodeSystem.builder(units).tetrapolar(sPU, lCC).build();
      assertAll(tetrapolar.toString(),
          () -> assertThat(tetrapolar.sToL()).isCloseTo(Math.abs(sPU / lCC), byLessThan(0.001)),
          () -> assertThat(tetrapolar.sPU()).isCloseTo(units.toSI(Math.abs(sPU)), byLessThan(0.001)),
          () -> assertThat(tetrapolar.lCC()).isCloseTo(units.toSI(Math.abs(lCC)), byLessThan(0.001))
      );
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
        10 | 20 | METRE | 0.0666
        20 | 10 | MILLI | 66.666
        """)
    void phiFactor(double sPU, double lCC, Metrics.Length units, double expectedPhi) {
      ElectrodeSystem.Tetrapolar tetrapolar = ElectrodeSystem.builder(units).tetrapolar(sPU, lCC).build();
      assertThat(tetrapolar.phiFactor()).isCloseTo(expectedPhi, byLessThan(0.001));
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
        18 |  6.1 | METRE | '18000,0 x 6100,0 mm'
        10 | 30.1 | MILLI | '10,0 x 30,1 mm'
        """)
    void toString(double sPU, double lCC, Metrics.Length units, String expected) {
      assertThat(ElectrodeSystem.builder(units).tetrapolar(sPU, lCC).build()).hasToString(expected);
    }
  }

  @Nested
  class Inexact {
    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
         -10 | -30 | 0.1 | METRE
          30 |  10 | 0.1 | MILLI
        """)
    void get(double sPU, double lCC, double absError, Metrics.Length units) {
      ElectrodeSystem.Inexact inexact = ElectrodeSystem.builder(units).tetrapolar(sPU, lCC).absError(absError).build();
      assertAll(inexact.toString(),
          () -> assertThat(inexact.sToL()).isCloseTo(Math.abs(sPU / lCC), byLessThan(0.001)),
          () -> assertThat(inexact.sPU()).isCloseTo(units.toSI(Math.abs(sPU)), byLessThan(0.001)),
          () -> assertThat(inexact.lCC()).isCloseTo(units.toSI(Math.abs(lCC)), byLessThan(0.001)),
          () -> assertThat(inexact.apparentRhoRelativeError()).isCloseTo(Math.abs(6.0 * absError / Math.max(Math.abs(sPU), Math.abs(lCC))),
              byLessThan(0.001)),
          () -> assertThat(inexact.hMax(K.PLUS_ONE)).as(inexact::toString)
              .isCloseTo(units.toSI(0.177 * 30.0 / StrictMath.pow(0.1 / 30.0, 1.0 / 3.0)), byLessThan(0.1)),
          () -> assertThat(inexact.hMin(K.PLUS_ONE)).as(inexact::toString).isZero()
      );
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
        10 | 30 | 0.1 | METRE | 1.0 | 2.0
        10 | 30 | 0.1 | MILLI | 1.0 | 2.0
        """)
    void hMinMax(double sPU, double lCC, double absError, Metrics.Length units, double rho1, double rho2) {
      ElectrodeSystem.Inexact inexact = ElectrodeSystem.builder(units).tetrapolar(sPU, lCC).absError(absError).build();
      assertAll(inexact.toString(),
          () -> assertThat(inexact.hMin(K.of(rho1, rho2))).isCloseTo(units.toSI(0.6), byLessThan(0.1)),
          () -> assertThat(inexact.hMax(K.of(rho1, rho2))).isCloseTo(units.toSI(23.5), byLessThan(0.1))
      );
      assertAll(inexact.toString(),
          () -> assertThat(inexact.hMin(K.PLUS_ONE)).isZero(),
          () -> assertThat(inexact.hMax(K.PLUS_ONE)).isCloseTo(units.toSI(35.6), byLessThan(0.1))
      );
      assertAll(inexact.toString(),
          () -> assertThat(inexact.hMin(K.ZERO)).isInfinite(),
          () -> assertThat(inexact.hMax(K.ZERO)).isZero()
      );
      assertAll(inexact.toString(),
          () -> assertThat(inexact.hMin(K.MINUS_ONE)).isCloseTo(units.toSI(1.0), byLessThan(0.1)),
          () -> assertThat(inexact.hMax(K.MINUS_ONE)).isCloseTo(units.toSI(32.3), byLessThan(0.1))
      );
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
        -10 | -30 |  0.1 | METRE | '10000,0 x 30000,0 mm / 100,0 mm; ↕ 35589 mm'
         50 |  30 | 0.01 | METRE | '50000,0 x 30000,0 mm / 10,0 mm; ↕ 131112 mm'
        -10 | -30 |  0.1 | MILLI | '10,0 x 30,0 mm / 0,1 mm; ↕ 36 mm'
         50 |  30 | 0.01 | MILLI | '50,0 x 30,0 mm / 0,0 mm; ↕ 131 mm'
        """)
    void toString(double sPU, double lCC, double absError, Metrics.Length units, String expected) {
      assertThat(ElectrodeSystem.builder(units).tetrapolar(sPU, lCC).absError(absError).build()).hasToString(expected);
    }

    @ParameterizedTest
    @EnumSource(Metrics.Length.class)
    void invalidZero(Metrics.Length units) {
      assertThatIllegalArgumentException()
          .isThrownBy(() -> ElectrodeSystem.builder(units).tetrapolar(10.0, 20.0).absError(0.0).build())
          .withMessage("absError cannot be zero");
    }
  }
}