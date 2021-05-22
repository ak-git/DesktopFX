package com.ak.rsm;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.util.Metrics;
import org.testng.Assert;
import org.testng.annotations.Test;

public class DerivativeApparent3RhoTest {
  @Test(dataProviderClass = Resistance2LayerTest.class, dataProvider = "layer-model")
  public void testValue2(@Nonnull double[] rho, @Nonnegative double hmm, @Nonnegative double smm, @Nonnegative double lmm, @Nonnegative double rOhm) {
    TetrapolarSystem system = TetrapolarSystem.milli().s(smm).l(lmm);
    double h = Metrics.fromMilli(1);
    double dh = h / 1000.0;
    int p1 = (int) hmm;
    Resistance3Layer resistance3LayerAfter = new Resistance3Layer(system, h + dh);
    Resistance3Layer resistance3LayerBefore = new Resistance3Layer(system, h);
    double expected = system.getApparent(
        (resistance3LayerAfter.value(rho[0], rho[1], rho[1], p1, 1) - resistance3LayerBefore.value(rho[0], rho[1], rho[1], p1, 1)) / dh
    );
    expected /= rho[0];

    double actual = new DerivativeApparent3Rho(system.toRelative()).value(Layers.getK12(rho[0], rho[1]), 0.0,
        h / Metrics.fromMilli(lmm), p1, 1) / system.getL();
    Assert.assertEquals(StrictMath.log(Math.abs(actual)), StrictMath.log(Math.abs(expected)), 0.1);
    Assert.assertEquals(Math.signum(actual), Math.signum(expected), 0.1);
  }

  @Test(dataProviderClass = Resistance3LayerTest.class, dataProvider = "layer-model")
  public void testValue3(@Nonnull double[] rho, @Nonnegative double h, @Nonnull int[] p,
                         @Nonnegative double smm, @Nonnegative double lmm, @Nonnegative double rOhm) {
    TetrapolarSystem system = TetrapolarSystem.milli().s(smm).l(lmm);
    double dh = h / 1000.0;
    Resistance3Layer resistance3LayerAfter = new Resistance3Layer(system, h + dh);
    Resistance3Layer resistance3LayerBefore = new Resistance3Layer(system, h);
    double expected = system.getApparent(
        (resistance3LayerAfter.value(rho[0], rho[1], rho[2], p[0], p[1]) - resistance3LayerBefore.value(rho[0], rho[1], rho[2], p[0], p[1])) / dh
    );
    expected /= rho[0];

    double actual = new DerivativeApparent3Rho(system.toRelative()).value(Layers.getK12(rho[0], rho[1]), Layers.getK12(rho[1], rho[2]),
        h / Metrics.fromMilli(lmm), p[0], p[1]) / system.getL();
    Assert.assertEquals(StrictMath.log(Math.abs(actual)), StrictMath.log(Math.abs(expected)), 0.1);
    Assert.assertEquals(Math.signum(actual), Math.signum(expected), 0.1);
  }
}