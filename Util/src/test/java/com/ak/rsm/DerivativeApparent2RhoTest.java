package com.ak.rsm;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.util.Metrics;
import org.apache.commons.math3.analysis.TrivariateFunction;
import org.testng.Assert;
import org.testng.annotations.Test;

public class DerivativeApparent2RhoTest {
  @Test(dataProviderClass = Resistance2LayerTest.class, dataProvider = "layer-model")
  public void testValueSL(@Nonnull double[] rho, @Nonnegative double hmm, @Nonnegative double smm, @Nonnegative double lmm, @Nonnegative double rOhm) {
    TetrapolarSystem system = TetrapolarSystem.milli().s(smm).l(lmm);
    double h = Metrics.fromMilli(hmm);
    double dh = Metrics.fromMilli(-0.00001);
    TrivariateFunction resistance2Layer = new Resistance2Layer(system);
    double expected = system.getApparent(
        (resistance2Layer.value(rho[0], rho[1], h + dh) - resistance2Layer.value(rho[0], rho[1], h)) / dh
    );
    expected /= rho[0];
    double actual = new DerivativeApparent2Rho(system).value(Layers.getK12(rho[0], rho[1]), hmm / lmm);
    Assert.assertEquals(StrictMath.log(Math.abs(actual)), StrictMath.log(Math.abs(expected)), 0.1);
    Assert.assertEquals(Math.signum(actual), Math.signum(expected), 1.0e-6);
  }

  @Test(dataProviderClass = Resistance2LayerTest.class, dataProvider = "layer-model")
  public void testValueLS(@Nonnull double[] rho, @Nonnegative double hmm, @Nonnegative double smm, @Nonnegative double lmm, @Nonnegative double rOhm) {
    TetrapolarSystem system = TetrapolarSystem.milli().s(lmm).l(smm);
    double h = Metrics.fromMilli(hmm);
    double dh = Metrics.fromMilli(-0.00001);
    TrivariateFunction resistance2Layer = new Resistance2Layer(system);
    double expected = system.getApparent(
        (resistance2Layer.value(rho[0], rho[1], h + dh) - resistance2Layer.value(rho[0], rho[1], h)) / dh
    );
    expected /= rho[0];
    double actual = new DerivativeApparent2Rho(system).value(Layers.getK12(rho[0], rho[1]), hmm / smm);
    Assert.assertEquals(StrictMath.log(Math.abs(actual)), StrictMath.log(Math.abs(expected)), 0.1);
    Assert.assertEquals(Math.signum(actual), Math.signum(expected), 1.0e-6);
  }
}