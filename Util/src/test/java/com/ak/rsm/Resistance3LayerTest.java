package com.ak.rsm;

import java.util.Arrays;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.util.Metrics;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class Resistance3LayerTest {
  @DataProvider(name = "layer-model")
  public static Object[][] threeLayerParameters() {
    return new Object[][] {
        {new double[] {8.0, 8.0, 1.0}, Metrics.fromMilli(1), new int[] {5, 5}, 10.0, 20.0, 309.342},
        {new double[] {8.0, 8.0, 1.0}, Metrics.fromMilli(1), new int[] {0, 10}, 10.0, 20.0, 309.342},
        {new double[] {8.0, 8.0, 1.0}, Metrics.fromMilli(1), new int[] {10, 0}, 10.0, 20.0, 309.342},
        {new double[] {8.0, 8.0, 1.0}, Metrics.fromMilli(1), new int[] {10, 0}, 10.0, 20.0, 309.342},
        {new double[] {8.0, 8.0, 1.0}, Metrics.fromMilli(5), new int[] {1, 1}, 10.0, 20.0, 309.342},
        {new double[] {8.0, 1.0, 1.0}, Metrics.fromMilli(5), new int[] {2, 1}, 10.0, 20.0, 309.342},
        {new double[] {8.0, 1.0, 1.0}, Metrics.fromMilli(10), new int[] {1, 5}, 10.0, 20.0, 309.342},

        {new double[] {8.0, 8.0, 8.0}, Metrics.fromMilli(10), new int[] {1, 5}, 10.0, 20.0,
            new Resistance1Layer(TetrapolarSystem.milli(0.1).s(10.0).l(20.0)).value(8.0)
        },
        {new double[] {8.0, 8.0, 8.0}, Metrics.fromMilli(10), new int[] {0, 0}, 10.0, 20.0,
            new Resistance1Layer(TetrapolarSystem.milli(0.1).s(10.0).l(20.0)).value(8.0)
        },
        {new double[] {8.0, 8.0, 1.0}, Metrics.fromMilli(10), new int[] {30, 30}, 10.0, 20.0,
            new Resistance1Layer(TetrapolarSystem.milli(0.1).s(10.0).l(20.0)).value(8.0)
        },

        {new double[] {8.0, 1.0, 1.0}, Metrics.fromMilli(5), new int[] {1, 1}, 10.0, 20.0,
            new Resistance2Layer(TetrapolarSystem.milli(0.1).s(10.0).l(20.0)).value(8.0, 1.0, Metrics.fromMilli(5))},
        {new double[] {8.0, 2.0, 1.0}, Metrics.fromMilli(5), new int[] {1, 1}, 10.0, 20.0, 242.751},
        {new double[] {8.0, 3.0, 1.0}, Metrics.fromMilli(5), new int[] {1, 1}, 10.0, 20.0, 257.079},
        {new double[] {8.0, 4.0, 1.0}, Metrics.fromMilli(5), new int[] {1, 1}, 10.0, 20.0, 269.694},
        {new double[] {8.0, 5.0, 1.0}, Metrics.fromMilli(5), new int[] {1, 1}, 10.0, 20.0, 281.017},
        {new double[] {8.0, 8.0, 1.0}, Metrics.fromMilli(5), new int[] {1, 1}, 10.0, 20.0,
            new Resistance2Layer(TetrapolarSystem.milli(0.1).s(10.0).l(20.0)).value(8.0, 1.0, Metrics.fromMilli(10))},

        {new double[] {1.0, 1.0, 1.0}, Metrics.fromMilli(10), new int[] {1, 5}, 10.0, 20.0,
            new Resistance1Layer(TetrapolarSystem.milli(0.1).s(10.0).l(20.0)).value(1.0)
        },

        {new double[] {8.0, 1.0, 1.0}, Metrics.fromMilli(0.01), new int[] {1, 1}, 10.0, 20.0,
            new Resistance1Layer(TetrapolarSystem.milli(0.1).s(10.0).l(20.0)).value(1.0)
        },

        {new double[] {1.0, 1.0, 5.0}, Metrics.fromMilli(5), new int[] {1, 1}, 10.0, 20.0, 46.568},
        {new double[] {1.0, 1.0, 8.0}, Metrics.fromMilli(5), new int[] {1, 1}, 10.0, 20.0, 47.403},
        {new double[] {1.0, 5.0, 5.0}, Metrics.fromMilli(5), new int[] {1, 1}, 10.0, 20.0, 60.127},
        {new double[] {1.0, 5.0, 8.0}, Metrics.fromMilli(5), new int[] {1, 1}, 10.0, 20.0, 61.435},
        {new double[] {1.0, 8.0, 5.0}, Metrics.fromMilli(5), new int[] {1, 1}, 10.0, 20.0, 62.886},
        {new double[] {1.0, 8.0, 8.0}, Metrics.fromMilli(5), new int[] {1, 1}, 10.0, 20.0, 64.048},

        {new double[] {22.19, 1.57, 4.24}, Metrics.fromMilli(0.001), new int[] {154, 301}, 7.0, 7.0 * 5, 31.938},
        {new double[] {22.19, 1.57, 4.24}, Metrics.fromMilli(0.001), new int[] {154, 301}, 7.0, 7.0 * 3, 94.584},
        {new double[] {22.19, 1.57, 4.24}, Metrics.fromMilli(0.001), new int[] {154, 301}, 7.0 * 3, 7.0 * 5, 142.539},

        {new double[] {10.0, 5.0, 1.0}, Metrics.fromMilli(1), new int[] {5, 5}, 20.0, 40.0, 99.949},// briko: 101.99
        {new double[] {5.0, 10.0, 1.0}, Metrics.fromMilli(1), new int[] {5, 5}, 20.0, 40.0, 103.657},// briko: 104.87
        {new double[] {1.0, 5.0, 10.0}, Metrics.fromMilli(1), new int[] {5, 5}, 20.0, 40.0, 49.651},// briko: 53.11

        {new double[] {10.0, 1.0, 10.0}, Metrics.fromMilli(0.1), new int[] {3_0, 1}, 10.0, 30.0, 149.637},
        {new double[] {10.0, 1.0, 10.0}, Metrics.fromMilli(0.01), new int[] {3_00, 10}, 10.0, 30.0, 149.637},
        {new double[] {10.0, 1.0, 10.0}, Metrics.fromMilli(0.1), new int[] {1000, 1}, 10.0, 30.0, 159.154},
    };
  }

  @Test(dataProvider = "layer-model")
  public void testLayer(@Nonnull double[] rho, @Nonnegative double hStepSI, @Nonnull int[] p,
                        @Nonnegative double smm, @Nonnegative double lmm, @Nonnegative double rOhm) {
    TetrapolarSystem system = TetrapolarSystem.milli(0.1).s(smm).l(lmm);
    Assert.assertEquals(new Resistance3Layer(system, hStepSI).value(rho[0], rho[1], rho[2], p[0], p[1]), rOhm, 0.001, Arrays.toString(rho));
  }

  @DataProvider(name = "layer-model-special")
  public static Object[][] threeLayerParametersSpecial() {
    return new Object[][] {
        {new double[] {10.0, 1.0, 1.0}, Metrics.fromMilli(0.1), new int[] {10, 0}, 10.0, 30.0,
            new Resistance2Layer(TetrapolarSystem.milli(0.1).s(10.0).l(30.0)).value(10.0, 1.0, Metrics.fromMilli(1))},
        {new double[] {1.0, 10.0, 1.0}, Metrics.fromMilli(0.1), new int[] {0, 10}, 10.0, 30.0,
            new Resistance2Layer(TetrapolarSystem.milli(0.1).s(10.0).l(30.0)).value(10.0, 1.0, Metrics.fromMilli(1))},
        {new double[] {1.0, 1.0, 10.0}, Metrics.fromMilli(0.1), new int[] {0, 0}, 10.0, 30.0,
            new Resistance1Layer(TetrapolarSystem.milli(0.1).s(10.0).l(30.0)).value(10.0)},
    };
  }

  @Test(dataProvider = "layer-model-special")
  public void testLayerSpecial(@Nonnull double[] rho, @Nonnegative double hStepSI, @Nonnull int[] p,
                               @Nonnegative double smm, @Nonnegative double lmm, @Nonnegative double rOhm) {
    TetrapolarSystem system = TetrapolarSystem.milli(0.1).s(smm).l(lmm);
    Assert.assertEquals(new Resistance3Layer(system, hStepSI).value(rho[0], rho[1], rho[2], p[0], p[1]), rOhm, 0.001, Arrays.toString(rho));
  }
}