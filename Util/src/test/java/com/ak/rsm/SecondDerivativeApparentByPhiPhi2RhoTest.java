package com.ak.rsm;

import java.util.function.ToDoubleFunction;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import org.testng.Assert;
import org.testng.annotations.Test;

public class SecondDerivativeApparentByPhiPhi2RhoTest {
  @Test
  public void test() {
    ToDoubleFunction<RelativeMediumLayers> operator = Apparent2Rho.newSecondDerivativeApparentByPhiPhi2Rho(new RelativeTetrapolarSystem(10.0 / 30.0));
    Assert.assertEquals(operator.applyAsDouble(new Layer2RelativeMedium(0.9, 5.0 / 30.0)), 143.0, 0.1);
  }

  @Test(dataProviderClass = Resistance2LayerTest.class, dataProvider = "layer-model")
  public void testValueSL(@Nonnull double[] rho, @Nonnegative double hmm, @Nonnegative double smm, @Nonnegative double lmm, @Nonnegative double rOhm) {
    TetrapolarSystem system = TetrapolarSystem.milli(0.0).s(smm).l(lmm);
    double phi = hmm / lmm;
    double dPhi = 1.0e-6 * phi;
    double k12 = Layers.getK12(rho[0], rho[1]);
    double actual = Apparent2Rho.newDerivativeApparentByPhi2Rho(system.toRelative()).applyAsDouble(new Layer2RelativeMedium(k12, phi + dPhi)) -
        Apparent2Rho.newDerivativeApparentByPhi2Rho(system.toRelative()).applyAsDouble(new Layer2RelativeMedium(k12, phi));
    actual /= dPhi;
    Assert.assertEquals(actual, Apparent2Rho.newSecondDerivativeApparentByPhiPhi2Rho(system.toRelative()).applyAsDouble(new Layer2RelativeMedium(k12, phi)), 0.6);
  }

  @Test(dataProviderClass = Resistance2LayerTest.class, dataProvider = "layer-model")
  public void testValueLS(@Nonnull double[] rho, @Nonnegative double hmm, @Nonnegative double smm, @Nonnegative double lmm, @Nonnegative double rOhm) {
    TetrapolarSystem system = TetrapolarSystem.milli(0.0).s(lmm).l(smm);
    double phi = hmm / lmm;
    double dPhi = 1.0e-6 * phi;
    double k12 = Layers.getK12(rho[0], rho[1]);
    double actual = Apparent2Rho.newDerivativeApparentByPhi2Rho(system.toRelative()).applyAsDouble(new Layer2RelativeMedium(k12, phi + dPhi)) -
        Apparent2Rho.newDerivativeApparentByPhi2Rho(system.toRelative()).applyAsDouble(new Layer2RelativeMedium(k12, phi));
    actual /= dPhi;
    Assert.assertEquals(actual, Apparent2Rho.newSecondDerivativeApparentByPhiPhi2Rho(system.toRelative()).applyAsDouble(new Layer2RelativeMedium(k12, phi)), Math.abs(actual / 10.0));
  }
}