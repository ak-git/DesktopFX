package com.ak.rsm2;

import com.ak.util.Metrics;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.security.SecureRandom;
import java.util.random.RandomGenerator;

import static org.assertj.core.api.Assertions.*;

class ResistivityTest {
  private static final RandomGenerator RANDOM = new SecureRandom();

  @Nested
  class Apparent {
    @ParameterizedTest
    @MethodSource("com.ak.rsm.resistance.Resistance2LayerTest#twoLayerParameters")
    void apparent(double[] ignoredRho, double ignoredHmm, double smm, double lmm, double rOhm) {
      double apparentNor = Resistivity.of(ElectrodeSystem.builder(Metrics.Length.MILLI).tetrapolar(smm, lmm).build()).apparent(rOhm);
      double apparentInv = Resistivity.of(ElectrodeSystem.builder(Metrics.Length.MILLI).tetrapolar(lmm, smm).build()).apparent(rOhm);
      assertThat(apparentNor).isFinite().isEqualTo(apparentInv);
    }

    private static void apparent(double smm, double lmm, double rOhm) {
      double apparentNor = Resistivity.of(ElectrodeSystem.builder(Metrics.Length.MILLI).tetrapolar(smm, lmm).build()).apparent(rOhm);
      double apparentInv = Resistivity.of(ElectrodeSystem.builder(Metrics.Length.MILLI).tetrapolar(lmm, smm).build()).apparent(rOhm);
      assertThat(apparentNor).isFinite().isEqualTo(apparentInv);
    }

    @Nested
    class Layer2Absolute {
      @ParameterizedTest
      @CsvSource(delimiter = '|', textBlock = """
          8.0 | 1.0 | 10.0 | 10.0 | 20.0 | METRE | 7.289
          8.0 | 1.0 | 10.0 | 20.0 | 10.0 | MILLI | 7.289
          """)
      void apparent(double rho1, double rho2, double h, double sPU, double lCC, Metrics.Length units, double expected) {
        ElectrodeSystem.Tetrapolar tetrapolar = ElectrodeSystem.builder(units).tetrapolar(sPU, lCC).build();
        Model layer2 = new Model.Layer2Absolute(rho1, rho2, units.toSI(h));
        double value = Resistivity.of(tetrapolar).apparent(layer2).value();
        assertThat(value).isCloseTo(expected, byLessThan(0.001));
      }

      @ParameterizedTest
      @MethodSource("com.ak.rsm.resistance.Resistance2LayerTest#twoLayerParameters" )
      void apparent(double[] rho, double hmm, double smm, double lmm, double rOhm) {
        Apparent.apparent(smm, lmm, rOhm);
        innerApparent(rho, hmm, smm, lmm);
        innerApparent(rho, hmm, smm, lmm, rOhm);
      }

      private static void innerApparent(double[] rho, double hmm, double smm, double lmm) {
        Model layer2 = new Model.Layer2Absolute(rho[0], rho[1], Metrics.Length.MILLI.toSI(hmm));
        double predictedNor = Resistivity.of(ElectrodeSystem.builder(Metrics.Length.MILLI).tetrapolar(smm, lmm).build()).apparent(layer2).value();
        double predictedRev = Resistivity.of(ElectrodeSystem.builder(Metrics.Length.MILLI).tetrapolar(lmm, smm).build()).apparent(layer2).value();
        assertThat(predictedNor).isFinite().isCloseTo(predictedRev, byLessThan(0.000_001));
      }

      private static void innerApparent(double[] rho, double hmm, double smm, double lmm, double rOhm) {
        ElectrodeSystem.Tetrapolar tetrapolar = ElectrodeSystem.builder(Metrics.Length.MILLI).tetrapolar(smm, lmm).build();
        double apparent = Resistivity.of(tetrapolar).apparent(rOhm);
        Model layer2 = new Model.Layer2Absolute(rho[0], rho[1], Metrics.Length.MILLI.toSI(hmm));
        double predicted = Resistivity.of(tetrapolar).apparent(layer2).value();
        assertThat(apparent).isCloseTo(predicted, byLessThan(0.001));
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
        Model layer3 = new Model.Layer3Absolute(rho1, rho2, rho3, units.toSI(1), new Model.P(p1mm, p2mp1mm));
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
        Model layer3 = new Model.Layer3Absolute(rho[0], rho[1], rho[2], hStepSI, p);
        double predictedNor = Resistivity.of(ElectrodeSystem.builder(Metrics.Length.MILLI).tetrapolar(smm, lmm).build()).apparent(layer3).value();
        double predictedRev = Resistivity.of(ElectrodeSystem.builder(Metrics.Length.MILLI).tetrapolar(lmm, smm).build()).apparent(layer3).value();
        assertThat(predictedNor).isFinite().isCloseTo(predictedRev, byLessThan(0.000_001));
      }

      private static void innerApparent(double[] rho, double hStepSI, int[] ps, double smm, double lmm, double rOhm) {
        ElectrodeSystem.Tetrapolar tetrapolar = ElectrodeSystem.builder(Metrics.Length.MILLI).tetrapolar(smm, lmm).build();
        double apparent = Resistivity.of(tetrapolar).apparent(rOhm);
        Model.P p = new Model.P(ps);
        Model layer3 = new Model.Layer3Absolute(rho[0], rho[1], rho[2], hStepSI, p);
        double predicted = Resistivity.of(tetrapolar).apparent(layer3).value();
        assertThat(apparent).isCloseTo(predicted, byLessThan(0.001));
      }
    }

    @Nested
    class Layer3AbsoluteDhDRho {
      @ParameterizedTest
      @CsvSource(delimiter = '|', textBlock = """
          8.0 | 8.0 | 1.0 |  5 | 5 | 10.0 | 20.0 | METRE
          8.0 | 1.0 | 1.0 | 10 | 1 | 20.0 | 10.0 | MILLI
          """)
      void apparent(double rho1, double rho2, double rho3, int p1mm, int p2mp1mm, double sPU, double lCC, Metrics.Length units) {
        ElectrodeSystem.Tetrapolar tetrapolar = ElectrodeSystem.builder(units).tetrapolar(sPU, lCC).build();
        Model layer3 = new Model.Layer3AbsoluteDhDRho(
            new Model.Layer3Absolute(rho1, rho2, rho3, units.toSI(1), new Model.P(p1mm, p2mp1mm)),
            new Model.P(p1mm, p2mp1mm), RANDOM.nextDouble(1.0), RANDOM.nextDouble(1.0)
        );
        assertThatIllegalArgumentException().isThrownBy(() -> Resistivity.of(tetrapolar).apparent(layer3));
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
        assertThat(predictedNor).isFinite().isCloseTo(predictedRev, byLessThan(0.000_001));
      }

      private static void apparentDivRho1(double[] rho, double hmm, double smm, double lmm, double rOhm) {
        ElectrodeSystem.Tetrapolar tetrapolar = ElectrodeSystem.builder(Metrics.Length.MILLI).tetrapolar(smm, lmm).build();
        double apparent = Resistivity.of(tetrapolar).apparent(rOhm);
        Model layer2 = new Model.Layer2Relative(K.of(rho[0], rho[1]), Metrics.Length.MILLI.toSI(hmm));
        double predicted = Resistivity.of(tetrapolar).apparent(layer2).value();
        assertThat(apparent / rho[0]).isCloseTo(predicted, byLessThan(0.001));
      }
    }

    @Nested
    class Layer3Relative {
      @ParameterizedTest
      @CsvSource(delimiter = '|', textBlock = """
          8.0 | 8.0 | 1.0 |  5 | 5 | 10.0 | 20.0 | METRE | 0.911
          8.0 | 1.0 | 1.0 | 10 | 1 | 20.0 | 10.0 | MILLI | 0.911
          """)
      void apparent(double rho1, double rho2, double rho3, int p1mm, int p2mp1mm, double sPU, double lCC, Metrics.Length units, double expected) {
        ElectrodeSystem.Tetrapolar tetrapolar = ElectrodeSystem.builder(units).tetrapolar(sPU, lCC).build();
        Model layer3 = new Model.Layer3Relative(K.of(rho1, rho2), K.of(rho2, rho3), units.toSI(1), new Model.P(p1mm, p2mp1mm));
        double value = Resistivity.of(tetrapolar).apparent(layer3).value();
        assertThat(value).isCloseTo(expected, byLessThan(0.001));
        double derivative = Resistivity.of(tetrapolar).apparent(layer3).derivative();
        assertThat(derivative).isNaN();
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
        assertThat(predictedNor).isFinite().isCloseTo(predictedRev, byLessThan(0.000_001));
      }
    }

    @Nested
    class Layer2RelativeDh {
      @ParameterizedTest
      @CsvSource(delimiter = '|', textBlock = """
          8.0 |  1.0 | 10.0 | 10.0 | 20.0 | METRE |  0.308
          8.0 |  1.0 | 10.0 | 20.0 | 10.0 | MILLI |  0.308
          2.0 | 10.0 |  3.0 |  6.0 | 18.0 | METRE | -9.609
          2.0 | 10.0 |  3.0 | 18.0 |  6.0 | MILLI | -9.609
          """)
      void derivativeDivRho1(double rho1, double rho2, double h, double sPU, double lCC, Metrics.Length units, double expected) {
        ElectrodeSystem.Tetrapolar tetrapolar = ElectrodeSystem.builder(units).tetrapolar(sPU, lCC).build();
        Model layer2 = new Model.Layer2RelativeDh(K.of(rho1, rho2), units.toSI(h), RANDOM.nextDouble());
        double value = Resistivity.of(tetrapolar).apparent(layer2).derivative();
        assertThat(value).isCloseTo(expected, byLessThan(0.001));
      }

      @ParameterizedTest
      @MethodSource("com.ak.rsm.resistance.Resistance2LayerTest#twoLayerParameters")
      void derivativeDivRho1(double[] rho, double hmm, double smm, double lmm) {
        Model layer2 = new Model.Layer2RelativeDh(K.of(rho[0], rho[1]), Metrics.Length.MILLI.toSI(hmm), RANDOM.nextDouble());
        double predictedNor = Resistivity.of(ElectrodeSystem.builder(Metrics.Length.MILLI).tetrapolar(smm, lmm).build())
            .apparent(layer2).derivative();
        double predictedRev = Resistivity.of(ElectrodeSystem.builder(Metrics.Length.MILLI).tetrapolar(lmm, smm).build())
            .apparent(layer2).derivative();
        assertThat(predictedNor).isFinite().isCloseTo(predictedRev, byLessThan(0.000_001));
      }
    }

    @Nested
    class Layer3RelativeDh {
      @ParameterizedTest
      @CsvSource(delimiter = '|', textBlock = """
          8.0 | 8.0 | 1.0 |  500 | 500 | 10.0 | 20.0 | METRE | 0.308
          8.0 | 1.0 | 1.0 | 1000 | 500 | 20.0 | 10.0 | MILLI | 0.154
          """)
      void derivativeDivRho1(double rho1, double rho2, double rho3, int p1mm, int p2mp1mm, double sPU, double lCC, Metrics.Length units, double expected) {
        ElectrodeSystem.Tetrapolar tetrapolar = ElectrodeSystem.builder(units).tetrapolar(sPU, lCC).build();
        Model layer3 = new Model.Layer3RelativeDh(
            new Model.Layer3Relative(K.of(rho1, rho2), K.of(rho2, rho3), units.toSI(0.01), new Model.P(p1mm, p2mp1mm)),
            new Model.P(1, 1));
        double value = Resistivity.of(tetrapolar).apparent(layer3).derivative();
        assertThat(value).isCloseTo(expected, byLessThan(0.001));
      }

      @ParameterizedTest
      @MethodSource("com.ak.rsm.resistance.Resistance3LayerTest#threeLayerParameters")
      void derivativeDivRho1(double[] rho, double hStepSI, int[] ps, double smm, double lmm) {
        Model.P p = new Model.P(ps);
        Model layer3 = new Model.Layer3RelativeDh(new Model.Layer3Relative(K.of(rho[0], rho[1]), K.of(rho[1], rho[2]), hStepSI, p),
            new Model.P(1, 2));
        double predictedNor = Resistivity.of(ElectrodeSystem.builder(Metrics.Length.MILLI).tetrapolar(smm, lmm).build())
            .apparent(layer3).derivative();
        double predictedRev = Resistivity.of(ElectrodeSystem.builder(Metrics.Length.MILLI).tetrapolar(lmm, smm).build())
            .apparent(layer3).derivative();
        assertThat(predictedNor).isFinite().isCloseTo(predictedRev, byLessThan(0.000_001));
      }
    }
  }

  @Nested
  class DerivativeApparentByPhi {
    @Nested
    class Layer2Absolute {
      @ParameterizedTest
      @CsvSource(delimiter = '|', textBlock = """
          8.0 |  1.0 | 10.0 | 10.0 | 20.0 | METRE |  2.464
          8.0 |  1.0 | 10.0 | 20.0 | 10.0 | MILLI |  2.464
          2.0 | 10.0 |  3.0 |  6.0 | 18.0 | METRE | -19.219
          2.0 | 10.0 |  3.0 | 18.0 |  6.0 | MILLI | -19.219
          """)
      void derivative(double rho1, double rho2, double h, double sPU, double lCC, Metrics.Length units, double expected) {
        ElectrodeSystem.Tetrapolar tetrapolar = ElectrodeSystem.builder(units).tetrapolar(sPU, lCC).build();
        Model layer2 = new Model.Layer2Absolute(rho1, rho2, units.toSI(h));
        double value = Resistivity.of(tetrapolar).apparent(layer2).derivative();
        assertThat(value).isCloseTo(expected, byLessThan(0.001));
      }

      @ParameterizedTest
      @MethodSource("com.ak.rsm.resistance.Resistance2LayerTest#twoLayerParameters" )
      void derivative(double[] rho, double hmm, double smm, double lmm) {
        Model layer2 = new Model.Layer2Absolute(rho[0], rho[1], Metrics.Length.MILLI.toSI(hmm));
        double predictedNor = Resistivity.of(ElectrodeSystem.builder(Metrics.Length.MILLI).tetrapolar(smm, lmm).build())
            .apparent(layer2).derivative();
        double predictedRev = Resistivity.of(ElectrodeSystem.builder(Metrics.Length.MILLI).tetrapolar(lmm, smm).build())
            .apparent(layer2).derivative();
        assertThat(predictedNor).isCloseTo(predictedRev, byLessThan(0.000_001));
      }
    }

    @Nested
    class Layer2AbsoluteDh {
      @ParameterizedTest
      @CsvSource(delimiter = '|', textBlock = """
          8.0 |  1.0 | 10.0 | 10.0 | 20.0 | METRE |  2.464
          8.0 |  1.0 | 10.0 | 20.0 | 10.0 | MILLI |  2.464
          2.0 | 10.0 |  3.0 |  6.0 | 18.0 | METRE | -19.219
          2.0 | 10.0 |  3.0 | 18.0 |  6.0 | MILLI | -19.219
          """)
      void derivative(double rho1, double rho2, double h, double sPU, double lCC, Metrics.Length units, double expected) {
        ElectrodeSystem.Tetrapolar tetrapolar = ElectrodeSystem.builder(units).tetrapolar(sPU, lCC).build();
        Model layer2 = new Model.Layer2AbsoluteDh(rho1, rho2, units.toSI(h), RANDOM.nextDouble());
        double value = Resistivity.of(tetrapolar).apparent(layer2).derivative();
        assertThat(value).isCloseTo(expected, byLessThan(0.001));
      }

      @ParameterizedTest
      @MethodSource("com.ak.rsm.resistance.Resistance2LayerTest#twoLayerParameters")
      void derivative(double[] rho, double hmm, double smm, double lmm) {
        Model layer2 = new Model.Layer2AbsoluteDh(rho[0], rho[1], Metrics.Length.MILLI.toSI(hmm), RANDOM.nextDouble());
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
      void derivative(double[] rho, double hStepSI, int[] ps, double smm, double lmm) {
        Model.P p = new Model.P(ps);
        Model layer3 = new Model.Layer3Absolute(rho[0], rho[1], rho[2], hStepSI, p);
        assertThat(Resistivity.of(ElectrodeSystem.builder(Metrics.Length.MILLI).tetrapolar(smm, lmm).build())
            .apparent(layer3).derivative()).isNaN();
      }
    }
  }
}