package com.ak.rsm;

import java.security.SecureRandom;
import java.util.Collection;
import java.util.Random;
import java.util.logging.Logger;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static com.ak.rsm.TetrapolarSystem.systems2;

public class InverseLayer1Test {
  private static final Logger LOGGER = Logger.getLogger(InverseLayer1Test.class.getName());

  @DataProvider(name = "layer1")
  public static Object[][] layer1() {
    Random random = new SecureRandom();
    int rho = random.nextInt(9) + 1;
    return new Object[][] {
        {
            TetrapolarMeasurement.of(systems2(0.1, 10.0), its -> new Resistance1Layer(its).value(rho) + random.nextGaussian()),
            rho
        },
    };
  }

  @Test(dataProvider = "layer1")
  public void testInverseLayer1(@Nonnull Collection<? extends Measurement> measurements, @Nonnegative double expected) {
    MediumLayers medium = InverseStatic.INSTANCE.inverse(measurements);
    Assert.assertEquals(medium.rho().getValue(), expected, 0.2, medium.toString());
    for (Measurement m : measurements) {
      Assert.assertTrue(medium.rho().getAbsError() / medium.rho().getValue() < m.system().getApparentRelativeError(), medium.toString());
    }
    LOGGER.info(medium::toString);
  }
}