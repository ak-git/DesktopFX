package com.ak.rsm.apparent;

import com.ak.rsm.relative.Layer2RelativeMedium;
import com.ak.rsm.resistance.NormalizedResistance2Layer;
import com.ak.rsm.resistance.TetrapolarResistance;
import com.ak.rsm.system.Layers;
import com.ak.rsm.system.RelativeTetrapolarSystem;
import com.ak.rsm.system.TetrapolarSystem;
import com.ak.util.Metrics;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import static java.lang.StrictMath.log;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.byLessThan;

class Apparent2RhoTest {
  @ParameterizedTest
  @MethodSource("com.ak.rsm.resistance.Resistance2LayerTest#twoLayerParameters")
  void testValueLog(@Nonnull double[] rho,
                    @Nonnegative double hmm, @Nonnegative double smm, @Nonnegative double lmm, @Nonnegative double rOhm) {
    double logApparent = log(TetrapolarResistance.ofMilli(smm, lmm).ofOhms(rOhm).resistivity()) - log(rho[0]);

    double logPredicted = Apparent2Rho.newLog1pApparentDivRho1(new RelativeTetrapolarSystem(lmm / smm))
        .applyAsDouble(new Layer2RelativeMedium(rho, hmm / smm));
    assertThat(logApparent).isCloseTo(logPredicted, byLessThan(0.001));

    double logPredicted2 = Apparent2Rho.newLog1pApparentDivRho1(new RelativeTetrapolarSystem(smm / lmm))
        .applyAsDouble(new Layer2RelativeMedium(rho, hmm / lmm));
    assertThat(logApparent).isCloseTo(logPredicted2, byLessThan(0.001));
  }

  @ParameterizedTest
  @MethodSource("com.ak.rsm.resistance.Resistance2LayerTest#twoLayerParameters")
  void testValueNormalized(@Nonnull double[] rho,
                           @Nonnegative double hmm, @Nonnegative double smm, @Nonnegative double lmm, @Nonnegative double rOhm) {
    double apparent = TetrapolarResistance.ofMilli(smm, lmm).ofOhms(rOhm).resistivity() / rho[0];

    double predicted = Apparent2Rho.newApparentDivRho1(new RelativeTetrapolarSystem(lmm / smm))
        .applyAsDouble(new Layer2RelativeMedium(rho, hmm / smm));
    assertThat(apparent).isCloseTo(predicted, byLessThan(0.001));

    double predicted2 = Apparent2Rho.newApparentDivRho1(new RelativeTetrapolarSystem(smm / lmm))
        .applyAsDouble(new Layer2RelativeMedium(rho, hmm / lmm));
    assertThat(apparent).isCloseTo(predicted2, byLessThan(0.001));
  }

  @ParameterizedTest
  @MethodSource("com.ak.rsm.resistance.Resistance2LayerTest#twoLayerParameters")
  void testValueLogVSNormalized(@Nonnull double[] rho,
                                @Nonnegative double hmm, @Nonnegative double smm, @Nonnegative double lmm, @Nonnegative double rOhm) {
    TetrapolarSystem system = new TetrapolarSystem(Metrics.fromMilli(smm), Metrics.fromMilli(lmm));
    double k = Layers.getK12(rho[0], rho[1]);
    double apparent1 = log(TetrapolarResistance.ofMilli(smm, lmm).ofOhms(
        new NormalizedResistance2Layer(system).applyAsDouble(k, Metrics.fromMilli(hmm))
    ).resistivity());
    double apparent2 = Apparent2Rho.newLog1pApparentDivRho1(system.relativeSystem()).applyAsDouble(new Layer2RelativeMedium(k, hmm / lmm));
    assertThat(apparent1).isCloseTo(apparent2, byLessThan(0.001));
  }
}