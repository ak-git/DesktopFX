package com.ak.rsm;

import java.util.stream.IntStream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.inverse.Inequality;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class Resistance1LayerTest {
  public static final double SQRT2 = 1.4142135623730951;

  @DataProvider(name = "layer-model")
  public static Object[][] singleLayerParameters() {
    return new Object[][] {
        {1.0, 20.0, 40.0, 21.221},
        {2.0, 40.0, 20.0, 21.221 * 2.0},
        {1.0, 40.0, 80.0, 10.610},
        {1.0, 80.0, 40.0, 10.610},

        {0.7, 6.0 * 1, 6.0 * 3, 18.568},
        {0.7, 6.0 * 3, 6.0 * 5, 27.852},
        {0.7, 6.0 * 2, 6.0 * 4, 12.3785 * 2},
        {0.7, 6.0 * 4, 6.0 * 6, 27.2325 * 2 - 12.3785 * 2},

        {0.7, 7.0 * 1, 7.0 * 3, 15.915},
        {0.7, 7.0 * 3, 7.0 * 5, 23.873},
        {0.7, 7.0 * 2, 7.0 * 4, 10.6105 * 2},
        {0.7, 7.0 * 4, 7.0 * 6, 23.343 * 2 - 10.6105 * 2},

        {0.7, 8.0 * 1, 8.0 * 3, 13.926},
        {0.7, 8.0 * 3, 8.0 * 5, 20.889},
        {0.7, 8.0 * 2, 8.0 * 4, 9.284 * 2},
        {0.7, 8.0 * 4, 8.0 * 6, 20.425 * 2 - 9.284 * 2},
    };
  }

  @Test(dataProvider = "layer-model")
  public void testOneLayer(@Nonnegative double rho, @Nonnegative double smm, @Nonnegative double lmm, @Nonnegative double rOhm) {
    TetrapolarSystem system = TetrapolarSystem.milli().s(smm).l(lmm);
    Assert.assertEquals(new Resistance1Layer(system).value(rho), rOhm, 0.001);
  }

  @DataProvider(name = "tetrapolarSystemsWithErrors")
  public static Object[][] tetrapolarSystemWithErrors() {
    double relError = 0.0001;
    return new Object[][] {
        {InexactTetrapolarSystem.si(2.0 * relError).s(1.0).l(2.0), 6 * relError},
        {InexactTetrapolarSystem.si(2.0 * relError).s(2.0).l(1.0), 6 * relError},
        {InexactTetrapolarSystem.si(3.0 * relError).s(1.0).l(3.0), 6 * relError},
        {InexactTetrapolarSystem.si(relError).s(SQRT2 - 1.0).l(1.0), (3.0 + 2.0 * SQRT2) * relError},
    };
  }

  @Test(dataProvider = "tetrapolarSystemsWithErrors")
  public void testElectrodeSystemRelativeError(@Nonnull InexactTetrapolarSystem system, @Nonnegative double expectedError) {
    double rOhms = new Resistance1Layer(system.getSystem()).value(1.0);
    double error = IntStream.range(0, 1 << 2)
        .mapToDouble(n -> {
          int signS = (n & 1) == 0 ? 1 : -1;
          int signL = (n & (1 << 1)) == 0 ? 1 : -1;
          return system.shift(signS, signL).getApparent(rOhms);
        })
        .map(rho -> Inequality.proportional().applyAsDouble(rho, 1.0)).max().orElseThrow();
    Assert.assertEquals(error, expectedError, 0.01);
  }
}