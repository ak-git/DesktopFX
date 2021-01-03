package com.ak.rsm;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import org.testng.Assert;
import org.testng.annotations.Test;

import static java.lang.StrictMath.log;

public class Log1pApparent3RhoTest {
  @Test(dataProviderClass = Resistance3LayerTest.class, dataProvider = "layer-model")
  public void testValue(@Nonnull double[] rho, @Nonnegative double hStepSI, @Nonnull int[] p,
                        @Nonnegative double smm, @Nonnegative double lmm, @Nonnegative double rOhm) {
    TetrapolarSystem system1 = TetrapolarSystem.milli().s(smm).l(lmm);
    TetrapolarSystem system2 = TetrapolarSystem.milli().s(lmm).l(smm);
    double logApparent = log(system1.getApparent(rOhm)) - log(rho[0]);
    double logPredicted = new Log1pApparent3Rho(system2).
        value(Layers.getK12(rho[0], rho[1]), Layers.getK12(rho[1], rho[2]), hStepSI, p[0], p[1]);

    Assert.assertEquals(logApparent, logPredicted, 0.001);
  }
}