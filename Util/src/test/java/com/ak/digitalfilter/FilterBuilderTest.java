package com.ak.digitalfilter;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import tec.uom.se.quantity.Quantities;
import tec.uom.se.unit.MetricPrefix;
import tec.uom.se.unit.Units;

import static java.lang.Integer.MAX_VALUE;
import static java.lang.Integer.MIN_VALUE;

public class FilterBuilderTest {
  private static final DigitalFilter[] EMPTY_FILTERS = {};

  private FilterBuilderTest() {
  }

  @DataProvider(name = "simple")
  public static Object[][] simple() {
    return new Object[][] {{
        new int[][] {{1}, {2}, {4}, {8}, {5}, {2}, {1}},
        FilterBuilder.of().build(),
        new int[][] {{1}, {2}, {4}, {8}, {5}, {2}, {1}},
        0.0, 1.0
    }, {
        new int[][] {{1}, {2}, {4}, {8}, {5}, {2}, {1}},
        FilterBuilder.of().fir(1.0, 2.0).build(),
        new int[][] {{2}, {5}, {10}, {20}, {18}, {9}, {4}},
        0.5, 1.0
    }, {
        new int[][] {{1}, {2}, {4}},
        FilterBuilder.of().fir(-1.0, 0.0, 1.0).fir(1.0, 2.0).fir(2.0).fir(3.0).build(),
        new int[][] {{12}, {30}, {16 * 3}},
        1.5, 1.0
    }, {
        new int[][] {{1}, {10}, {100}},
        FilterBuilder.of().fork(
            FilterBuilder.of().fir(2.0).build(),
            FilterBuilder.of().fir(3.0).build(),
            FilterBuilder.of().fir(4.0).build()).build(),
        new int[][] {{2, 3, 4}, {20, 30, 40}, {200, 300, 400}},
        0.0, 1.0
    }, {
        new int[][] {{1}, {2}, {4}},
        FilterBuilder.of().fork(FilterBuilder.of().fir(1.0, 2.0).build(), FilterBuilder.of().fir(3.0).build()).build(),
        new int[][] {{2, 0}, {5, 3}, {10, 6}},
        1.0, 1.0
    }};
  }

  @DataProvider(name = "delay")
  public static Object[][] delay() {
    return new Object[][] {{
        new int[][] {{1, 1, 1}, {2, 2, 2}, {4, 4, 4}, {2, 2, 2}, {2, 2, 2}, {1, 1, 1}},
        FilterBuilder.parallel(
            FilterBuilder.of().fork(
                FilterBuilder.of().fir(1.0).build(),
                FilterBuilder.of().fir(1.0).build()
            ).build(),
            FilterBuilder.of().fir(-1.0, 0.0, 1.0).build(),
            FilterBuilder.of().recursiveMean(2).build()
        ),
        new int[][] {{2, 2, 3, 1}, {4, 4, 0, 3}, {2, 2, -2, 3}, {2, 2, -1, 2}},
        -1.0, 1.0
    }, {
        new int[][] {{1}, {2}, {4}, {2}, {2}, {1}},
        FilterBuilder.of().fork(
            FilterBuilder.of().fir(1.0).build(),
            FilterBuilder.of().fir(-1.0, 0.0, 1.0).build()
        ).buildNoDelay(),
        new int[][] {{2, 3}, {4, 0}, {2, -2}, {2, -1}},
        -1.0, 1.0
    }, {
        new int[][] {{-1}, {1}, {MAX_VALUE}, {1}, {MAX_VALUE}},
        FilterBuilder.of().integrate().build(),
        new int[][] {{-1}, {0}, {MAX_VALUE}, {MIN_VALUE}, {-1}},
        -0.5, 1.0
    }, {
        new int[][] {{-1}, {1}, {MAX_VALUE}, {1}, {MAX_VALUE}},
        FilterBuilder.of().iir(1.0).build(),
        new int[][] {{-1}, {0}, {MAX_VALUE}, {MIN_VALUE}, {-1}},
        0.0, 1.0
    }, {
        new int[][] {{10}, {10}, {10}, {10}, {10}, {10}, {10}, {10}, {10}},
        FilterBuilder.of().iirMATLAB(new double[] {0.2452, 0, -0.2452}, new double[] {1.0, 0, 0.5095}).build(),
        new int[][] {{2}, {2}, {-1}, {-1}, {1}, {1}, {-1}, {-1}, {1}},
        1.0, 1.0
    }, {
        new int[][] {{100}, {0}, {-100}, {0}, {100}, {0}, {-100}, {0}, {100}, {0}, {-100}, {0}, {100}},
        FilterBuilder.of().iirMATLAB(new double[] {0.2452, 0, -0.2452}, new double[] {1.0, 0, 0.5095}).build(),
        new int[][] {{25}, {0}, {-62}, {0}, {81}, {0}, {-90}, {0}, {95}, {0}, {-97}, {0}, {98}},
        1.0, 1.0
    }, {
        new int[][] {{1}, {2}, {4}, {2}, {2}, {1}},
        FilterBuilder.of().recursiveMean(2).build(),
        new int[][] {{1}, {1}, {3}, {3}, {2}, {1}},
        0.0, 1.0
    }, {
        new int[][] {{10}, {10}, {10}, {10}, {10}, {10}},
        FilterBuilder.of().decimate(3).build(),
        new int[][] {{10}, {10}},
        0.0, 1.0 / 3.0
    }, {
        new int[][] {{10}, {10}, {10}, {10}, {10}, {10}},
        FilterBuilder.of().decimate(3).buildNoDelay(),
        new int[][] {{10}, {10}},
        0.0, 1.0 / 3.0
    }, {
        new int[][] {{1}, {4}, {7}},
        FilterBuilder.of().interpolate(3).build(),
        new int[][] {{0}, {0}, {1}, {2}, {3}, {4}, {5}, {6}, {7}},
        1.0, 3.0
    }, {
        new int[][] {{1}, {4}, {7}},
        FilterBuilder.of().interpolate(3).buildNoDelay(),
        new int[][] {{1}, {2}, {3}, {4}, {5}, {6}, {7}},
        -1.0, 3.0
    }, {
        new int[][] {{1}, {4}, {7}},
        FilterBuilder.of().decimate(3).interpolate(3).buildNoDelay(),
        new int[][] {{4}},
        -1.0, 1.0
    }, {
        new int[][] {{2}, {4}, {6}, {8}},
        FilterBuilder.of().fork(
            FilterBuilder.of().decimate(2).interpolate(2).build(),
            FilterBuilder.of().fork(
                FilterBuilder.of().decimate(2).interpolate(2).build(),
                FilterBuilder.of().fir(1).build()
            ).build(),
            FilterBuilder.of().fir(2).build()
        ).build(),
        new int[][] {{0, 1, 0, 0}, {1, 3, 2, 4}, {3, 5, 4, 8}, {5, 7, 6, 12}},
        1.5, 1.0
    }, {
        new int[][] {{1, 2}, {3, 4}, {5, 6}},
        FilterBuilder.of().biOperator(() -> Integer::sum).buildNoDelay(),
        new int[][] {{1 + 2}, {3 + 4}, {5 + 6}},
        0.0, 1.0
    }};
  }

  @DataProvider(name = "strings")
  public static Object[][] strings() {
    return new Object[][] {{
        FilterBuilder.of().build(),
        String.format("NoFilter (delay %.1f)", 0.0),
    }, {
        FilterBuilder.of().decimate(7).build(),
        String.format("LinearDecimationFilter (f / %.1f)", 7.0)
    }, {
        FilterBuilder.of().interpolate(7).buildNoDelay(),
        String.format("NoDelayFilter (compensate %.1f delay x 2) - LinearInterpolationFilter (f \u00b7 %.1f)", 10.0, 7.0)
    }, {
        FilterBuilder.of().fork(
            FilterBuilder.of().fork(
                FilterBuilder.of().fir(1.0).build(),
                FilterBuilder.of().fir(1.0).build()
            ).build(),
            FilterBuilder.of().fir(-1.0, 0.0, 1.0).build(),
            FilterBuilder.of().recursiveMean(4).build()
        ).buildNoDelay(),
        String.format(
            "NoDelayFilter (compensate %.1f delay x 2) - DelayFilter (delay %d) - FIRFilter (delay %.1f)%n" +
                "                                                                   FIRFilter (delay %.1f)%n" +
                "                                           FIRFilter (delay %.1f)%n" +
                "                                           DelayFilter (delay %d) - MeanFilter (delay %.1f)",
            1.0, 1, 0.0,
            0.0,
            1.0,
            1, 0.0
        )
    }, {
        FilterBuilder.parallel(
            FilterBuilder.of().fork(
                FilterBuilder.of().fir(1.0).build(),
                FilterBuilder.of().fir(1.0).build()
            ).build(),
            FilterBuilder.of().fir(-1.0, 0.0, 1.0).build(),
            FilterBuilder.of().recursiveMean(2).build()
        ),
        String.format(
            "NoDelayFilter (compensate %.1f delay x 2) - DelayFilter (delay %d) - SelectFilter (indexes = [0]) - FIRFilter (delay %.1f)%n" +
                "                                                                                                  FIRFilter (delay %.1f)%n" +
                "                                           SelectFilter (indexes = [1]) - FIRFilter (delay %.1f)%n" +
                "                                           DelayFilter (delay %d) - SelectFilter (indexes = [2]) - MeanFilter (delay %.1f)",
            1.0, 1, 0.0,
            0.0,
            1.0,
            1, 0.0
        )
    }, {
        FilterBuilder.parallel(Arrays.asList(new int[] {0}, new int[] {1, 2}),
            FilterBuilder.of().operator(() -> Integer::bitCount).recursiveMean(10).build(), FilterBuilder.of().biOperator(() -> Integer::compare).build()),
        String.format(
            "NoDelayFilter (compensate %.1f delay x 2) - SelectFilter (indexes = [0]) - Operator  (delay %.1f) - MeanFilter (delay %.1f)%n" +
                "                                           SelectFilter (indexes = [1, 2]) - BiOperator  (delay %.1f)",
            0.0, 0.0, 0.0,
            0.0
        )
    }};
  }

  @Test(dataProvider = "simple")
  public static void testWithLostZeroFilter(int[][] input, DigitalFilter filter, int[][] result, double delay, double frequencyFactor) {
    filter.accept(0);
    testFilter(input, filter, result, delay, frequencyFactor);
  }

  @Test(dataProvider = "delay")
  public static void testFilter(int[][] input, DigitalFilter filter, int[][] result, double delay, double frequencyFactor) {
    AtomicInteger filteredCounter = new AtomicInteger();
    filter.forEach(new IntsAcceptor() {
      int i;

      @Override
      public void accept(int... value) {
        filteredCounter.incrementAndGet();
        Assert.assertEquals(value, result[i],
            String.format("Output Sample %d of %d, Actual %s, expected %s", filteredCounter.get() - 1, result.length,
                Arrays.toString(value), Arrays.toString(result[i])));
        i++;
      }
    });
    for (int[] anInput : input) {
      filter.accept(anInput);
    }

    Assert.assertEquals(filteredCounter.get(), result.length, filter.toString());

    Assert.assertEquals(filter.getDelay(), delay, 1.0e-3, filter.toString());
    Assert.assertEquals(filter.getFrequencyFactor(), frequencyFactor, 1.0e-3, filter.toString());
    Assert.assertEquals(filter.getOutputDataSize(), result[0].length);
  }

  @Test(expectedExceptions = IllegalStateException.class)
  public static void testInvalidChain() {
    DigitalFilter filter = FilterBuilder.of().fir(2.0).build();
    FilterBuilder.of().fork(filter, filter).build();
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public static void testInvalidAccept() {
    DigitalFilter filter1 = FilterBuilder.of().fir(1.0).build();
    DigitalFilter filter2 = FilterBuilder.of().fir(1.0).build();
    FilterBuilder.of().fork(filter1, filter2).fir(1.0).build().accept(1);
  }

  @Test(expectedExceptions = IllegalStateException.class)
  public static void testInvalidForkLeft() {
    DigitalFilter filter1 = FilterBuilder.of().fir(1.0).build();
    DigitalFilter filter2 = FilterBuilder.of().fir(2.0).build();
    FilterBuilder.of().fork(filter1, filter2).build();
    filter1.accept(1);
    filter1.accept(2);
    filter2.accept(1);
    filter1.accept(3);
  }

  @Test(expectedExceptions = IllegalStateException.class)
  public static void testInvalidForkRight() {
    DigitalFilter filter1 = FilterBuilder.of().fir(3.0).build();
    DigitalFilter filter2 = FilterBuilder.of().fir(4.0).build();
    FilterBuilder.of().fork(filter1, filter2).build();
    filter2.accept(1);
    filter2.accept(2);
    filter1.accept(1);
    filter2.accept(3);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public static void testInvalidFork() {
    FilterBuilder.of().fork(EMPTY_FILTERS).build();
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public static void testInvalidFork2() {
    new ForkFilter(new DigitalFilter[] {FilterBuilder.of().build()});
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public static void testInvalidParallel() {
    FilterBuilder.parallel(EMPTY_FILTERS);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public static void testInvalidParallel2() {
    DigitalFilter filter = FilterBuilder.parallel(
        FilterBuilder.of().fir(1.0).build(),
        FilterBuilder.of().fir(1.0).build());
    filter.accept(1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "selectedIndexes.*filters.*")
  public static void testInvalidParallel3() {
    FilterBuilder.parallel(Collections.singletonList(new int[] {1}),
        FilterBuilder.of().fir(1.0).build(),
        FilterBuilder.of().fir(1.0).build());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public static void testInvalidDecimateFactor() {
    FilterBuilder.of().decimate(0).build();
  }

  @Test(dataProvider = "strings")
  public static void testToString(DigitalFilter filter, String toString) {
    Assert.assertEquals(filter.toString(), toString, filter.toString());
  }

  @DataProvider(name = "sharpingDecimate")
  public static Object[][] sharpingDecimate() {
    return new Object[][] {{
        new int[] {1, 2, 3}, 1, new int[] {1, 2, 3}
    }, {
        new int[] {1, 2, 3}, 2, new int[] {2}
    }, {
        new int[] {1, 1, 2, 0, 2, -1, 20, -1}, 2, new int[] {1, 0, 2, 20},
    }, {
        new int[] {1, 1, 2, 0, 2, -1, 20, -1}, 3, new int[] {2, -1},
    }, {
        new int[] {1, 1, -2, 0, 2, -1, 20, -1}, 4, new int[] {-2, 20},
    }};
  }

  @Test(dataProvider = "sharpingDecimate")
  public static void testSharpingDecimate(@Nonnull int[] input, @Nonnegative int factor, @Nonnull int[] output) {
    int[] actual = FilterBuilder.of().sharpingDecimate(factor).filter(input);
    Assert.assertEquals(actual, output, String.format("Actual = %s", Arrays.toString(actual)));
  }
}