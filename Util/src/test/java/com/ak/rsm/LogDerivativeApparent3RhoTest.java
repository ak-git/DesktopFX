package com.ak.rsm;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.util.Metrics;
import org.testng.Assert;
import org.testng.annotations.Test;

import static tec.uom.se.unit.MetricPrefix.MILLI;
import static tec.uom.se.unit.Units.METRE;

public class LogDerivativeApparent3RhoTest {
  @Test(dataProviderClass = Resistance2LayerTest.class, dataProvider = "layer-model")
  public void testValue2(@Nonnull double[] rho, @Nonnegative double hmm, @Nonnegative double smm, @Nonnegative double lmm, @Nonnegative double rOhm) {
    TetrapolarSystem system = new TetrapolarSystem(smm, lmm, MILLI(METRE));
    double h = Metrics.fromMilli(1);
    double dh = h / 1000.0;
    int p1 = (int) hmm;
    Resistance3Layer resistance3LayerAfter = new Resistance3Layer(system, h + dh);
    Resistance3Layer resistance3LayerBefore = new Resistance3Layer(system, h);
    double expected = new Resistance1Layer(system).getApparent(
        (resistance3LayerAfter.value(rho[0], rho[1], rho[1], p1, 1) - resistance3LayerBefore.value(rho[0], rho[1], rho[1], p1, 1)) / dh
    );
    expected = Math.signum(expected) * StrictMath.log(Math.abs(expected) / rho[0]);

    double actual = new LogDerivativeApparent3Rho(system).value(Layers.getK12(rho[0], rho[1]), 0.0, h, p1, 1);
    Assert.assertEquals(actual, expected, 0.1);
  }

  @Test(dataProviderClass = Resistance3LayerTest.class, dataProvider = "layer-model")
  public void testValue3(@Nonnull double[] rho, @Nonnegative double h, @Nonnull int[] p,
                         @Nonnegative double smm, @Nonnegative double lmm, @Nonnegative double rOhm) {
    TetrapolarSystem system = new TetrapolarSystem(smm, lmm, MILLI(METRE));
    double dh = h / 1000.0;
    Resistance3Layer resistance3LayerAfter = new Resistance3Layer(system, h + dh);
    Resistance3Layer resistance3LayerBefore = new Resistance3Layer(system, h);
    double expected = new Resistance1Layer(system).getApparent(
        (resistance3LayerAfter.value(rho[0], rho[1], rho[2], p[0], p[1]) - resistance3LayerBefore.value(rho[0], rho[1], rho[2], p[0], p[1])) / dh
    );
    expected = Math.signum(expected) * StrictMath.log(Math.abs(expected) / rho[0]);

    double actual = new LogDerivativeApparent3Rho(system).value(Layers.getK12(rho[0], rho[1]), Layers.getK12(rho[1], rho[2]), h, p[0], p[1]);
    Assert.assertEquals(actual, expected, 0.1);
  }
}