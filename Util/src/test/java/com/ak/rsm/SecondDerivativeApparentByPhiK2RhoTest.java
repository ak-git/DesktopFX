package com.ak.rsm;

import java.util.function.DoubleBinaryOperator;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import org.testng.Assert;
import org.testng.annotations.Test;

public class SecondDerivativeApparentByPhiK2RhoTest {
  @Test
  public void test() {
    DoubleBinaryOperator operator = Apparent2Rho.newSecondDerivativeApparentByPhiK2Rho(new RelativeTetrapolarSystem(10.0 / 30.0));
    Assert.assertEquals(operator.applyAsDouble(0.9, 5.0 / 30.0), -30.855, 0.001);
  }

  @Test(dataProviderClass = Resistance2LayerTest.class, dataProvider = "layer-model")
  public void testValueSL(@Nonnull double[] rho, @Nonnegative double hmm, @Nonnegative double smm, @Nonnegative double lmm, @Nonnegative double rOhm) {
    TetrapolarSystem system = TetrapolarSystem.milli(0.0).s(smm).l(lmm);
    double phi = hmm / lmm;
    double dK = 1.0e-6;
    double k12 = Layers.getK12(rho[0], rho[1]);
    double actual = Apparent2Rho.newDerivativeApparentByPhi2Rho(system.toRelative()).applyAsDouble(k12 + dK, phi) -
        Apparent2Rho.newDerivativeApparentByPhi2Rho(system.toRelative()).applyAsDouble(k12, phi);
    actual /= dK;
    Assert.assertEquals(actual, Apparent2Rho.newSecondDerivativeApparentByPhiK2Rho(system.toRelative()).applyAsDouble(k12, phi), 0.9);
  }

  @Test(dataProviderClass = Resistance2LayerTest.class, dataProvider = "layer-model")
  public void testValueLS(@Nonnull double[] rho, @Nonnegative double hmm, @Nonnegative double smm, @Nonnegative double lmm, @Nonnegative double rOhm) {
    TetrapolarSystem system = TetrapolarSystem.milli(0.0).s(lmm).l(smm);
    double phi = hmm / lmm;
    double dK = 1.0e-6;
    double k12 = Layers.getK12(rho[0], rho[1]);
    double actual = Apparent2Rho.newDerivativeApparentByPhi2Rho(system.toRelative()).applyAsDouble(k12 + dK, phi) -
        Apparent2Rho.newDerivativeApparentByPhi2Rho(system.toRelative()).applyAsDouble(k12, phi);
    actual /= dK;
    Assert.assertEquals(actual, Apparent2Rho.newSecondDerivativeApparentByPhiK2Rho(system.toRelative()).applyAsDouble(k12, phi), Math.abs(actual / 10.0));
  }
}