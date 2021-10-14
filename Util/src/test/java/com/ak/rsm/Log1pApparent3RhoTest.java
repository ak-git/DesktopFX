package com.ak.rsm;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.util.Metrics;
import org.testng.Assert;
import org.testng.annotations.Test;

import static java.lang.StrictMath.log;

public class Log1pApparent3RhoTest {
  @Test(dataProviderClass = Resistance3LayerTest.class, dataProvider = "layer-model")
  public void testValue(@Nonnull double[] rho, @Nonnegative double hStepSI, @Nonnull int[] p,
                        @Nonnegative double smm, @Nonnegative double lmm, @Nonnegative double rOhm) {
    TetrapolarSystem system = TetrapolarSystem.milli(0.1).s(smm).l(lmm);
    double logApparent = log(system.getApparent(rOhm)) - log(rho[0]);
    double logPredicted = Apparent3Rho.newLog1pApparent3Rho(new RelativeTetrapolarSystem(lmm / smm)).
        value(Layers.getK12(rho[0], rho[1]), Layers.getK12(rho[1], rho[2]), hStepSI / Metrics.fromMilli(smm), p[0], p[1]);

    Assert.assertEquals(logApparent, logPredicted, 0.001);
  }
}