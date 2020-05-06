package com.ak.rsm;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.inverse.Inequality;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static tec.uom.se.unit.MetricPrefix.MILLI;
import static tec.uom.se.unit.Units.METRE;

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
    TetrapolarSystem system = new TetrapolarSystem(smm, lmm, MILLI(METRE));
    Assert.assertEquals(new Resistance1Layer(system).value(rho), rOhm, 0.001);
  }

  @DataProvider(name = "system-apparent")
  public static Object[][] systemApparent() {
    return new Object[][] {
        {new TetrapolarSystem(0.030, 0.06, METRE), 1.0, Math.PI * 9.0 / 400.0},
        {new TetrapolarSystem(90.0, 30.0, MILLI(METRE)), 1.0 / Math.PI, 3.0 / 50.0},
        {new TetrapolarSystem(40.0, 80.0, MILLI(METRE)), 1.0 / Math.PI, 3.0 / 100.0},
    };
  }

  @Test(dataProvider = "system-apparent")
  public void testApparentResistivity(@Nonnull TetrapolarSystem system, @Nonnegative double resistance,
                                      @Nonnegative double specificResistance) {
    Resistance1Layer r = new Resistance1Layer(system);
    Assert.assertEquals(r.getApparent(resistance), specificResistance, 1.0e-6);
  }

  @Test(dataProviderClass = LayersProvider.class, dataProvider = "theoryStaticParameters", enabled = false)
  @ParametersAreNonnullByDefault
  public void testInverse(TetrapolarSystem[] systems, double[] rOhms) {
    Resistance1Layer.inverseStatic(systems, rOhms);
  }

  @DataProvider(name = "layer1")
  public static Object[][] layer1() {
    TetrapolarSystem[] systems4 = LayersProvider.systems4(10.0);
    Random random = new Random();
    int rho = random.nextInt(9) + 1;
    return new Object[][] {
        {
            systems4,
            Arrays.stream(LayersProvider.rOhms(systems4, LayersProvider.layer1(rho))).map(r -> r + random.nextGaussian()).toArray(),
            rho
        },
    };
  }

  @Test(dataProvider = "layer1")
  @ParametersAreNonnullByDefault
  public void testInverse(TetrapolarSystem[] systems, double[] rOhms, @Nonnegative double expected) {
    Assert.assertEquals(Resistance1Layer.inverseStatic(systems, rOhms).getRho(), expected, 0.1);
  }

  @DataProvider(name = "tetrapolarSystemsWithErrors")
  public static Object[][] tetrapolarSystemWithErrors() {
    return new Object[][] {
        {new TetrapolarSystem(1.0, 2.0, MILLI(METRE)), 6},
        {new TetrapolarSystem(2.0, 1.0, MILLI(METRE)), 3},
        {new TetrapolarSystem(1.0, 3.0, MILLI(METRE)), 6},
        {new TetrapolarSystem(SQRT2 - 1.0, 1.0, MILLI(METRE)), 3.0 + 2.0 * SQRT2},
    };
  }

  @Test(dataProvider = "tetrapolarSystemsWithErrors")
  public void testElectrodeSystemRelativeError(@Nonnull TetrapolarSystem system, double errRiseFactor) {
    double relError = 0.0001;
    double absError = system.lToH(1.0) * relError;
    double rOhms = new Resistance1Layer(system).value(1.0);
    double error = IntStream.range(0, 1 << 2)
        .mapToDouble(n -> {
          int signS = (n & 1) == 0 ? 1 : -1;
          int signL = (n & (1 << 1)) == 0 ? 1 : -1;
          return new Resistance1Layer(system.newWithError(absError, signS, signL)).getApparent(rOhms);
        })
        .map(rho -> Inequality.proportional().applyAsDouble(rho, 1.0)).max().orElseThrow();
    Assert.assertEquals(error / relError, errRiseFactor, 0.01);
  }
}