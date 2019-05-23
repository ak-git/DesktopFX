package com.ak.rsm;

import com.ak.util.Metrics;
import org.testng.annotations.DataProvider;

import static tec.uom.se.unit.MetricPrefix.MILLI;
import static tec.uom.se.unit.Units.METRE;

class LayersProvider {
  private LayersProvider() {
  }

  @DataProvider(name = "staticParameters")
  public static Object[][] staticParameters() {
    return new Object[][] {
        {
            new TetrapolarSystem[] {
                new TetrapolarSystem(7.0, 21.0, MILLI(METRE)),
                new TetrapolarSystem(21.0, 35.0, MILLI(METRE)),
                new TetrapolarSystem(7.0, 35.0, MILLI(METRE)),
            },
            new double[] {88.81, 141.1, 34.58},
        },
        {
            new TetrapolarSystem[] {
                new TetrapolarSystem(7.0, 21.0, MILLI(METRE)),
                new TetrapolarSystem(21.0, 35.0, MILLI(METRE)),
                new TetrapolarSystem(7.0, 35.0, MILLI(METRE)),
                new TetrapolarSystem(14.0, 28.0, MILLI(METRE)),
                new TetrapolarSystem(28.0, 42.0, MILLI(METRE)),
            },
            new double[] {123.3, 176.1, 43.09, 170.14, 85.84 * 2}
        },
    };
  }

  @DataProvider(name = "staticParameters5")
  public static Object[][] staticParameters5() {
    return new Object[][] {
        {
            new TetrapolarSystem[] {
                new TetrapolarSystem(7.0, 21.0, MILLI(METRE)),
                new TetrapolarSystem(21.0, 35.0, MILLI(METRE)),
                new TetrapolarSystem(7.0, 35.0, MILLI(METRE)),
                new TetrapolarSystem(14.0, 28.0, MILLI(METRE)),
                new TetrapolarSystem(28.0, 42.0, MILLI(METRE)),
            },
            new double[] {123.3, 176.1, 43.09, 170.14, 85.84 * 2}
        },
    };
  }

  @DataProvider(name = "dynamicParameters")
  public static Object[][] dynamicParameters() {
    return new Object[][] {
        {
            new TetrapolarSystem[] {
                new TetrapolarSystem(7.0, 21.0, MILLI(METRE)),
                new TetrapolarSystem(21.0, 35.0, MILLI(METRE)),
                new TetrapolarSystem(7.0, 35.0, MILLI(METRE)),
            },
            new double[] {88.81, 141.1, 34.58},
            new double[] {88.81 - 0.04, 141.1 - 0.06, 34.58 - 0.03},
            -Metrics.fromMilli(0.1)
        },
        {
            new TetrapolarSystem[] {
                new TetrapolarSystem(7.0, 21.0, MILLI(METRE)),
                new TetrapolarSystem(21.0, 35.0, MILLI(METRE)),
                new TetrapolarSystem(7.0, 35.0, MILLI(METRE)),
            },
            new double[] {123.3, 176.1, 43.09},
            new double[] {123.3 - 0.1, 176.1 - 0.125, 43.09 - 0.04},
            -Metrics.fromMilli(0.1)
        },
        {
            new TetrapolarSystem[] {
                new TetrapolarSystem(7.0, 21.0, MILLI(METRE)),
                new TetrapolarSystem(21.0, 35.0, MILLI(METRE)),
                new TetrapolarSystem(7.0, 35.0, MILLI(METRE)),
                new TetrapolarSystem(14.0, 28.0, MILLI(METRE)),
            },
            new double[] {123.3, 176.1, 43.09, 170.14},
            new double[] {123.3 - 0.1, 176.1 - 0.125, 43.09 - 0.04, 170.14 - 0.16},
            -Metrics.fromMilli(0.1)
        },
        {
            new TetrapolarSystem[] {
                new TetrapolarSystem(7.0, 21.0, MILLI(METRE)),
                new TetrapolarSystem(21.0, 35.0, MILLI(METRE)),
                new TetrapolarSystem(7.0, 35.0, MILLI(METRE)),
                new TetrapolarSystem(14.0, 28.0, MILLI(METRE)),
                new TetrapolarSystem(28.0, 42.0, MILLI(METRE)),
            },
            new double[] {123.3, 176.1, 43.09, 170.14, 85.84 * 2},
            new double[] {123.3 - 0.1, 176.1 - 0.125, 43.09 - 0.04, 170.14 - 0.16, 85.84 * 2 - 0.1 * 2},
            -Metrics.fromMilli(0.1)
        },
    };
  }

  @DataProvider(name = "theoryDynamicParameters2")
  public static Object[][] theoryDynamicParameters2() {
    return new Object[][] {
        // h = 5 mm
        {
            new TetrapolarSystem[] {
                new TetrapolarSystem(10.0, 30.0, MILLI(METRE)),
                new TetrapolarSystem(30.0, 50.0, MILLI(METRE)),
            },
            new double[] {30.971, 61.860},
            new double[] {31.278, 62.479},
            -Metrics.fromMilli(10.0 / 200.0)
        },
        {
            new TetrapolarSystem[] {
                new TetrapolarSystem(10.0, 50.0, MILLI(METRE)),
                new TetrapolarSystem(30.0, 50.0, MILLI(METRE)),
            },
            new double[] {18.069, 61.860},
            new double[] {18.252, 62.479},
            -Metrics.fromMilli(10.0 / 200.0)
        },

        // h = 10 mm
        {
            new TetrapolarSystem[] {
                new TetrapolarSystem(10.0, 30.0, MILLI(METRE)),
                new TetrapolarSystem(30.0, 50.0, MILLI(METRE)),
            },
            new double[] {16.761, 32.246},
            new double[] {16.821, 32.383},
            -Metrics.fromMilli(10.0 / 200.0)
        },
        {
            new TetrapolarSystem[] {
                new TetrapolarSystem(10.0, 50.0, MILLI(METRE)),
                new TetrapolarSystem(30.0, 50.0, MILLI(METRE)),
            },
            new double[] {9.074, 32.246},
            new double[] {9.118, 32.383},
            -Metrics.fromMilli(10.0 / 200.0)
        },

        // h = 15 mm
        {
            new TetrapolarSystem[] {
                new TetrapolarSystem(10.0, 30.0, MILLI(METRE)),
                new TetrapolarSystem(30.0, 50.0, MILLI(METRE)),
            },
            new double[] {13.338, 23.903},
            new double[] {13.357, 23.953},
            -Metrics.fromMilli(10.0 / 200.0)
        },
        {
            new TetrapolarSystem[] {
                new TetrapolarSystem(10.0, 50.0, MILLI(METRE)),
                new TetrapolarSystem(30.0, 50.0, MILLI(METRE)),
            },
            new double[] {6.267, 23.903},
            new double[] {6.284, 23.953},
            -Metrics.fromMilli(10.0 / 200.0)
        },

        // h = 20 mm
        {
            new TetrapolarSystem[] {
                new TetrapolarSystem(10.0, 30.0, MILLI(METRE)),
                new TetrapolarSystem(30.0, 50.0, MILLI(METRE)),
            },
            new double[] {12.187, 20.567},
            new double[] {12.194, 20.589},
            -Metrics.fromMilli(10.0 / 200.0)
        },
        {
            new TetrapolarSystem[] {
                new TetrapolarSystem(10.0, 50.0, MILLI(METRE)),
                new TetrapolarSystem(30.0, 50.0, MILLI(METRE)),
            },
            new double[] {5.082, 20.567},
            new double[] {5.090, 20.589},
            -Metrics.fromMilli(10.0 / 200.0)
        },

        // h = 25 mm
        {
            new TetrapolarSystem[] {
                new TetrapolarSystem(10.0, 30.0, MILLI(METRE)),
                new TetrapolarSystem(30.0, 50.0, MILLI(METRE)),
            },
            new double[] {11.710, 18.986},
            new double[] {11.714, 18.998},
            -Metrics.fromMilli(10.0 / 200.0)
        },
        {
            new TetrapolarSystem[] {
                new TetrapolarSystem(10.0, 50.0, MILLI(METRE)),
                new TetrapolarSystem(30.0, 50.0, MILLI(METRE)),
            },
            new double[] {4.514, 18.986},
            new double[] {4.518, 18.998},
            -Metrics.fromMilli(10.0 / 200.0)
        },

        // h = 30 mm
        {
            new TetrapolarSystem[] {
                new TetrapolarSystem(10.0, 30.0, MILLI(METRE)),
                new TetrapolarSystem(30.0, 50.0, MILLI(METRE)),
            },
            new double[] {11.482, 18.152},
            new double[] {11.484, 18.158},
            -Metrics.fromMilli(10.0 / 200.0)
        },
        {
            new TetrapolarSystem[] {
                new TetrapolarSystem(10.0, 50.0, MILLI(METRE)),
                new TetrapolarSystem(30.0, 50.0, MILLI(METRE)),
            },
            new double[] {4.216, 18.152},
            new double[] {4.218, 18.158},
            -Metrics.fromMilli(10.0 / 200.0)
        },

        // h = 35 mm
        {
            new TetrapolarSystem[] {
                new TetrapolarSystem(10.0, 30.0, MILLI(METRE)),
                new TetrapolarSystem(30.0, 50.0, MILLI(METRE)),
            },
            new double[] {11.361, 17.674},
            new double[] {11.362, 17.678},
            -Metrics.fromMilli(10.0 / 200.0)
        },
        {
            new TetrapolarSystem[] {
                new TetrapolarSystem(10.0, 50.0, MILLI(METRE)),
                new TetrapolarSystem(30.0, 50.0, MILLI(METRE)),
            },
            new double[] {4.047, 17.674},
            new double[] {4.048, 17.678},
            -Metrics.fromMilli(10.0 / 200.0)
        },
    };
  }

  @DataProvider(name = "theoryDynamicParameters3")
  public static Object[][] theoryDynamicParameters3() {
    return new Object[][] {
        // h = 5 mm
        {
            new TetrapolarSystem[] {
                new TetrapolarSystem(10.0, 30.0, MILLI(METRE)),
                new TetrapolarSystem(30.0, 50.0, MILLI(METRE)),
                new TetrapolarSystem(10.0, 50.0, MILLI(METRE)),
            },
            new double[] {30.971, 61.860, 18.069},
            new double[] {31.278, 62.479, 18.252},
            -Metrics.fromMilli(10.0 / 200.0)
        },
        // h = 10 mm
        {
            new TetrapolarSystem[] {
                new TetrapolarSystem(10.0, 30.0, MILLI(METRE)),
                new TetrapolarSystem(30.0, 50.0, MILLI(METRE)),
                new TetrapolarSystem(10.0, 50.0, MILLI(METRE)),
            },
            new double[] {16.761, 32.246, 9.074},
            new double[] {16.821, 32.383, 9.118},
            -Metrics.fromMilli(10.0 / 200.0)
        },
        // h = 15 mm
        {
            new TetrapolarSystem[] {
                new TetrapolarSystem(10.0, 30.0, MILLI(METRE)),
                new TetrapolarSystem(30.0, 50.0, MILLI(METRE)),
                new TetrapolarSystem(10.0, 50.0, MILLI(METRE)),
            },
            new double[] {13.338, 23.903, 6.267},
            new double[] {13.357, 23.953, 6.284},
            -Metrics.fromMilli(10.0 / 200.0)
        },
        // h = 20 mm
        {
            new TetrapolarSystem[] {
                new TetrapolarSystem(10.0, 30.0, MILLI(METRE)),
                new TetrapolarSystem(30.0, 50.0, MILLI(METRE)),
                new TetrapolarSystem(10.0, 50.0, MILLI(METRE)),
            },
            new double[] {12.187, 20.567, 5.082},
            new double[] {12.194, 20.589, 5.090},
            -Metrics.fromMilli(10.0 / 200.0)
        },
        // h = 25 mm
        {
            new TetrapolarSystem[] {
                new TetrapolarSystem(10.0, 30.0, MILLI(METRE)),
                new TetrapolarSystem(30.0, 50.0, MILLI(METRE)),
                new TetrapolarSystem(10.0, 50.0, MILLI(METRE)),
            },
            new double[] {11.710, 18.986, 4.514},
            new double[] {11.714, 18.998, 4.518},
            -Metrics.fromMilli(10.0 / 200.0)
        },
        // h = 30 mm
        {
            new TetrapolarSystem[] {
                new TetrapolarSystem(10.0, 30.0, MILLI(METRE)),
                new TetrapolarSystem(30.0, 50.0, MILLI(METRE)),
                new TetrapolarSystem(10.0, 50.0, MILLI(METRE)),
            },
            new double[] {11.482, 18.152, 4.216},
            new double[] {11.484, 18.158, 4.218},
            -Metrics.fromMilli(10.0 / 200.0)
        },
        // h = 35 mm
        {
            new TetrapolarSystem[] {
                new TetrapolarSystem(10.0, 30.0, MILLI(METRE)),
                new TetrapolarSystem(30.0, 50.0, MILLI(METRE)),
                new TetrapolarSystem(10.0, 50.0, MILLI(METRE)),
            },
            new double[] {11.361, 17.674, 4.047},
            new double[] {11.362, 17.678, 4.048},
            -Metrics.fromMilli(10.0 / 200.0)
        },
    };
  }
}
