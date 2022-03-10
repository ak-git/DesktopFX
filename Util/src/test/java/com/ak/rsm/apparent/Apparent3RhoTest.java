package com.ak.rsm.apparent;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.rsm.resistance.Resistance3LayerTest;
import com.ak.rsm.resistance.TetrapolarResistance;
import com.ak.rsm.system.Layers;
import com.ak.rsm.system.RelativeTetrapolarSystem;
import com.ak.util.Metrics;
import org.testng.Assert;
import org.testng.annotations.Test;

import static java.lang.StrictMath.log;

public class Apparent3RhoTest {
  @Test(dataProviderClass = Resistance3LayerTest.class, dataProvider = "layer-model")
  public void testValue(@Nonnull double[] rho, @Nonnegative double hStepSI, @Nonnull int[] p,
                        @Nonnegative double smm, @Nonnegative double lmm, @Nonnegative double rOhm) {
    double logApparent = log(TetrapolarResistance.ofMilli(smm, lmm).ofOhms(rOhm).resistivity()) - log(rho[0]);
    double logPredicted = Apparent3Rho.newLog1pApparent3Rho(new RelativeTetrapolarSystem(lmm / smm)).
        value(Layers.getK12(rho[0], rho[1]), Layers.getK12(rho[1], rho[2]), hStepSI / Metrics.fromMilli(smm), p[0], p[1]);
    Assert.assertEquals(logApparent, logPredicted, 0.001);
  }

  @Test(dataProviderClass = Resistance3LayerTest.class, dataProvider = "layer-model")
  public void testValueNormalized(@Nonnull double[] rho, @Nonnegative double hStepSI, @Nonnull int[] p,
                                  @Nonnegative double smm, @Nonnegative double lmm, @Nonnegative double rOhm) {
    double apparent = TetrapolarResistance.ofMilli(smm, lmm).ofOhms(rOhm).resistivity() / rho[0];

    double predicted = Apparent3Rho.newNormalizedApparent2Rho(new RelativeTetrapolarSystem(lmm / smm))
        .value(Layers.getK12(rho[0], rho[1]), Layers.getK12(rho[1], rho[2]), hStepSI / Metrics.fromMilli(smm), p[0], p[1]);
    Assert.assertEquals(apparent, predicted, 0.001);

    double predicted2 = Apparent3Rho.newNormalizedApparent2Rho(new RelativeTetrapolarSystem(smm / lmm))
        .value(Layers.getK12(rho[0], rho[1]), Layers.getK12(rho[1], rho[2]), hStepSI / Metrics.fromMilli(lmm), p[0], p[1]);
    Assert.assertEquals(apparent, predicted2, 0.001);
  }
}