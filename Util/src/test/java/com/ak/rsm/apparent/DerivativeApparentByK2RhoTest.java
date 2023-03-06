package com.ak.rsm.apparent;

import com.ak.rsm.relative.Layer2RelativeMedium;
import com.ak.rsm.resistance.TetrapolarResistance;
import com.ak.rsm.system.Layers;
import com.ak.rsm.system.RelativeTetrapolarSystem;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.byLessThan;

class DerivativeApparentByK2RhoTest {
  @ParameterizedTest
  @MethodSource("com.ak.rsm.resistance.Resistance2LayerTest#twoLayerParameters")
  void testValueSL(@Nonnull double[] rho, @Nonnegative double hmm, @Nonnegative double smm, @Nonnegative double lmm) {
    if (Double.compare(rho[0], rho[1]) != 0) {
      double k12 = Layers.getK12(rho[0], rho[1]);
      double dk = 0.00001;
      var b = TetrapolarResistance.ofMilli(smm, lmm);
      double expected = (
          b.rho1(rho[0]).rho2(rho[0] / Layers.getRho1ToRho2(k12)).h(hmm).resistivity() -
              b.rho1(rho[0]).rho2(rho[0] / Layers.getRho1ToRho2(k12 - dk)).h(hmm).resistivity()
      ) / dk;
      expected /= rho[0];
      double actual = Apparent2Rho.newDerivativeApparentByKDivRho1(new RelativeTetrapolarSystem(smm / lmm))
          .applyAsDouble(new Layer2RelativeMedium(k12, hmm / lmm));
      assertThat(actual).isCloseTo(expected, byLessThan(0.1));
    }
  }

  @ParameterizedTest
  @MethodSource("com.ak.rsm.resistance.Resistance2LayerTest#twoLayerParameters")
  void testValueLS(@Nonnull double[] rho, @Nonnegative double hmm, @Nonnegative double smm, @Nonnegative double lmm) {
    if (Double.compare(rho[0], rho[1]) != 0) {
      double k12 = Layers.getK12(rho[0], rho[1]);
      double dk = 0.00001;
      var b = TetrapolarResistance.ofMilli(lmm, smm);
      double expected = (
          b.rho1(rho[0]).rho2(rho[0] / Layers.getRho1ToRho2(k12)).h(hmm).resistivity() -
              b.rho1(rho[0]).rho2(rho[0] / Layers.getRho1ToRho2(k12 - dk)).h(hmm).resistivity()
      ) / dk;
      expected /= rho[0];
      double actual = Apparent2Rho.newDerivativeApparentByKDivRho1(new RelativeTetrapolarSystem(lmm / smm))
          .applyAsDouble(new Layer2RelativeMedium(k12, hmm / smm));
      assertThat(actual).isCloseTo(expected, byLessThan(0.1));
    }
  }
}