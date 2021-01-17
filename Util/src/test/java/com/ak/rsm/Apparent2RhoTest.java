package com.ak.rsm;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import org.testng.Assert;
import org.testng.annotations.Test;

import static java.lang.StrictMath.log;

public class Apparent2RhoTest {
  @Test(dataProviderClass = Resistance2LayerTest.class, dataProvider = "layer-model")
  public void testValueLog(@Nonnull double[] rho,
                           @Nonnegative double hmm, @Nonnegative double smm, @Nonnegative double lmm, @Nonnegative double rOhm) {
    TetrapolarSystem system = TetrapolarSystem.milli().s(smm).l(lmm);
    double logApparent = log(system.getApparent(rOhm)) - log(rho[0]);
    double logPredicted = new Log1pApparent2Rho(new RelativeTetrapolarSystem(lmm / smm))
        .value(Layers.getK12(rho[0], rho[1]), hmm / smm);
    Assert.assertEquals(logApparent, logPredicted, 0.001);
  }

  @Test(dataProviderClass = Resistance2LayerTest.class, dataProvider = "layer-model")
  public void testValueNormalized(@Nonnull double[] rho,
                                  @Nonnegative double hmm, @Nonnegative double smm, @Nonnegative double lmm, @Nonnegative double rOhm) {
    TetrapolarSystem system = TetrapolarSystem.milli().s(smm).l(lmm);
    double apparent = system.getApparent(rOhm) / rho[0];
    double predicted = new NormalizedApparent2Rho(new RelativeTetrapolarSystem(lmm / smm))
        .value(Layers.getK12(rho[0], rho[1]), hmm / smm);
    Assert.assertEquals(apparent, predicted, 0.001);
  }
}