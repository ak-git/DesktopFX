package com.ak.rsm.apparent;

import java.util.function.ToDoubleFunction;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.rsm.relative.Layer2RelativeMedium;
import com.ak.rsm.relative.RelativeMediumLayers;
import com.ak.rsm.system.Layers;
import com.ak.rsm.system.RelativeTetrapolarSystem;
import com.ak.rsm.system.TetrapolarSystem;
import com.ak.util.Metrics;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.byLessThan;
import static org.assertj.core.api.Assertions.withinPercentage;

class SecondDerivativeApparentByPhiPhi2RhoTest {
  @Test
  void test() {
    ToDoubleFunction<RelativeMediumLayers> operator =
        Apparent2Rho.newSecondDerivativeApparentByPhiPhi2Rho(new RelativeTetrapolarSystem(10.0 / 30.0));
    assertThat(operator.applyAsDouble(new Layer2RelativeMedium(0.9, 5.0 / 30.0))).isCloseTo(143.0, byLessThan(0.1));
  }

  @ParameterizedTest
  @MethodSource("com.ak.rsm.resistance.Resistance2LayerTest#twoLayerParameters")
  void testValueSL(@Nonnull double[] rho, @Nonnegative double hmm, @Nonnegative double smm, @Nonnegative double lmm) {
    TetrapolarSystem system = new TetrapolarSystem(Metrics.fromMilli(smm), Metrics.fromMilli(lmm));
    double phi = hmm / lmm;
    double dPhi = 1.0e-6 * phi;
    double k12 = Layers.getK12(rho[0], rho[1]);
    double actual = Apparent2Rho.newDerivativeApparentByPhi2Rho(
        system.relativeSystem()).applyAsDouble(new Layer2RelativeMedium(k12, phi + dPhi)) -
        Apparent2Rho.newDerivativeApparentByPhi2Rho(system.relativeSystem()).applyAsDouble(new Layer2RelativeMedium(k12, phi));
    actual /= dPhi;
    assertThat(actual).isCloseTo(
        Apparent2Rho.newSecondDerivativeApparentByPhiPhi2Rho(system.relativeSystem()).applyAsDouble(new Layer2RelativeMedium(k12, phi)),
        byLessThan(0.6));
  }

  @ParameterizedTest
  @MethodSource("com.ak.rsm.resistance.Resistance2LayerTest#twoLayerParameters")
  void testValueLS(@Nonnull double[] rho, @Nonnegative double hmm, @Nonnegative double smm, @Nonnegative double lmm) {
    TetrapolarSystem system = new TetrapolarSystem(Metrics.fromMilli(lmm), Metrics.fromMilli(smm));
    double phi = hmm / lmm;
    double dPhi = 1.0e-6 * phi;
    double k12 = Layers.getK12(rho[0], rho[1]);
    double actual = Apparent2Rho.newDerivativeApparentByPhi2Rho(system.relativeSystem()).applyAsDouble(new Layer2RelativeMedium(k12, phi + dPhi)) -
        Apparent2Rho.newDerivativeApparentByPhi2Rho(system.relativeSystem()).applyAsDouble(new Layer2RelativeMedium(k12, phi));
    actual /= dPhi;
    assertThat(actual).isCloseTo(
        Apparent2Rho.newSecondDerivativeApparentByPhiPhi2Rho(system.relativeSystem()).applyAsDouble(new Layer2RelativeMedium(k12, phi)),
        withinPercentage(10.0)
    );
  }
}