package com.ak.rsm.apparent;

import com.ak.rsm.resistance.TetrapolarResistance;
import com.ak.rsm.system.Layers;
import com.ak.rsm.system.RelativeTetrapolarSystem;
import com.ak.util.Metrics;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import javax.annotation.Nonnegative;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.byLessThan;
import static tech.units.indriya.unit.Units.METRE;

class Apparent3RhoTest {
  @ParameterizedTest
  @MethodSource("com.ak.rsm.resistance.Resistance3LayerTest#threeLayerParameters")
  void testValueNormalized(double[] rho, @Nonnegative double hStepSI, int[] p,
                           @Nonnegative double smm, @Nonnegative double lmm, @Nonnegative double rOhm) {
    double apparent = TetrapolarResistance.ofMilli(smm, lmm).ofOhms(rOhm).resistivity() / rho[0];

    double predicted = Apparent3Rho.newApparentDivRho1(new RelativeTetrapolarSystem(lmm / smm))
        .value(Layers.getK12(rho[0], rho[1]), Layers.getK12(rho[1], rho[2]), hStepSI / Metrics.Length.MILLI.to(smm, METRE), p[0], p[1]);
    assertThat(apparent).isCloseTo(predicted, byLessThan(0.001));

    double predicted2 = Apparent3Rho.newApparentDivRho1(new RelativeTetrapolarSystem(smm / lmm))
        .value(Layers.getK12(rho[0], rho[1]), Layers.getK12(rho[1], rho[2]), hStepSI / Metrics.Length.MILLI.to(lmm, METRE), p[0], p[1]);
    assertThat(apparent).isCloseTo(predicted2, byLessThan(0.001));
  }

  @Test
  void testApparent3Rho() {
    int factor = 100;
    double value1 = Apparent3Rho.newApparentDivRho1(new RelativeTetrapolarSystem(10.0 / 20.0))
        .value(0.0, 1.0, 1.0 / 20.0 / factor, 5 * factor, 5 * factor);
    double value2 = Apparent3Rho.newApparentDivRho1(new RelativeTetrapolarSystem(10.0 / 20.0))
        .value(0.0, 1.0, 1.0 / 20.0, 5, 5);
    assertThat(value1).isCloseTo(value2, byLessThan(1.0e-4));
  }
}