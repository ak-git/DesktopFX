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
  class Apparent {
    @ParameterizedTest
    @MethodSource("com.ak.rsm.resistance.Resistance2LayerTest#twoLayerParameters")
    void apparent(double[] rho, double hmm, double smm, double lmm, double rOhm) {
      double apparentNor = Resistivity.of(ElectrodeSystem.builder(Metrics.Length.MILLI).tetrapolar(smm, lmm).build()).build().apparent(rOhm);
      double apparentInv = Resistivity.of(ElectrodeSystem.builder(Metrics.Length.MILLI).tetrapolar(lmm, smm).build()).build().apparent(rOhm);
      assertThat(apparentNor).isEqualTo(apparentInv);
    }
  }

  @Nested
  class ApparentDivRho1 {
    @Nested
    class Layer2Relative {
      @ParameterizedTest
      @CsvSource(delimiter = '|', textBlock = """
          8.0 | 1.0 | 10.0 | 10.0 | 20.0 | METRE | 0.911
          8.0 | 1.0 | 10.0 | 20.0 | 10.0 | MILLI | 0.911
          """)
      void apparentDivRho1(double rho1, double rho2, double h, double sPU, double lCC, Metrics.Length units, double expected) {
        ElectrodeSystem.Tetrapolar tetrapolar = ElectrodeSystem.builder(units).tetrapolar(sPU, lCC).build();
        Model layer2 = new Model.Layer2Relative(K.of(rho1, rho2), units.toSI(h));
        double value = Resistivity.of(tetrapolar).apparentDivRho1(layer2).value();
        assertThat(value).isCloseTo(expected, byLessThan(0.001));
      }

      @ParameterizedTest
      @MethodSource("com.ak.rsm.resistance.Resistance2LayerTest#twoLayerParameters")
      void apparent(double[] rho, double hmm, double smm, double lmm, double rOhm) {
        ApparentDivRho1.apparent(smm, lmm, rOhm);
        apparentDivRho1(rho, hmm, smm, lmm);
        apparentDivRho1(rho, hmm, smm, lmm, rOhm);
      }

      private static void apparentDivRho1(double[] rho, double hmm, double smm, double lmm) {
        Model layer2 = new Model.Layer2Relative(K.of(rho[0], rho[1]), Metrics.Length.MILLI.toSI(hmm));
        double predictedNor = Resistivity.of(ElectrodeSystem.builder(Metrics.Length.MILLI).tetrapolar(smm, lmm).build()).apparentDivRho1(layer2).value();
        double predictedRev = Resistivity.of(ElectrodeSystem.builder(Metrics.Length.MILLI).tetrapolar(lmm, smm).build()).apparentDivRho1(layer2).value();
        assertThat(predictedNor).isCloseTo(predictedRev, byLessThan(0.000_001));
      }

      private static void apparentDivRho1(double[] rho, double hmm, double smm, double lmm, double rOhm) {
        ElectrodeSystem.Tetrapolar tetrapolar = ElectrodeSystem.builder(Metrics.Length.MILLI).tetrapolar(smm, lmm).build();
        double apparent = Resistivity.of(tetrapolar).build().apparent(rOhm);
        Model layer2 = new Model.Layer2Relative(K.of(rho[0], rho[1]), Metrics.Length.MILLI.toSI(hmm));
        double predicted = Resistivity.of(tetrapolar).apparentDivRho1(layer2).value();
        assertThat(apparent / rho[0]).isCloseTo(predicted, byLessThan(0.001));
      }
    }

    @Nested
    class Layer3Relative {
      @ParameterizedTest
      @MethodSource("com.ak.rsm.resistance.Resistance3LayerTest#threeLayerParameters")
      void apparent(double[] rho, double hStepSI, int[] p, double smm, double lmm, double rOhm) {
        ApparentDivRho1.apparent(smm, lmm, rOhm);
        apparentDivRho1(rho, hStepSI, p, smm, lmm);
        apparentDivRho1(rho, hStepSI, p, smm, lmm, rOhm);
      }

      private static void apparentDivRho1(double[] rho, double hStepSI, int[] p, double smm, double lmm) {
        Model layer3 = new Model.Layer3Relative(K.of(rho[0], rho[1]), K.of(rho[1], rho[2]), hStepSI, p[0], p[1]);
        double predictedNor = Resistivity.of(ElectrodeSystem.builder(Metrics.Length.MILLI).tetrapolar(smm, lmm).build()).apparentDivRho1(layer3).value();
        double predictedRev = Resistivity.of(ElectrodeSystem.builder(Metrics.Length.MILLI).tetrapolar(lmm, smm).build()).apparentDivRho1(layer3).value();
        assertThat(predictedNor).isCloseTo(predictedRev, byLessThan(0.000_001));
      }

      private static void apparentDivRho1(double[] rho, double hStepSI, int[] p, double smm, double lmm, double rOhm) {
        ElectrodeSystem.Tetrapolar tetrapolar = ElectrodeSystem.builder(Metrics.Length.MILLI).tetrapolar(smm, lmm).build();
        double apparent = Resistivity.of(tetrapolar).build().apparent(rOhm);
        Model layer3 = new Model.Layer3Relative(K.of(rho[0], rho[1]), K.of(rho[1], rho[2]), hStepSI, p[0], p[1]);
        double predicted = Resistivity.of(tetrapolar).apparentDivRho1(layer3).value();
        assertThat(apparent / rho[0]).isCloseTo(predicted, byLessThan(0.001));
      }
    }

    private static void apparent(double smm, double lmm, double rOhm) {
      double apparentNor = Resistivity.of(ElectrodeSystem.builder(Metrics.Length.MILLI).tetrapolar(smm, lmm).build()).build().apparent(rOhm);
      double apparentInv = Resistivity.of(ElectrodeSystem.builder(Metrics.Length.MILLI).tetrapolar(lmm, smm).build()).build().apparent(rOhm);
      assertThat(apparentNor).isEqualTo(apparentInv);
    }
  }

  @Nested
  class DerivativeApparentByPhoDivRho1 {
    @Nested
    class Layer2Relative {
      @ParameterizedTest
      @CsvSource(delimiter = '|', textBlock = """
        8.0 |  1.0 | 10.0 | 10.0 | 20.0 | METRE |  0.308
        8.0 |  1.0 | 10.0 | 20.0 | 10.0 | MILLI |  0.308
        2.0 | 10.0 |  3.0 |  6.0 | 18.0 | METRE | -9.609
        2.0 | 10.0 |  3.0 | 18.0 |  6.0 | MILLI | -9.609
        """)
      void apparentDivRho1(double rho1, double rho2, double h, double sPU, double lCC, Metrics.Length units, double expected) {
        ElectrodeSystem.Tetrapolar tetrapolar = ElectrodeSystem.builder(units).tetrapolar(sPU, lCC).build();
        Model layer2 = new Model.Layer2Relative(K.of(rho1, rho2), units.toSI(h));
        double value = Resistivity.of(tetrapolar).apparentDivRho1(layer2).derivativeByPhi();
        assertThat(value).isCloseTo(expected, byLessThan(0.001));
      }

      @ParameterizedTest
      @MethodSource("com.ak.rsm.resistance.Resistance2LayerTest#twoLayerParameters")
      void apparentDivRho1(double[] rho, double hmm, double smm, double lmm) {
        Model layer2 = new Model.Layer2Relative(K.of(rho[0], rho[1]), Metrics.Length.MILLI.toSI(hmm));
        double predictedNor = Resistivity.of(ElectrodeSystem.builder(Metrics.Length.MILLI).tetrapolar(smm, lmm).build())
            .apparentDivRho1(layer2).derivativeByPhi();
        double predictedRev = Resistivity.of(ElectrodeSystem.builder(Metrics.Length.MILLI).tetrapolar(lmm, smm).build())
            .apparentDivRho1(layer2).derivativeByPhi();
        assertThat(predictedNor).isCloseTo(predictedRev, byLessThan(0.000_001));
      }
    }

    @Nested
    class Layer3Relative {
      @ParameterizedTest
      @MethodSource("com.ak.rsm.resistance.Resistance3LayerTest#threeLayerParameters")
      void apparentDivRho1(double[] rho, double hStepSI, int[] p, double smm, double lmm, double rOhm) {
        Model layer3 = new Model.Layer3Relative(K.of(rho[0], rho[1]), K.of(rho[1], rho[2]), hStepSI, p[0], p[1]);
        double predictedNor = Resistivity.of(ElectrodeSystem.builder(Metrics.Length.MILLI).tetrapolar(smm, lmm).build())
            .apparentDivRho1(layer3).derivativeByPhi();
        double predictedRev = Resistivity.of(ElectrodeSystem.builder(Metrics.Length.MILLI).tetrapolar(lmm, smm).build())
            .apparentDivRho1(layer3).derivativeByPhi();
        assertThat(predictedNor).isCloseTo(predictedRev, byLessThan(0.000_001));
      }
    }
  }
}