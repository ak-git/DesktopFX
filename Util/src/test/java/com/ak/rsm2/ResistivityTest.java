package com.ak.rsm2;

import com.ak.util.Metrics;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.byLessThan;

class ResistivityTest {
  @ParameterizedTest
  @CsvSource(delimiter = '|', textBlock = """
      8.0 | 1.0 | 10.0 | 10.0 | 20.0 | 0.911
      8.0 | 1.0 | 10.0 | 20.0 | 10.0 | 0.911
      """)
  void normalizedByRho1(double rho1, double rho2, double hmm, double smm, double lmm, double expected) {
    ElectrodeSystem.Tetrapolar tetrapolar = ElectrodeSystem.ofMilli().tetrapolar(smm, lmm).build();
    double value = Resistivity.normalizedByRho1(tetrapolar).value(K.of(rho1, rho2).value(), Metrics.Length.MILLI.toSI(hmm));
    assertThat(value).isCloseTo(expected, byLessThan(0.001));
  }

  @ParameterizedTest
  @MethodSource("com.ak.rsm.resistance.Resistance2LayerTest#twoLayerParameters")
  void apparent(double[] rho, double hmm, double smm, double lmm, double rOhm) {
    apparent(smm, lmm, rOhm);
    normalizedByRho1(rho, hmm, smm, lmm);
    normalizedByRho1(rho, hmm, smm, lmm, rOhm);
  }

  private static void apparent(double smm, double lmm, double rOhm) {
    double apparentNor = Resistivity.apparent(ElectrodeSystem.ofMilli().tetrapolar(smm, lmm).build(), rOhm);
    double apparentInv = Resistivity.apparent(ElectrodeSystem.ofMilli().tetrapolar(lmm, smm).build(), rOhm);
    assertThat(apparentNor).isEqualTo(apparentInv);
  }

  private static void normalizedByRho1(double[] rho, double hmm, double smm, double lmm) {
    double predictedNor = Resistivity.normalizedByRho1(ElectrodeSystem.ofMilli().tetrapolar(smm, lmm).build())
        .value(K.of(rho[0], rho[1]).value(), Metrics.Length.MILLI.toSI(hmm));
    double predictedRev = Resistivity.normalizedByRho1(ElectrodeSystem.ofMilli().tetrapolar(lmm, smm).build())
        .value(K.of(rho[0], rho[1]).value(), Metrics.Length.MILLI.toSI(hmm));
    assertThat(predictedNor).isCloseTo(predictedRev, byLessThan(0.000_001));
  }

  private static void normalizedByRho1(double[] rho, double hmm, double smm, double lmm, double rOhm) {
    ElectrodeSystem.Tetrapolar tetrapolar = ElectrodeSystem.ofMilli().tetrapolar(smm, lmm).build();
    double apparent = Resistivity.apparent(tetrapolar, rOhm);
    double predicted = Resistivity.normalizedByRho1(tetrapolar).value(K.of(rho[0], rho[1]).value(), Metrics.Length.MILLI.toSI(hmm));
    assertThat(apparent / rho[0]).isCloseTo(predicted, byLessThan(0.001));
  }
}