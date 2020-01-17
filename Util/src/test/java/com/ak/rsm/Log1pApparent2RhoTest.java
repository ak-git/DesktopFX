package com.ak.rsm;

import com.ak.util.Metrics;
import org.testng.Assert;
import org.testng.annotations.Test;

import static java.lang.StrictMath.log;
import static tec.uom.se.unit.MetricPrefix.MILLI;
import static tec.uom.se.unit.Units.METRE;

public class Log1pApparent2RhoTest {
  @Test(dataProviderClass = Resistance2LayerTest.class, dataProvider = "layer-model")
  public void testValue(double[] rho, double hmm, double smm, double lmm, double rOhm) {
    TetrapolarSystem system1 = new TetrapolarSystem(smm, lmm, MILLI(METRE));
    TetrapolarSystem system2 = new TetrapolarSystem(lmm, smm, MILLI(METRE));
    double logApparent = log(new Resistance1Layer(system1).getApparent(rOhm)) - log(rho[0]);
    double logPredicted = new Log1pApparent2Rho(system2).value(Layers.getK12(rho[0], rho[1]), Metrics.fromMilli(hmm));
    Assert.assertEquals(logApparent, logPredicted, 0.001);
  }
}