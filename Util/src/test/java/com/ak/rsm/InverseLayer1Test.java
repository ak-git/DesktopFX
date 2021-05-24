package com.ak.rsm;

import java.util.Arrays;
import java.util.Random;
import java.util.logging.Logger;

import javax.annotation.Nonnegative;
import javax.annotation.ParametersAreNonnullByDefault;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static com.ak.rsm.InexactTetrapolarSystem.systems2;

public class InverseLayer1Test {
  private static final Logger LOGGER = Logger.getLogger(InverseLayer1Test.class.getName());

  @DataProvider(name = "layer1")
  public static Object[][] layer1() {
    InexactTetrapolarSystem[] systems2 = systems2(0.1, 10.0);
    Random random = new Random();
    int rho = random.nextInt(9) + 1;
    return new Object[][] {
        {
            systems2,
            Arrays.stream(systems2).mapToDouble(its -> new Resistance1Layer(its.toExact()).value(rho))
                .map(r -> r + random.nextGaussian()).toArray(),
            rho
        },
    };
  }

  @Test(dataProvider = "layer1")
  @ParametersAreNonnullByDefault
  public void testInverseLayer1(InexactTetrapolarSystem[] systems, double[] rOhms, @Nonnegative double expected) {
    MediumLayers medium = Inverse.inverseStatic(TetrapolarMeasurement.of(systems, rOhms));
    Assert.assertEquals(medium.rho().getValue(), expected, 0.2, medium.toString());
    for (InexactTetrapolarSystem system : systems) {
      Assert.assertTrue(medium.rho().getAbsError() / medium.rho().getValue() < system.getApparentRelativeError(), medium.toString());
    }
    LOGGER.info(medium::toString);
  }
}