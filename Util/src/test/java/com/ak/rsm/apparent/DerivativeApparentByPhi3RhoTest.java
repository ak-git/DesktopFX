package com.ak.rsm.apparent;

import com.ak.rsm.resistance.TetrapolarResistance;
import com.ak.rsm.system.Layers;
import com.ak.rsm.system.TetrapolarSystem;
import com.ak.util.Metrics;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import javax.annotation.Nonnegative;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.byLessThan;
import static tech.units.indriya.unit.Units.METRE;

class DerivativeApparentByPhi3RhoTest {
  private static final int SCALE = 1;

  @ParameterizedTest
  @MethodSource("com.ak.rsm.resistance.Resistance2LayerTest#twoLayerParameters")
  void testValue2(double[] rho, @Nonnegative double hmm, @Nonnegative double smm, @Nonnegative double lmm) {
    TetrapolarSystem system = new TetrapolarSystem(Metrics.Length.MILLI.to(smm, METRE), Metrics.Length.MILLI.to(lmm, METRE));
    double hStep = Metrics.Length.MILLI.to(1, METRE);
    int p1 = (int) hmm;

    var b = TetrapolarResistance.of(system);
    double expected = (
        b.rho1(rho[0]).rho2(rho[1]).rho3(rho[1]).hStep(hStep / SCALE).p((p1 + 1) * SCALE, SCALE).resistivity() -
            b.rho1(rho[0]).rho2(rho[1]).rho3(rho[1]).hStep(hStep / SCALE).p(p1 * SCALE, SCALE).resistivity()
    ) / (hStep / system.lCC());
    expected /= rho[0];

    double actual = Apparent3Rho.newDerApparentByPhiDivRho1(system,
        new double[] {Layers.getK12(rho[0], rho[1]), 0.0}, hStep, p1, 1, hStep);
    assertThat(StrictMath.log(Math.abs(actual))).isCloseTo(StrictMath.log(Math.abs(expected)), byLessThan(0.1));
    assertThat(Math.signum(actual)).isCloseTo(Math.signum(expected), byLessThan(0.1));
  }

  @ParameterizedTest
  @MethodSource("com.ak.rsm.resistance.Resistance3LayerTest#threeLayerParameters")
  void testValue3(double[] rho, @Nonnegative double hStep, int[] p,
                  @Nonnegative double smm, @Nonnegative double lmm) {
    TetrapolarSystem system = new TetrapolarSystem(Metrics.Length.MILLI.to(smm, METRE), Metrics.Length.MILLI.to(lmm, METRE));

    var b = TetrapolarResistance.of(system);
    double expected = (
        b.rho1(rho[0]).rho2(rho[1]).rho3(rho[2]).hStep(hStep / SCALE).p((p[0] + 1) * SCALE, p[1] * SCALE).resistivity() -
            b.rho1(rho[0]).rho2(rho[1]).rho3(rho[2]).hStep(hStep / SCALE).p(p[0] * SCALE, p[1] * SCALE).resistivity()
    ) / (hStep / system.lCC());
    expected /= rho[0];

    double actual = Apparent3Rho.newDerApparentByPhiDivRho1(system,
        new double[] {Layers.getK12(rho[0], rho[1]), Layers.getK12(rho[1], rho[2])},
        hStep, p[0], p[1], hStep);
    assertThat(StrictMath.log(Math.abs(actual))).isCloseTo(StrictMath.log(Math.abs(expected)), byLessThan(0.1));
    assertThat(Math.signum(actual)).isCloseTo(Math.signum(expected), byLessThan(0.1));
  }
}