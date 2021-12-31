package com.ak.rsm.inverse;

import java.security.SecureRandom;
import java.util.Collection;
import java.util.logging.Logger;
import java.util.random.RandomGenerator;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.rsm.measurement.Measurement;
import com.ak.rsm.measurement.TetrapolarMeasurement;
import com.ak.rsm.medium.MediumLayers;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class InverseLayer1Test {
  private static final Logger LOGGER = Logger.getLogger(InverseLayer1Test.class.getName());

  @DataProvider(name = "layer1")
  public static Object[][] layer1() {
    RandomGenerator random = new SecureRandom();
    int rho = random.nextInt(9) + 1;
    return new Object[][] {
        {
            TetrapolarMeasurement.milli2(0.1, 10.0).rho(rho),
            rho
        },
    };
  }

  @Test(dataProvider = "layer1")
  public void testInverseLayer1(@Nonnull Collection<? extends Measurement> measurements, @Nonnegative double expected) {
    MediumLayers medium = InverseStatic.INSTANCE.inverse(measurements);
    Assert.assertEquals(medium.rho().getValue(), expected, 0.2, medium.toString());
    for (Measurement m : measurements) {
      Assert.assertTrue(medium.rho().getAbsError() / medium.rho().getValue() < m.inexact().getApparentRelativeError(), medium.toString());
    }
    LOGGER.info(medium::toString);
  }
}