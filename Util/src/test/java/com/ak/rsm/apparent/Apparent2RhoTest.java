package com.ak.rsm.apparent;

import com.ak.rsm.relative.RelativeMediumLayers;
import com.ak.rsm.resistance.TetrapolarResistance;
import com.ak.rsm.system.RelativeTetrapolarSystem;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.byLessThan;

class Apparent2RhoTest {
  @ParameterizedTest
  @MethodSource("com.ak.rsm.resistance.Resistance2LayerTest#twoLayerParameters")
  void testValueNormalized(@Nonnull double[] rho,
                           @Nonnegative double hmm, @Nonnegative double smm, @Nonnegative double lmm, @Nonnegative double rOhm) {
    double apparent = TetrapolarResistance.ofMilli(smm, lmm).ofOhms(rOhm).resistivity() / rho[0];

    double predicted = Apparent2Rho.newApparentDivRho1(new RelativeTetrapolarSystem(lmm / smm))
        .applyAsDouble(new RelativeMediumLayers(rho, hmm / smm));
    assertThat(apparent).isCloseTo(predicted, byLessThan(0.001));

    double predicted2 = Apparent2Rho.newApparentDivRho1(new RelativeTetrapolarSystem(smm / lmm))
        .applyAsDouble(new RelativeMediumLayers(rho, hmm / lmm));
    assertThat(apparent).isCloseTo(predicted2, byLessThan(0.001));
  }
}