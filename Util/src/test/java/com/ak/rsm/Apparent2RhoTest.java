package com.ak.rsm;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.util.Metrics;
import org.testng.Assert;
import org.testng.annotations.Test;

import static java.lang.StrictMath.log;

public class Apparent2RhoTest {
  @Test(dataProviderClass = Resistance2LayerTest.class, dataProvider = "layer-model")
  public void testValueLog(@Nonnull double[] rho,
                           @Nonnegative double hmm, @Nonnegative double smm, @Nonnegative double lmm, @Nonnegative double rOhm) {
    double logApparent = log(TetrapolarSystem.milli(0.0).s(smm).l(lmm).getApparent(rOhm)) - log(rho[0]);

    double logPredicted = new Log1pApparent2Rho(new RelativeTetrapolarSystem(lmm / smm))
        .value(Layers.getK12(rho[0], rho[1]), hmm / smm);
    Assert.assertEquals(logApparent, logPredicted, 0.001);

    double logPredicted2 = new Log1pApparent2Rho(new RelativeTetrapolarSystem(smm / lmm))
        .value(Layers.getK12(rho[0], rho[1]), hmm / lmm);
    Assert.assertEquals(logApparent, logPredicted2, 0.001);
  }

  @Test(dataProviderClass = Resistance2LayerTest.class, dataProvider = "layer-model")
  public void testValueNormalized(@Nonnull double[] rho,
                                  @Nonnegative double hmm, @Nonnegative double smm, @Nonnegative double lmm, @Nonnegative double rOhm) {
    double apparent = TetrapolarSystem.milli(0.0).s(smm).l(lmm).getApparent(rOhm) / rho[0];

    double predicted = new NormalizedApparent2Rho(new RelativeTetrapolarSystem(lmm / smm))
        .value(Layers.getK12(rho[0], rho[1]), hmm / smm);
    Assert.assertEquals(apparent, predicted, 0.001);

    double predicted2 = new NormalizedApparent2Rho(new RelativeTetrapolarSystem(smm / lmm))
        .value(Layers.getK12(rho[0], rho[1]), hmm / lmm);
    Assert.assertEquals(apparent, predicted2, 0.001);
  }

  @Test(dataProviderClass = Resistance2LayerTest.class, dataProvider = "layer-model")
  public void testValueLogVSNormalized(@Nonnull double[] rho,
                                       @Nonnegative double hmm, @Nonnegative double smm, @Nonnegative double lmm, @Nonnegative double rOhm) {
    TetrapolarSystem system = TetrapolarSystem.milli(0.0).s(smm).l(lmm);
    double k = Layers.getK12(rho[0], rho[1]);
    double apparent1 = log(system.getApparent(new NormalizedResistance2Layer(system).applyAsDouble(k, Metrics.fromMilli(hmm))));
    double apparent2 = new Log1pApparent2Rho(system.toRelative()).value(k, hmm / lmm);
    Assert.assertEquals(apparent1, apparent2, 0.001);
  }
}