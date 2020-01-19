package com.ak.rsm;

import com.ak.util.Metrics;
import org.testng.Assert;
import org.testng.annotations.Test;

import static tec.uom.se.unit.MetricPrefix.MILLI;
import static tec.uom.se.unit.Units.METRE;

public class LogDerivativeApparent3RhoTest {
  @Test(dataProviderClass = Resistance2LayerTest.class, dataProvider = "layer-model")
  public void testValueLayers2(double[] rho, double hmm, double smm, double lmm, double rOhm) {
    TetrapolarSystem system = new TetrapolarSystem(smm, lmm, MILLI(METRE));
    double h = Metrics.fromMilli(1);
    double dh = h / 1000.0;
    int p1 = (int) hmm;
    int p2mp1 = 1;
    Resistance3Layer resistance3LayerAfter = new Resistance3Layer(system, h + dh);
    Resistance3Layer resistance3LayerBefore = new Resistance3Layer(system, h);
    double expected = new Resistance1Layer(system).getApparent(
        (resistance3LayerAfter.value(rho[0], rho[1], rho[1], p1, p2mp1) - resistance3LayerBefore.value(rho[0], rho[1], rho[1], p1, p2mp1)) / dh
    );
    expected = StrictMath.log(Math.abs(expected) / rho[0]);

    double actual = new LogDerivativeApparent3Rho(system).value(Layers.getK12(rho[0], rho[1]), 0.0, h, p1, p2mp1);
    Assert.assertEquals(actual, expected, 0.1);
  }
}