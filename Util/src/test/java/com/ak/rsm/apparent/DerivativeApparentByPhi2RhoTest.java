package com.ak.rsm.apparent;

import com.ak.rsm.relative.RelativeMediumLayers;
import com.ak.rsm.resistance.TetrapolarResistance;
import com.ak.rsm.system.TetrapolarSystem;
import com.ak.util.Metrics;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import javax.annotation.Nonnegative;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.byLessThan;
import static tech.units.indriya.unit.Units.METRE;

class DerivativeApparentByPhi2RhoTest {
  @ParameterizedTest
  @MethodSource("com.ak.rsm.resistance.Resistance2LayerTest#twoLayerParameters")
  void testValueSL(double[] rho, @Nonnegative double hmm, @Nonnegative double smm, @Nonnegative double lmm) {
    TetrapolarSystem system = new TetrapolarSystem(Metrics.Length.MILLI.to(smm, METRE), Metrics.Length.MILLI.to(lmm, METRE));
    double h = Metrics.Length.MILLI.to(hmm, METRE);
    double dh = Metrics.Length.MILLI.to(0.00001, METRE);

    var b = TetrapolarResistance.of(system);
    double expected = (
        b.rho1(rho[0]).rho2(rho[1]).h(h + dh).resistivity() -
            b.rho1(rho[0]).rho2(rho[1]).h(h).resistivity()
    ) / (dh / system.lCC());
    expected /= rho[0];

    assertThat(Apparent2Rho.newDerApparentByPhiDivRho1(system, Double.NaN).applyAsDouble(new RelativeMediumLayers(rho, hmm / lmm)))
        .isCloseTo(expected, byLessThan(0.1));
    assertThat(Apparent2Rho.newDerApparentByPhiDivRho1(system, dh).applyAsDouble(new RelativeMediumLayers(rho, hmm / lmm)))
        .isCloseTo(expected, byLessThan(0.1));
  }

  @ParameterizedTest
  @MethodSource("com.ak.rsm.resistance.Resistance2LayerTest#twoLayerParameters")
  void testValueLS(double[] rho, @Nonnegative double hmm, @Nonnegative double smm, @Nonnegative double lmm) {
    TetrapolarSystem system = new TetrapolarSystem(Metrics.Length.MILLI.to(lmm, METRE), Metrics.Length.MILLI.to(smm, METRE));
    double h = Metrics.Length.MILLI.to(hmm, METRE);
    double dh = Metrics.Length.MILLI.to(-0.00001, METRE);

    var b = TetrapolarResistance.of(system);
    double expected = (
        b.rho1(rho[0]).rho2(rho[1]).h(h + dh).resistivity() -
            b.rho1(rho[0]).rho2(rho[1]).h(h).resistivity()
    ) / (dh / system.lCC());
    expected /= rho[0];

    assertThat(Apparent2Rho.newDerApparentByPhiDivRho1(system, Double.NaN).applyAsDouble(new RelativeMediumLayers(rho, hmm / smm)))
        .isCloseTo(expected, byLessThan(0.1));
    assertThat(Apparent2Rho.newDerApparentByPhiDivRho1(system, dh).applyAsDouble(new RelativeMediumLayers(rho, hmm / smm)))
        .isCloseTo(expected, byLessThan(0.1));
  }
}