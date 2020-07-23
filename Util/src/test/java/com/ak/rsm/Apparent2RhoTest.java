package com.ak.rsm;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.util.Metrics;
import org.testng.Assert;
import org.testng.annotations.Test;

import static java.lang.StrictMath.log;
import static tec.uom.se.unit.MetricPrefix.MILLI;
import static tec.uom.se.unit.Units.METRE;

public class Apparent2RhoTest {
  @Test(dataProviderClass = Resistance2LayerTest.class, dataProvider = "layer-model")
  public void testValueLog(@Nonnull double[] rho,
                           @Nonnegative double hmm, @Nonnegative double smm, @Nonnegative double lmm, @Nonnegative double rOhm) {
    TetrapolarSystem system1 = new TetrapolarSystem(smm, lmm, MILLI(METRE));
    TetrapolarSystem system2 = new TetrapolarSystem(lmm, smm, MILLI(METRE));
    double logApparent = log(system1.getApparent(rOhm)) - log(rho[0]);
    double logPredicted = new Log1pApparent2Rho(system2).value(Layers.getK12(rho[0], rho[1]), Metrics.fromMilli(hmm));
    Assert.assertEquals(logApparent, logPredicted, 0.001);
  }

  @Test(dataProviderClass = Resistance2LayerTest.class, dataProvider = "layer-model")
  public void testValueNormalized(@Nonnull double[] rho,
                                  @Nonnegative double hmm, @Nonnegative double smm, @Nonnegative double lmm, @Nonnegative double rOhm) {
    TetrapolarSystem system1 = new TetrapolarSystem(smm, lmm, MILLI(METRE));
    TetrapolarSystem system2 = new TetrapolarSystem(lmm, smm, MILLI(METRE));
    double apparent = system1.getApparent(rOhm) / rho[0];
    double predicted = new NormalizedApparent2Rho(system2).value(Layers.getK12(rho[0], rho[1]), Metrics.fromMilli(hmm));
    Assert.assertEquals(apparent, predicted, 0.001);
  }
}