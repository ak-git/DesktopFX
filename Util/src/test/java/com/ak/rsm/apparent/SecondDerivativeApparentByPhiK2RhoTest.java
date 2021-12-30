package com.ak.rsm.apparent;

import java.util.function.ToDoubleFunction;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.rsm.relative.Layer2RelativeMedium;
import com.ak.rsm.relative.RelativeMediumLayers;
import com.ak.rsm.resistance.Resistance2LayerTest;
import com.ak.rsm.system.Layers;
import com.ak.rsm.system.RelativeTetrapolarSystem;
import com.ak.rsm.system.TetrapolarSystem;
import com.ak.util.Metrics;
import org.testng.Assert;
import org.testng.annotations.Test;

public class SecondDerivativeApparentByPhiK2RhoTest {
  @Test
  public void test() {
    ToDoubleFunction<RelativeMediumLayers> operator = Apparent2Rho.newSecondDerivativeApparentByPhiK2Rho(new RelativeTetrapolarSystem(10.0 / 30.0));
    Assert.assertEquals(operator.applyAsDouble(new Layer2RelativeMedium(0.9, 5.0 / 30.0)), -30.855, 0.001);
  }

  @Test(dataProviderClass = Resistance2LayerTest.class, dataProvider = "layer-model")
  public void testValueSL(@Nonnull double[] rho, @Nonnegative double hmm, @Nonnegative double smm, @Nonnegative double lmm, @Nonnegative double rOhm) {
    TetrapolarSystem system = new TetrapolarSystem(Metrics.fromMilli(smm), Metrics.fromMilli(lmm));
    double phi = hmm / lmm;
    double dK = 1.0e-6;
    double k12 = Layers.getK12(rho[0], rho[1]);
    double actual = Apparent2Rho.newDerivativeApparentByPhi2Rho(system.relativeSystem()).applyAsDouble(new Layer2RelativeMedium(k12 + dK, phi)) -
        Apparent2Rho.newDerivativeApparentByPhi2Rho(system.relativeSystem()).applyAsDouble(new Layer2RelativeMedium(k12, phi));
    actual /= dK;
    Assert.assertEquals(actual, Apparent2Rho.newSecondDerivativeApparentByPhiK2Rho(system.relativeSystem()).applyAsDouble(new Layer2RelativeMedium(k12, phi)), 0.9);
  }

  @Test(dataProviderClass = Resistance2LayerTest.class, dataProvider = "layer-model")
  public void testValueLS(@Nonnull double[] rho, @Nonnegative double hmm, @Nonnegative double smm, @Nonnegative double lmm, @Nonnegative double rOhm) {
    TetrapolarSystem system = new TetrapolarSystem(Metrics.fromMilli(lmm), Metrics.fromMilli(smm));
    double phi = hmm / lmm;
    double dK = 1.0e-6;
    double k12 = Layers.getK12(rho[0], rho[1]);
    double actual = Apparent2Rho.newDerivativeApparentByPhi2Rho(system.relativeSystem()).applyAsDouble(new Layer2RelativeMedium(k12 + dK, phi)) -
        Apparent2Rho.newDerivativeApparentByPhi2Rho(system.relativeSystem()).applyAsDouble(new Layer2RelativeMedium(k12, phi));
    actual /= dK;
    Assert.assertEquals(actual, Apparent2Rho.newSecondDerivativeApparentByPhiK2Rho(system.relativeSystem()).applyAsDouble(new Layer2RelativeMedium(k12, phi)), Math.abs(actual / 10.0));
  }
}