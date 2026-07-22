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

    private static void apparent(double smm, double lmm, double rOhm) {
      double apparentNor = Resistivity.of(ElectrodeSystem.builder(Metrics.Length.MILLI).tetrapolar(smm, lmm).build()).build().apparent(rOhm);
      double apparentInv = Resistivity.of(ElectrodeSystem.builder(Metrics.Length.MILLI).tetrapolar(lmm, smm).build()).build().apparent(rOhm);
      assertThat(apparentNor).isEqualTo(apparentInv);
    }

    @Nested
    class Layer3Absolute {
      @ParameterizedTest
      @CsvSource(delimiter = '|', textBlock = """
          8.0 | 8.0 | 1.0 |  5 | 5 | 10.0 | 20.0 | METRE | 7.288
          8.0 | 1.0 | 1.0 | 10 | 1 | 20.0 | 10.0 | MILLI | 7.288
          """)
      void apparent(double rho1, double rho2, double rho3, int p1mm, int p2mp1mm, double sPU, double lCC, Metrics.Length units, double expected) {
        ElectrodeSystem.Tetrapolar tetrapolar = ElectrodeSystem.builder(units).tetrapolar(sPU, lCC).build();
        Model layer3 = new Model.Layer3Absolute(rho1, rho2, rho3, units.toSI(1),
            new Model.P(p1mm, p2mp1mm), new Model.P(p1mm, p2mp1mm));
        double value = Resistivity.of(tetrapolar).apparent(layer3).value();
        assertThat(value).isCloseTo(expected, byLessThan(0.001));
      }

      @ParameterizedTest
      @MethodSource("com.ak.rsm.resistance.Resistance3LayerTest#threeLayerParameters")
      void apparent(double[] rho, double hStepSI, int[] p, double smm, double lmm, double rOhm) {
        Apparent.apparent(smm, lmm, rOhm);
        innerApparent(rho, hStepSI, p, smm, lmm);
        innerApparent(rho, hStepSI, p, smm, lmm, rOhm);
      }

      private static void innerApparent(double[] rho, double hStepSI, int[] ps, double smm, double lmm) {
        Model.P p = new Model.P(ps);
        Model layer3 = new Model.Layer3Absolute(rho[0], rho[1], rho[2], hStepSI, p, p);
        double predictedNor = Resistivity.of(ElectrodeSystem.builder(Metrics.Length.MILLI).tetrapolar(smm, lmm).build()).apparent(layer3).value();
        double predictedRev = Resistivity.of(ElectrodeSystem.builder(Metrics.Length.MILLI).tetrapolar(lmm, smm).build()).apparent(layer3).value();
        assertThat(predictedNor).isCloseTo(predictedRev, byLessThan(0.000_001));
      }

      private static void innerApparent(double[] rho, double hStepSI, int[] ps, double smm, double lmm, double rOhm) {
        ElectrodeSystem.Tetrapolar tetrapolar = ElectrodeSystem.builder(Metrics.Length.MILLI).tetrapolar(smm, lmm).build();
        double apparent = Resistivity.of(tetrapolar).build().apparent(rOhm);
        Model.P p = new Model.P(ps);
        Model layer3 = new Model.Layer3Absolute(rho[0], rho[1], rho[2], hStepSI, p, p);
        double predicted = Resistivity.of(tetrapolar).apparent(layer3).value();
        assertThat(apparent).isCloseTo(predicted, byLessThan(0.001));
      }
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
        double value = Resistivity.of(tetrapolar).apparent(layer2).value();
        assertThat(value).isCloseTo(expected, byLessThan(0.001));
      }

      @ParameterizedTest
      @MethodSource("com.ak.rsm.resistance.Resistance2LayerTest#twoLayerParameters")
      void apparent(double[] rho, double hmm, double smm, double lmm, double rOhm) {
        Apparent.apparent(smm, lmm, rOhm);
        apparentDivRho1(rho, hmm, smm, lmm);
        apparentDivRho1(rho, hmm, smm, lmm, rOhm);
      }

      private static void apparentDivRho1(double[] rho, double hmm, double smm, double lmm) {
        Model layer2 = new Model.Layer2Relative(K.of(rho[0], rho[1]), Metrics.Length.MILLI.toSI(hmm));
        double predictedNor = Resistivity.of(ElectrodeSystem.builder(Metrics.Length.MILLI).tetrapolar(smm, lmm).build()).apparent(layer2).value();
        double predictedRev = Resistivity.of(ElectrodeSystem.builder(Metrics.Length.MILLI).tetrapolar(lmm, smm).build()).apparent(layer2).value();
        assertThat(predictedNor).isCloseTo(predictedRev, byLessThan(0.000_001));
      }

      private static void apparentDivRho1(double[] rho, double hmm, double smm, double lmm, double rOhm) {
        ElectrodeSystem.Tetrapolar tetrapolar = ElectrodeSystem.builder(Metrics.Length.MILLI).tetrapolar(smm, lmm).build();
        double apparent = Resistivity.of(tetrapolar).build().apparent(rOhm);
        Model layer2 = new Model.Layer2Relative(K.of(rho[0], rho[1]), Metrics.Length.MILLI.toSI(hmm));
        double predicted = Resistivity.of(tetrapolar).apparent(layer2).value();
        assertThat(apparent / rho[0]).isCloseTo(predicted, byLessThan(0.001));
      }
    }

    @Nested
    class Layer3Absolute {
      @ParameterizedTest
      @CsvSource(delimiter = '|', textBlock = """
          8.0 | 8.0 | 1.0 |  5 | 5 | 10.0 | 20.0 | METRE | 7.288
          8.0 | 1.0 | 1.0 | 10 | 1 | 20.0 | 10.0 | MILLI | 7.288
          """)
      void apparent(double rho1, double rho2, double rho3, int p1mm, int p2mp1mm, double sPU, double lCC, Metrics.Length units, double expected) {
        ElectrodeSystem.Tetrapolar tetrapolar = ElectrodeSystem.builder(units).tetrapolar(sPU, lCC).build();
        Model layer3 = new Model.Layer3Absolute(rho1, rho2, rho3, units.toSI(1),
            new Model.P(p1mm, p2mp1mm), new Model.P(p1mm, p2mp1mm));
        double value = Resistivity.of(tetrapolar).apparent(layer3).value();
        assertThat(value).isCloseTo(expected, byLessThan(0.001));
      }

      @ParameterizedTest
      @MethodSource("com.ak.rsm.resistance.Resistance3LayerTest#threeLayerParameters")
      void apparent(double[] rho, double hStepSI, int[] p, double smm, double lmm, double rOhm) {
        Apparent.apparent(smm, lmm, rOhm);
        innerApparent(rho, hStepSI, p, smm, lmm);
        innerApparent(rho, hStepSI, p, smm, lmm, rOhm);
      }

      private static void innerApparent(double[] rho, double hStepSI, int[] ps, double smm, double lmm) {
        Model.P p = new Model.P(ps);
        Model layer3 = new Model.Layer3Absolute(rho[0], rho[1], rho[2], hStepSI, p, p);
        double predictedNor = Resistivity.of(ElectrodeSystem.builder(Metrics.Length.MILLI).tetrapolar(smm, lmm).build()).apparent(layer3).value();
        double predictedRev = Resistivity.of(ElectrodeSystem.builder(Metrics.Length.MILLI).tetrapolar(lmm, smm).build()).apparent(layer3).value();
        assertThat(predictedNor).isCloseTo(predictedRev, byLessThan(0.000_001));
      }

      private static void innerApparent(double[] rho, double hStepSI, int[] ps, double smm, double lmm, double rOhm) {
        ElectrodeSystem.Tetrapolar tetrapolar = ElectrodeSystem.builder(Metrics.Length.MILLI).tetrapolar(smm, lmm).build();
        double apparent = Resistivity.of(tetrapolar).build().apparent(rOhm);
        Model.P p = new Model.P(ps);
        Model layer3 = new Model.Layer3Absolute(rho[0], rho[1], rho[2], hStepSI, p, p);
        double predicted = Resistivity.of(tetrapolar).apparent(layer3).value();
        assertThat(apparent).isCloseTo(predicted, byLessThan(0.001));
      }
    }
  }

  @Nested
  class DerivativeApparentByPhiDivRho1 {
    @Nested
    class Layer2Relative {
      @ParameterizedTest
      @CsvSource(delimiter = '|', textBlock = """
          8.0 |  1.0 | 10.0 | 10.0 | 20.0 | METRE |  0.308
          8.0 |  1.0 | 10.0 | 20.0 | 10.0 | MILLI |  0.308
          2.0 | 10.0 |  3.0 |  6.0 | 18.0 | METRE | -9.609
          2.0 | 10.0 |  3.0 | 18.0 |  6.0 | MILLI | -9.609
          """)
      void derivativeDivRho1(double rho1, double rho2, double h, double sPU, double lCC, Metrics.Length units, double expected) {
        ElectrodeSystem.Tetrapolar tetrapolar = ElectrodeSystem.builder(units).tetrapolar(sPU, lCC).build();
        Model layer2 = new Model.Layer2Relative(K.of(rho1, rho2), units.toSI(h));
        double value = Resistivity.of(tetrapolar).apparent(layer2).derivative();
        assertThat(value).isCloseTo(expected, byLessThan(0.001));
      }

      @ParameterizedTest
      @MethodSource("com.ak.rsm.resistance.Resistance2LayerTest#twoLayerParameters")
      void derivativeDivRho1(double[] rho, double hmm, double smm, double lmm) {
        Model layer2 = new Model.Layer2Relative(K.of(rho[0], rho[1]), Metrics.Length.MILLI.toSI(hmm));
        double predictedNor = Resistivity.of(ElectrodeSystem.builder(Metrics.Length.MILLI).tetrapolar(smm, lmm).build())
            .apparent(layer2).derivative();
        double predictedRev = Resistivity.of(ElectrodeSystem.builder(Metrics.Length.MILLI).tetrapolar(lmm, smm).build())
            .apparent(layer2).derivative();
        assertThat(predictedNor).isCloseTo(predictedRev, byLessThan(0.000_001));
      }
    }
  }

  @Nested
  class DerivativeLogApparentByLogPhi {
    @Nested
    class Layer3Absolute {
      @ParameterizedTest
      @MethodSource("com.ak.rsm.resistance.Resistance3LayerTest#threeLayerParameters")
      void derivative(double[] rho, double hStepSI, int[] ps, double smm, double lmm, double rOhm) {
        Model.P p = new Model.P(ps);
        Model layer3 = new Model.Layer3Absolute(rho[0], rho[1], rho[2], hStepSI, p, p);
        double predictedNor = Resistivity.of(ElectrodeSystem.builder(Metrics.Length.MILLI).tetrapolar(smm, lmm).build())
            .apparent(layer3).derivative();
        double predictedRev = Resistivity.of(ElectrodeSystem.builder(Metrics.Length.MILLI).tetrapolar(lmm, smm).build())
            .apparent(layer3).derivative();
        assertThat(predictedNor).isCloseTo(predictedRev, byLessThan(0.000_001));
      }

      @ParameterizedTest
      @CsvSource(delimiter = '|', textBlock = """
          8.0 |  8.0 |  1.0 |  500 | 500 | 10.0 | 20.0 | METRE |  0.337
          8.0 |  8.0 |  1.0 |  500 | 500 | 20.0 | 10.0 | MILLI |  0.337
          8.0 |  1.0 |  1.0 | 1000 |   1 | 10.0 | 20.0 | METRE |  0.337
          8.0 |  1.0 |  1.0 | 1000 |   1 | 20.0 | 10.0 | MILLI |  0.337
          2.0 |  2.0 | 10.0 |  150 | 150 |  6.0 | 18.0 | METRE | -4.966
          2.0 | 10.0 | 10.0 |  300 |   1 | 18.0 |  6.0 | MILLI | -4.966
          """)
      void logDerivative(double rho1, double rho2, double rho3, int p1, int p2mp1, double sPU, double lCC, Metrics.Length units, double expected) {
        ElectrodeSystem.Tetrapolar tetrapolar = ElectrodeSystem.builder(units).tetrapolar(sPU, lCC).build();
        Model layer3 = new Model.Layer3Absolute(rho1, rho2, rho3, units.toSI(0.01),
            new Model.P(p1, p2mp1), new Model.P(p1 + 1, p2mp1));
        double value = Resistivity.of(tetrapolar).apparent(layer3).derivative();
        assertThat(value).isCloseTo(expected, byLessThan(0.001));
      }
    }
  }
}