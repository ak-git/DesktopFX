package com.ak.rsm2;

import com.ak.rsm.resistance.TetrapolarResistance;
import com.ak.util.Metrics;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.byLessThan;

class ResistivityTest {
  @Test
  void of() {
    ElectrodeSystem.Tetrapolar tetrapolar = ElectrodeSystem.ofMilli().tetrapolar(10.0, 20.0).build();
    double value = Resistivity.normalizedByRho1(tetrapolar).value(K.of(8.0, 1.0).value(), 0.01);
    assertThat(value).isCloseTo(0.911, byLessThan(0.001));
  }

  @Test
  void ofInv() {
    ElectrodeSystem.Tetrapolar tetrapolar = ElectrodeSystem.ofMilli().tetrapolar(20.0, 10.0).build();
    double value = Resistivity.normalizedByRho1(tetrapolar).value(K.of(8.0, 1.0).value(), 0.01);
    assertThat(value).isCloseTo(0.911, byLessThan(0.001));
  }

  @ParameterizedTest
  @MethodSource("com.ak.rsm.resistance.Resistance2LayerTest#twoLayerParameters")
  void valueNormalized(double[] rho, double hmm, double smm, double lmm, double rOhm) {
    double apparent = TetrapolarResistance.ofMilli(smm, lmm).ofOhms(rOhm).resistivity() / rho[0];
    double predictedNor = Resistivity.normalizedByRho1(ElectrodeSystem.ofMilli().tetrapolar(smm, lmm).build())
        .value(K.of(rho[0], rho[1]).value(), Metrics.Length.MILLI.toSI(hmm));
    double predictedRev = Resistivity.normalizedByRho1(ElectrodeSystem.ofMilli().tetrapolar(lmm, smm).build())
        .value(K.of(rho[0], rho[1]).value(), Metrics.Length.MILLI.toSI(hmm));
    assertThat(apparent).isCloseTo(predictedNor, byLessThan(0.001)).isCloseTo(predictedRev, byLessThan(0.001));
  }
}