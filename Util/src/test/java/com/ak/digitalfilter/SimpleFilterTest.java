package com.ak.digitalfilter;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nonnull;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class SimpleFilterTest {
  private SimpleFilterTest() {
  }

  @DataProvider(name = "data")
  public static Object[][] data() {
    return new Object[][] {{
        FilterBuilder.of().smoothingImpulsive(3).build(),
        new int[] {1, 2, 3, 4, 5, 6},
        new int[] {0, 1, 2, 3, 4, 5},
        1.0
    }, {
        FilterBuilder.of().smoothingImpulsive(10).build(),
        new int[] {10, 10, 11, 9, 10, 10, 13, 10, 100, -1},
        new int[] {0, 0, 0, 0, 1, 1, 1, 1, 1, 2},
        24.5
    }, {
        FilterBuilder.of().decimate(3).build(),
        new int[] {9, 9, 9, 9, 9, 9},
        new int[] {9, 9},
        0.0
    }, {
        FilterBuilder.of().decimate(() -> new double[] {1.0 / 3.0, 1.0 / 3.0, 1.0 / 3.0}, 3).build(),
        new int[] {9, 9, 9, 9, 9, 9},
        new int[] {9, 9},
        0.0
    }, {
        FilterBuilder.of().interpolate(3).build(),
        new int[] {9, 9, 9},
        new int[] {3, 6, 9, 9, 9, 9, 9, 9, 9},
        1.0
    }, {
        FilterBuilder.of().interpolate(3, () -> new double[] {1.0 / 3.0, 1.0 / 3.0, 1.0 / 3.0}).build(),
        new int[] {9, 9, 9},
        new int[] {3, 6, 9, 9, 9, 9, 9, 9, 9},
        1.0
    }, {
        FilterBuilder.of().decimate(4).build(),
        new int[] {9, 9, 9, 9, 10, 10, 10, 10},
        new int[] {4, 9},
        0.5
    }, {
        FilterBuilder.of().interpolate(1).decimate(1).build(),
        new int[] {100, 110, 120, 130, 140, 150, 160, 170},
        new int[] {100, 110, 120, 130, 140, 150, 160, 170},
        0.0
    }, {
        FilterBuilder.of().interpolate(2).decimate(2).build(),
        new int[] {100, 110, 120, 130, 140, 150, 160, 170},
        new int[] {75, 107, 117, 127, 137, 147, 157, 167},
        0.25
    }, {
        FilterBuilder.of().interpolate(3).decimate(3).build(),
        new int[] {100, 110, 120, 130, 140, 150, 160, 170},
        new int[] {66, 106, 116, 126, 136, 146, 156, 166},
        1.0 / 3.0
    }, {
        FilterBuilder.of().interpolate(4).decimate(4).build(),
        new int[] {100, 110, 120, 130, 140, 150, 160, 170},
        new int[] {15, 57, 97, 116, 126, 136, 146, 156},
        1.375
    }, {
        FilterBuilder.of().decimate(1).interpolate(1).build(),
        new int[] {100, 110, 120, 130, 140, 150, 160, 170},
        new int[] {100, 110, 120, 130, 140, 150, 160, 170},
        0.0
    }, {
        FilterBuilder.of().decimate(2).interpolate(2).build(),
        new int[] {100, 110, 120, 130, 140, 150, 160, 170},
        new int[] {52, 105, 115, 125, 135, 145, 155, 165},
        0.5
    }, {
        FilterBuilder.of().decimate(3).interpolate(3).build(),
        new int[] {100, 110, 120, 130, 140, 150, 160, 170, 180},
        new int[] {36, 73, 110, 120, 130, 140, 150, 160, 170},
        1.0
    }, {
        FilterBuilder.of().decimate(4).interpolate(4).build(),
        new int[] {100, 110, 120, 130, 140, 150, 160, 170},
        new int[] {7, 14, 21, 28, 45, 62, 79, 96},
        5.5
    }, {
        FilterBuilder.of().expSum().build(),
        new int[] {100, 110, 120, 130, 140, 150, 160, 170},
        new int[] {100, 100, 100, 100, 101, 102, 103, 104},
        0.0
    }, {
        FilterBuilder.of().sharpingDecimate(1).build(),
        new int[] {1, 2, 3},
        new int[] {1, 2, 3},
        0.0
    }, {
        FilterBuilder.of().sharpingDecimate(2).build(),
        new int[] {1, 2, 3},
        new int[] {2},
        0.0
    }, {
        FilterBuilder.of().sharpingDecimate(2).build(),
        new int[] {1, 1, 2, 0, 2, -1, 20, -1},
        new int[] {1, 0, 2, 20},
        0.0
    }, {
        FilterBuilder.of().sharpingDecimate(3).build(),
        new int[] {1, 1, 2, 0, 2, -1, 20, -1},
        new int[] {2, -1},
        0.0
    }, {
        FilterBuilder.of().sharpingDecimate(4).build(),
        new int[] {1, 1, -2, 0, 2, -1, 20, -1},
        new int[] {-2, 20},
        0.0
    }, {
        FilterBuilder.of().peakToPeak(3).build(),
        new int[] {1, 2, 3, 4, 5, 6, 7, 8},
        new int[] {1, 2, 2, 2, 2, 2, 2, 2},
        1.0
    }, {
        FilterBuilder.of().peakToPeak(3).build(),
        new int[] {-1, -2, -3, -4, -5, -6, -7, -8},
        new int[] {1, 2, 2, 2, 2, 2, 2, 2},
        1.0
    }, {
        FilterBuilder.of().peakToPeak(3).build(),
        new int[] {1, -2, 3, -4, 5, -6, 7, -8},
        new int[] {1, 3, 5, 7, 9, 11, 13, 15},
        1.0
    }, {
        FilterBuilder.of().rrs().build(),
        new int[] {10, 11, 9, 11, 9, 11, 9},
        new int[] {10, 11, 10, 10, 10, 10, 10},
        0.0
    }, {
        FilterBuilder.of().recursiveMean(7).build(),
        new int[] {-10, -30, -50, -70, -90, -110, -130, -70 - 10, -70 - 30},
        new int[] {-10, -20, -30, -40, -50, -60, -70, -80, -90},
        0.0
    }, {
        FilterBuilder.of().recursiveStd(4).build(),
        new int[] {100, -100, 100, -100, 100, -100, 100, -100, 100, -100, 100, -100, 100, -100},
        new int[] {0, 70, 69, 78, 92, 92, 100, 100, 100, 100, 100, 100, 100, 100},
        0.0
    }, {
        FilterBuilder.of().recursiveMeanAndStd(7).biOperator(() -> (mean, std) -> mean).build(),
        new int[] {-10, -30, -50, -70, -90, -110, -130, -70 - 10, -70 - 30},
        new int[] {-10, -20, -30, -40, -50, -60, -70, -80, -90},
        0.0
    }, {
        FilterBuilder.of().recursiveMeanAndStd(4).biOperator(() -> (mean, std) -> std).build(),
        new int[] {100, -100, 100, -100, 100, -100, 100, -100, 100, -100, 100, -100, 100, -100},
        new int[] {0, 70, 69, 78, 92, 92, 100, 100, 100, 100, 100, 100, 100, 100},
        0.0
    }};
  }


  @Test(dataProvider = "data")
  public static void testFilter(@Nonnull DigitalFilter filter, @Nonnull int[] data, @Nonnull int[] expected, double delay) {
    AtomicInteger index = new AtomicInteger();
    int[] actual = new int[expected.length];
    filter.forEach(values -> {
      Assert.assertEquals(values.length, 1);
      actual[index.getAndIncrement()] = values[0];
    });
    for (int n : data) {
      filter.accept(n);
    }

    Assert.assertEquals(index.get(), expected.length);
    Assert.assertEquals(actual, expected, Arrays.toString(actual));
    Assert.assertEquals(filter.getDelay(), delay, 0.001);
  }

  @DataProvider(name = "data-reset")
  public static Object[][] dataWithReset() {
    return new Object[][] {{
        FilterBuilder.of().fork(FilterBuilder.of().expSum().build(), FilterBuilder.of().fir(new double[] {1.0, 1.0, 1.0}).build()).buildNoDelay(),
        new int[] {100, 110, 120, 130, 140, 150, 160, 170},
        new int[][] {
            {
                100, 100, 100, 101, 102, 103,
                100, 100, 100, 101, 102, 103
            },
            {
                330, 360, 390, 420, 450, 480,
                330, 360, 390, 420, 450, 480
            }
        },
    }, {
        FilterBuilder.of().rrs().build(),
        new int[] {10, 11, 9, 11, 9, 11, 9},
        new int[][] {
            {
                10, 11, 10, 10, 10, 10, 10,
                10, 11, 10, 10, 10, 10, 10
            }
        },
    }, {
        FilterBuilder.of().recursiveMean(4).build(),
        new int[] {4, 2, 0, 2},
        new int[][] {
            {
                4, 3, 2, 2, 0, 0, 0, 0
            }
        },
    }, {
        FilterBuilder.of().recursiveStd(5).build(),
        new int[] {100, -100, 100, -100, 0, -100, 100, -100, 100},
        new int[][] {
            {
                0, 70, 69, 78, 69, 74, 74, 73, 73,
                35, 58, 52, 69, 53, 48, 37, 29, 0
            }
        },
    }, {
        FilterBuilder.parallel(
            Collections.singletonList(new int[] {0}),
            FilterBuilder.of().fir(new double[] {1.0, 1.0, 1.0}).build()
        ),
        new int[] {100, -100, 100, -100, 0, -100, 100, -100, 100},
        new int[][] {
            {
                100, -100, 0, -200, 0, -100, 100,
                100, -100, 0, -200, 0, -100, 100
            }
        },
    }};
  }

  @Test(dataProvider = "data-reset")
  public static void testFilterWithReset(@Nonnull DigitalFilter filter, @Nonnull int[] data, @Nonnull int[][] expected) {
    AtomicInteger index = new AtomicInteger();
    int[][] actual = new int[expected.length][expected[0].length];
    filter.forEach(values -> {
      Assert.assertEquals(values.length, expected.length);
      for (int i = 0; i < values.length; i++) {
        actual[i][index.get()] = values[i];
      }
      index.getAndIncrement();
    });

    for (int n : data) {
      filter.accept(n);
    }
    filter.reset();
    for (int n : data) {
      filter.accept(n);
    }

    Assert.assertEquals(index.get(), expected[0].length);
    Assert.assertEquals(actual, expected, Arrays.deepToString(actual));
  }
}