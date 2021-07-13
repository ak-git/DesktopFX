package com.ak.rsm;

import javax.annotation.Nonnegative;

import com.ak.inverse.Inequality;
import com.ak.util.Metrics;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ApparentRelativeErrorTest {
  @DataProvider(name = "layer-model")
  public static Object[][] twoLayerParameters() {
    return new Object[][] {
        {10.0, 30.0},
        {30.0, 10.0},
    };
  }

  @Test(dataProvider = "layer-model")
  public void testApplyAsDouble(@Nonnegative double smm, @Nonnegative double lmm) {
    double dLmm = 0.1;
    TetrapolarSystem system = TetrapolarSystem.milli(dLmm).s(smm).l(lmm);

    double ohmsR = new Resistance2Layer(system).value(1.0, Double.POSITIVE_INFINITY, Metrics.fromMilli(5.0));
    double apparent = system.getApparent(ohmsR);

    TetrapolarSystem system2 = TetrapolarSystem.milli(0.1).s(smm - dLmm).l(lmm + dLmm);
    double apparent2 = system2.getApparent(ohmsR);
    Assert.assertEquals(
        new ApparentRelativeError(system).getAsDouble(),
        Inequality.proportional().applyAsDouble(apparent2, apparent),
        0.001);
  }
}