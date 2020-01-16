package com.ak.rsm;

import com.ak.util.Metrics;
import org.apache.commons.math3.analysis.TrivariateFunction;
import org.testng.Assert;
import org.testng.annotations.Test;

import static tec.uom.se.unit.MetricPrefix.MILLI;
import static tec.uom.se.unit.Units.METRE;

public class LogDerivativeApparent2RhoTest {
  @Test(dataProviderClass = Resistance2LayerTest.class, dataProvider = "layer-model")
  public void testValue(double[] rho, double hmm, double smm, double lmm, double rOhm) {
    TetrapolarSystem system = new TetrapolarSystem(smm, lmm, MILLI(METRE));
    double h = Metrics.fromMilli(hmm);
    double dh = Metrics.fromMilli(-0.001);
    TrivariateFunction resistance2Layer = new Resistance2Layer(system);
    double expected = new Resistance1Layer(system).getApparent(
        (resistance2Layer.value(rho[0], rho[1], h + dh) - resistance2Layer.value(rho[0], rho[1], h)) / dh
    );
    expected = StrictMath.log(Math.abs(expected / rho[0] / h));
    if (!Double.isFinite(expected)) {
      expected = 0.0;
    }
    double actual = new LogDerivativeApparent2Rho(system).value(Layers.getK12(rho[0], rho[1]), Metrics.fromMilli(hmm));
    Assert.assertEquals(actual, expected, 0.2);
  }
}