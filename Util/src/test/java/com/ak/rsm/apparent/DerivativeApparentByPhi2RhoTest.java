package com.ak.rsm.apparent;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.rsm.medium.Layer2RelativeMedium;
import com.ak.rsm.resistance.Resistance2LayerTest;
import com.ak.rsm.resistance.TetrapolarDerivativeResistance;
import com.ak.rsm.system.Layers;
import com.ak.rsm.system.TetrapolarSystem;
import com.ak.util.Metrics;
import org.testng.Assert;
import org.testng.annotations.Test;

public class DerivativeApparentByPhi2RhoTest {
  @Test(dataProviderClass = Resistance2LayerTest.class, dataProvider = "layer-model")
  public void testValueSL(@Nonnull double[] rho, @Nonnegative double hmm, @Nonnegative double smm, @Nonnegative double lmm, @Nonnegative double rOhm) {
    TetrapolarSystem system = new TetrapolarSystem(Metrics.fromMilli(smm), Metrics.fromMilli(lmm));
    double h = Metrics.fromMilli(hmm);
    double dh = Metrics.fromMilli(-0.00001);
    double expected = TetrapolarDerivativeResistance.of(system).dh(dh).rho1(rho[0]).rho2(rho[1]).h(h).derivativeResistivity() / rho[0];
    double actual = Apparent2Rho.newDerivativeApparentByPhi2Rho(system.relativeSystem())
        .applyAsDouble(new Layer2RelativeMedium(Layers.getK12(rho[0], rho[1]), hmm / lmm));
    Assert.assertEquals(actual, expected, 0.1);
  }

  @Test(dataProviderClass = Resistance2LayerTest.class, dataProvider = "layer-model")
  public void testValueLS(@Nonnull double[] rho, @Nonnegative double hmm, @Nonnegative double smm, @Nonnegative double lmm, @Nonnegative double rOhm) {
    TetrapolarSystem system = new TetrapolarSystem(Metrics.fromMilli(lmm), Metrics.fromMilli(smm));
    double h = Metrics.fromMilli(hmm);
    double dh = Metrics.fromMilli(-0.00001);
    double expected = TetrapolarDerivativeResistance.of(system).dh(dh).rho1(rho[0]).rho2(rho[1]).h(h).derivativeResistivity() / rho[0];
    double actual = Apparent2Rho.newDerivativeApparentByPhi2Rho(system.relativeSystem())
        .applyAsDouble(new Layer2RelativeMedium(Layers.getK12(rho[0], rho[1]), hmm / smm));
    Assert.assertEquals(actual, expected, 0.1);
  }
}