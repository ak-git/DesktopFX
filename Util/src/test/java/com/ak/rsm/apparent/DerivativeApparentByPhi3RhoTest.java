package com.ak.rsm.apparent;

import com.ak.rsm.resistance.DeltaH;
import com.ak.rsm.resistance.TetrapolarDerivativeResistance;
import com.ak.rsm.resistance.TetrapolarResistance;
import com.ak.rsm.system.Layers;
import com.ak.rsm.system.TetrapolarSystem;
import com.ak.util.Metrics;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.byLessThan;
import static tech.units.indriya.unit.Units.METRE;

class DerivativeApparentByPhi3RhoTest {
  private static final int SCALE = 1;

  @ParameterizedTest
  @MethodSource("com.ak.rsm.resistance.Resistance2LayerTest#twoLayerParameters")
  void testValue2dH1(double[] rho, double hmm, double smm, double lmm) {
    TetrapolarSystem system = new TetrapolarSystem(Metrics.Length.MILLI.to(smm, METRE), Metrics.Length.MILLI.to(lmm, METRE));
    double hStep = Metrics.Length.MILLI.to(1, METRE);
    double dh = hStep * 2;
    int p1 = (int) hmm;

    var b = TetrapolarResistance.of(system);
    double expected = (
        b.rho1(rho[0]).rho2(rho[1]).rho3(rho[1]).hStep(hStep / SCALE).p((p1 + 2) * SCALE, SCALE).resistivity() -
            b.rho1(rho[0]).rho2(rho[1]).rho3(rho[1]).hStep(hStep / SCALE).p(p1 * SCALE, SCALE).resistivity()
    ) / (dh / system.lCC());
    expected /= rho[0];

    double actual = Apparent3Rho.newDerApparentByPhiDivRho1(system,
        new double[] {Layers.getK12(rho[0], rho[1]), 0.0}, hStep, p1, 1, DeltaH.H1.apply(dh));
    assertThat(StrictMath.log(Math.abs(actual))).isCloseTo(StrictMath.log(Math.abs(expected)), byLessThan(0.1));
    assertThat(Math.signum(actual)).isCloseTo(Math.signum(expected), byLessThan(0.1));
  }

  @ParameterizedTest
  @MethodSource("com.ak.rsm.resistance.Resistance2LayerTest#twoLayerParameters")
  void testValue2dH2(double[] rho, double hmm, double smm, double lmm) {
    TetrapolarSystem system = new TetrapolarSystem(Metrics.Length.MILLI.to(smm, METRE), Metrics.Length.MILLI.to(lmm, METRE));
    double hStep = Metrics.Length.MILLI.to(1, METRE);
    double dh = hStep * 2;
    int p2mp1 = (int) hmm;

    var b = TetrapolarResistance.of(system);
    double expected = (
        b.rho1(rho[0]).rho2(rho[0]).rho3(rho[1]).hStep(hStep / SCALE).p(SCALE, (p2mp1 + 2) * SCALE).resistivity() -
            b.rho1(rho[0]).rho2(rho[0]).rho3(rho[1]).hStep(hStep / SCALE).p(SCALE, p2mp1 * SCALE).resistivity()
    ) / (dh / system.lCC());
    expected /= rho[0];

    double actual = Apparent3Rho.newDerApparentByPhiDivRho1(system,
        new double[] {0.0, Layers.getK12(rho[0], rho[1])}, hStep, 1, p2mp1, DeltaH.H2.apply(dh));
    assertThat(StrictMath.log(Math.abs(actual))).isCloseTo(StrictMath.log(Math.abs(expected)), byLessThan(0.1));
    assertThat(Math.signum(actual)).isCloseTo(Math.signum(expected), byLessThan(0.1));
  }

  @ParameterizedTest
  @MethodSource("com.ak.rsm.resistance.Resistance3LayerTest#threeLayerParameters")
  void testValue3dH1(double[] rho, double hStep, int[] p, double smm, double lmm) {
    TetrapolarSystem system = new TetrapolarSystem(Metrics.Length.MILLI.to(smm, METRE), Metrics.Length.MILLI.to(lmm, METRE));
    double dh = hStep * 2;

    var b = TetrapolarResistance.of(system);
    double expected = (
        b.rho1(rho[0]).rho2(rho[1]).rho3(rho[2]).hStep(hStep / SCALE).p((p[0] + 2) * SCALE, p[1] * SCALE).resistivity() -
            b.rho1(rho[0]).rho2(rho[1]).rho3(rho[2]).hStep(hStep / SCALE).p(p[0] * SCALE, p[1] * SCALE).resistivity()
    ) / (dh / system.lCC());
    expected /= rho[0];

    double actual = Apparent3Rho.newDerApparentByPhiDivRho1(system,
        new double[] {Layers.getK12(rho[0], rho[1]), Layers.getK12(rho[1], rho[2])},
        hStep, p[0], p[1], DeltaH.H1.apply(dh));
    assertThat(StrictMath.log(Math.abs(actual))).isCloseTo(StrictMath.log(Math.abs(expected)), byLessThan(0.1));
    assertThat(Math.signum(actual)).isCloseTo(Math.signum(expected), byLessThan(0.1));
  }

  @ParameterizedTest
  @MethodSource("com.ak.rsm.resistance.Resistance3LayerTest#threeLayerParameters")
  void testValue3dH2(double[] rho, double hStep, int[] p, double smm, double lmm) {
    TetrapolarSystem system = new TetrapolarSystem(Metrics.Length.MILLI.to(smm, METRE), Metrics.Length.MILLI.to(lmm, METRE));
    double dh = hStep * 2;

    var b = TetrapolarResistance.of(system);
    double expected = (
        b.rho1(rho[0]).rho2(rho[1]).rho3(rho[2]).hStep(hStep / SCALE).p(p[0] * SCALE, (p[1] + 2) * SCALE).resistivity() -
            b.rho1(rho[0]).rho2(rho[1]).rho3(rho[2]).hStep(hStep / SCALE).p(p[0] * SCALE, p[1] * SCALE).resistivity()
    ) / (dh / system.lCC());
    expected /= rho[0];

    double actual = Apparent3Rho.newDerApparentByPhiDivRho1(system,
        new double[] {Layers.getK12(rho[0], rho[1]), Layers.getK12(rho[1], rho[2])},
        hStep / SCALE, p[0] * SCALE, p[1] * SCALE, DeltaH.H2.apply(dh));
    assertThat(StrictMath.log(Math.abs(actual))).isCloseTo(StrictMath.log(Math.abs(expected)), byLessThan(0.1));
    assertThat(Math.signum(actual)).isCloseTo(Math.signum(expected), byLessThan(0.1));
  }

  @ParameterizedTest
  @MethodSource("com.ak.rsm.resistance.Resistance3LayerTest#threeLayerParameters")
  void testValue3dH1H2(double[] rho, double hStep, int[] p, double smm, double lmm) {
    TetrapolarSystem system = new TetrapolarSystem(Metrics.Length.MILLI.to(smm, METRE), Metrics.Length.MILLI.to(lmm, METRE));
    int dp1 = 1;
    double dh1 = hStep * dp1;
    int dp2 = 2;
    double dh2 = hStep * dp2;

    double dh = TetrapolarDerivativeResistance.of(system).dh(DeltaH.ofH1andH2(dh1, dh2)).rho1(rho[0]).rho2(rho[1]).rho3(rho[2])
        .hStep(hStep).p(p[0], p[1]).dh();

    var b = TetrapolarResistance.of(system);
    double expected = (
        b.rho1(rho[0]).rho2(rho[1]).rho3(rho[2]).hStep(hStep / SCALE).p((p[0] + dp1) * SCALE, (p[1] + dp2) * SCALE).resistivity() -
            b.rho1(rho[0]).rho2(rho[1]).rho3(rho[2]).hStep(hStep / SCALE).p(p[0] * SCALE, p[1] * SCALE).resistivity()
    ) / (dh / system.lCC());
    expected /= rho[0];

    double actual = Apparent3Rho.newDerApparentByPhiDivRho1(system,
        new double[] {Layers.getK12(rho[0], rho[1]), Layers.getK12(rho[1], rho[2])},
        hStep / SCALE, p[0] * SCALE, p[1] * SCALE, DeltaH.ofH1andH2(dh1, dh2));
    assertThat(StrictMath.log(Math.abs(actual))).isCloseTo(StrictMath.log(Math.abs(expected)), byLessThan(0.1));
    assertThat(Math.signum(actual)).isCloseTo(Math.signum(expected), byLessThan(0.1));
  }
}