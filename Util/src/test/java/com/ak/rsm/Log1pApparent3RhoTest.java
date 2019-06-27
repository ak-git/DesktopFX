package com.ak.rsm;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.util.Metrics;
import org.testng.Assert;
import org.testng.annotations.Test;

import static java.lang.StrictMath.log;
import static tec.uom.se.unit.MetricPrefix.MILLI;
import static tec.uom.se.unit.Units.METRE;

public class Log1pApparent3RhoTest {
  private Log1pApparent3RhoTest() {
  }

  @Test(dataProviderClass = Resistance3LayerTest.class, dataProvider = "layer-model")
  public static void testValue(@Nonnull double[] rho, @Nonnegative double hStepSI, @Nonnull int[] p,
                               @Nonnegative double smm, @Nonnegative double lmm, @Nonnegative double rOhm) {
    TetrapolarSystem system1 = new TetrapolarSystem(smm, lmm, MILLI(METRE));
    TetrapolarSystem system2 = new TetrapolarSystem(lmm, smm, MILLI(METRE));
    double logApparent = log(new Resistance1Layer(system1).getApparent(rOhm)) - log(rho[0]);
    double logPredicted = new Log1pApparent3Rho(system2.sToL(), Metrics.fromMilli(smm) / hStepSI).
        value(Layers.getK12(rho[0], rho[1]), Layers.getK12(rho[1], rho[2]), p[0], p[1]);

    Assert.assertEquals(logApparent, logPredicted, 0.001);
  }
}