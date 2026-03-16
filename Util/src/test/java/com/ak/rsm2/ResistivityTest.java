package com.ak.rsm2;

import com.ak.util.Metrics;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.byLessThan;

class ResistivityTest {
  @Nested
  class ApparentDivRho1 {
    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
        8.0 | 1.0 | 10.0 | 10.0 | 20.0 | 0.911
        8.0 | 1.0 | 10.0 | 20.0 | 10.0 | 0.911
        """)
    void apparentDivRho1(double rho1, double rho2, double hmm, double smm, double lmm, double expected) {
      ElectrodeSystem.Tetrapolar tetrapolar = ElectrodeSystem.ofMilli().tetrapolar(smm, lmm).build();
      Model.Layer2Relative layer2 = new Model.Layer2Relative(K.of(rho1, rho2), Metrics.Length.MILLI.toSI(hmm));
      double value = Resistivity.of(tetrapolar).apparentDivRho1().applyAsDouble(layer2);
      assertThat(value).isCloseTo(expected, byLessThan(0.001));
    }

    @ParameterizedTest
    @MethodSource("com.ak.rsm.resistance.Resistance2LayerTest#twoLayerParameters")
    void apparent(double[] rho, double hmm, double smm, double lmm, double rOhm) {
      apparent(smm, lmm, rOhm);
      apparentDivRho1(rho, hmm, smm, lmm);
      apparentDivRho1(rho, hmm, smm, lmm, rOhm);
    }

    private static void apparent(double smm, double lmm, double rOhm) {
      double apparentNor = Resistivity.of(ElectrodeSystem.ofMilli().tetrapolar(smm, lmm).build()).apparent(rOhm);
      double apparentInv = Resistivity.of(ElectrodeSystem.ofMilli().tetrapolar(lmm, smm).build()).apparent(rOhm);
      assertThat(apparentNor).isEqualTo(apparentInv);
    }

    private static void apparentDivRho1(double[] rho, double hmm, double smm, double lmm) {
      Model.Layer2Relative layer2 = new Model.Layer2Relative(K.of(rho[0], rho[1]), Metrics.Length.MILLI.toSI(hmm));
      double predictedNor = Resistivity.of(ElectrodeSystem.ofMilli().tetrapolar(smm, lmm).build())
          .apparentDivRho1().applyAsDouble(layer2);
      double predictedRev = Resistivity.of(ElectrodeSystem.ofMilli().tetrapolar(lmm, smm).build())
          .apparentDivRho1().applyAsDouble(layer2);
      assertThat(predictedNor).isCloseTo(predictedRev, byLessThan(0.000_001));
    }

    private static void apparentDivRho1(double[] rho, double hmm, double smm, double lmm, double rOhm) {
      ElectrodeSystem.Tetrapolar tetrapolar = ElectrodeSystem.ofMilli().tetrapolar(smm, lmm).build();
      double apparent = Resistivity.of(tetrapolar).apparent(rOhm);
      Model.Layer2Relative layer2 = new Model.Layer2Relative(K.of(rho[0], rho[1]), Metrics.Length.MILLI.toSI(hmm));
      double predicted = Resistivity.of(tetrapolar).apparentDivRho1().applyAsDouble(layer2);
      assertThat(apparent / rho[0]).isCloseTo(predicted, byLessThan(0.001));
    }
  }

  @Nested
  class DerivativeApparentByPhoDivRho1 {
    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
        8.0 |  1.0 | 10.0 | 10.0 | 20.0 |  0.308
        8.0 |  1.0 | 10.0 | 20.0 | 10.0 |  0.308
        2.0 | 10.0 |  3.0 |  6.0 | 18.0 | -9.609
        2.0 | 10.0 |  3.0 | 18.0 |  6.0 | -9.609
        """)
    void apparentDivRho1(double rho1, double rho2, double hmm, double smm, double lmm, double expected) {
      ElectrodeSystem.Tetrapolar tetrapolar = ElectrodeSystem.ofMilli().tetrapolar(smm, lmm).build();
      Model.Layer2Relative layer2 = new Model.Layer2Relative(K.of(rho1, rho2), Metrics.Length.MILLI.toSI(hmm));
      double value = Resistivity.of(tetrapolar).derivativeApparentByPhoDivRho1().applyAsDouble(layer2);
      assertThat(value).isCloseTo(expected, byLessThan(0.001));
    }

    @ParameterizedTest
    @MethodSource("com.ak.rsm.resistance.Resistance2LayerTest#twoLayerParameters")
    void apparentDivRho1(double[] rho, double hmm, double smm, double lmm) {
      Model.Layer2Relative layer2 = new Model.Layer2Relative(K.of(rho[0], rho[1]), Metrics.Length.MILLI.toSI(hmm));
      double predictedNor = Resistivity.of(ElectrodeSystem.ofMilli().tetrapolar(smm, lmm).build())
          .derivativeApparentByPhoDivRho1().applyAsDouble(layer2);
      double predictedRev = Resistivity.of(ElectrodeSystem.ofMilli().tetrapolar(lmm, smm).build())
          .derivativeApparentByPhoDivRho1().applyAsDouble(layer2);
      assertThat(predictedNor).isCloseTo(predictedRev, byLessThan(0.000_001));
    }
  }
}