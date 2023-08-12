package com.ak.rsm.apparent;

import com.ak.rsm.relative.RelativeMediumLayers;
import com.ak.rsm.resistance.TetrapolarDerivativeResistance;
import com.ak.rsm.system.TetrapolarSystem;
import com.ak.util.Metrics;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.byLessThan;

class DerivativeApparentByPhi2RhoTest {
  @ParameterizedTest
  @MethodSource("com.ak.rsm.resistance.Resistance2LayerTest#twoLayerParameters")
  void testValueSL(@Nonnull double[] rho, @Nonnegative double hmm, @Nonnegative double smm, @Nonnegative double lmm) {
    TetrapolarSystem system = new TetrapolarSystem(Metrics.fromMilli(smm), Metrics.fromMilli(lmm));
    double h = Metrics.fromMilli(hmm);
    double dh = Metrics.fromMilli(-0.00001);
    double expected = TetrapolarDerivativeResistance.of(system).dh(dh).rho1(rho[0]).rho2(rho[1]).h(h).derivativeResistivity() / rho[0];
    double actual = Apparent2Rho.newDerApparentByPhiDivRho1(system, Double.NaN)
        .applyAsDouble(new RelativeMediumLayers(rho, hmm / lmm));
    double actual2 = Apparent2Rho.newDerApparentByPhiDivRho1(system, dh)
        .applyAsDouble(new RelativeMediumLayers(rho, hmm / lmm));
    assertThat(actual).isCloseTo(expected, byLessThan(0.1));
    assertThat(actual2).isCloseTo(expected, byLessThan(0.1));
  }

  @ParameterizedTest
  @MethodSource("com.ak.rsm.resistance.Resistance2LayerTest#twoLayerParameters")
  void testValueLS(@Nonnull double[] rho, @Nonnegative double hmm, @Nonnegative double smm, @Nonnegative double lmm) {
    TetrapolarSystem system = new TetrapolarSystem(Metrics.fromMilli(lmm), Metrics.fromMilli(smm));
    double h = Metrics.fromMilli(hmm);
    double dh = Metrics.fromMilli(-0.00001);
    double expected = TetrapolarDerivativeResistance.of(system).dh(dh).rho1(rho[0]).rho2(rho[1]).h(h).derivativeResistivity() / rho[0];
    double actual = Apparent2Rho.newDerApparentByPhiDivRho1(system, Double.NaN)
        .applyAsDouble(new RelativeMediumLayers(rho, hmm / smm));
    double actual2 = Apparent2Rho.newDerApparentByPhiDivRho1(system, dh)
        .applyAsDouble(new RelativeMediumLayers(rho, hmm / smm));
    assertThat(actual).isCloseTo(expected, byLessThan(0.1));
    assertThat(actual2).isCloseTo(expected, byLessThan(0.1));
  }
}