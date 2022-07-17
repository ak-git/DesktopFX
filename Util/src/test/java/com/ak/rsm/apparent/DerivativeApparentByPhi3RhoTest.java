package com.ak.rsm.apparent;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.rsm.resistance.TetrapolarDerivativeResistance;
import com.ak.rsm.system.Layers;
import com.ak.rsm.system.TetrapolarSystem;
import com.ak.util.Metrics;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.byLessThan;

class DerivativeApparentByPhi3RhoTest {
  @ParameterizedTest
  @MethodSource("com.ak.rsm.resistance.Resistance2LayerTest#twoLayerParameters")
  void testValue2(@Nonnull double[] rho, @Nonnegative double hmm, @Nonnegative double smm, @Nonnegative double lmm) {
    TetrapolarSystem system = new TetrapolarSystem(Metrics.fromMilli(smm), Metrics.fromMilli(lmm));
    double hStep = Metrics.fromMilli(1);
    int p1 = (int) hmm;
    double expected = TetrapolarDerivativeResistance.of(system).dh(hStep).rho1(rho[0]).rho2(rho[1]).rho3(rho[1]).hStep(hStep).p(p1, 1)
        .derivativeResistivity() / rho[0];
    double actual = Apparent3Rho.newDerivativeApparentByPhi2Rho(system,
        new double[] {Layers.getK12(rho[0], rho[1]), 0.0}, hStep, p1, 1);
    assertThat(StrictMath.log(Math.abs(actual))).isCloseTo(StrictMath.log(Math.abs(expected)), byLessThan(0.1));
    assertThat(Math.signum(actual)).isCloseTo(Math.signum(expected), byLessThan(0.1));
  }

  @ParameterizedTest
  @MethodSource("com.ak.rsm.resistance.Resistance3LayerTest#threeLayerParameters")
  void testValue3(@Nonnull double[] rho, @Nonnegative double h, @Nonnull int[] p,
                  @Nonnegative double smm, @Nonnegative double lmm) {
    TetrapolarSystem system = new TetrapolarSystem(Metrics.fromMilli(smm), Metrics.fromMilli(lmm));
    double expected = TetrapolarDerivativeResistance.of(system).dh(h).rho1(rho[0]).rho2(rho[1]).rho3(rho[2]).hStep(h).p(p[0], p[1])
        .derivativeResistivity() / rho[0];

    double actual = Apparent3Rho.newDerivativeApparentByPhi2Rho(system,
        new double[] {Layers.getK12(rho[0], rho[1]), Layers.getK12(rho[1], rho[2])},
        h, p[0], p[1]);
    assertThat(StrictMath.log(Math.abs(actual))).isCloseTo(StrictMath.log(Math.abs(expected)), byLessThan(0.1));
    assertThat(Math.signum(actual)).isCloseTo(Math.signum(expected), byLessThan(0.1));
  }
}