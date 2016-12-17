package com.ak.digitalfilter;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import tec.uom.se.quantity.Quantities;
import tec.uom.se.unit.MetricPrefix;
import tec.uom.se.unit.Units;

import static java.lang.Integer.MAX_VALUE;
import static java.lang.Integer.MIN_VALUE;

public class FIRFilterTest {
  @DataProvider(name = "simple")
  public Object[][] simple() {
    return new Object[][] {{
        new int[] {1, 2, 4, 8, 5, 2, 1},
        FilterBuilder.of().build(),
        new int[][] {{1}, {2}, {4}, {8}, {5}, {2}, {1}},
        0.0, 1.0
    }, {
        new int[] {1, 2, 4, 8, 5, 2, 1},
        FilterBuilder.of().fir(-1.0, 0.0, 1.0).build(),
        new int[][] {{1}, {2}, {4 - 1}, {8 - 2}, {5 - 4}, {2 - 8}, {1 - 5}},
        1.0, 1.0
    }, {
        new int[] {1, 2, 4, 8, 5, 2, 1},
        FilterBuilder.of().fir(1.0, 2.0).build(),
        new int[][] {{2}, {5}, {10}, {20}, {18}, {9}, {4}},
        0.5, 1.0
    }, {
        new int[] {1, 2, 4},
        FilterBuilder.of().fir(-1.0, 0.0, 1.0).fir(1.0, 2.0).fir(2.0).fir(3.0).build(),
        new int[][] {{12}, {30}, {16 * 3}},
        1.5, 1.0
    }, {
        new int[] {1, 10, 100},
        FilterBuilder.of().fork(
            FilterBuilder.of().fir(2.0).build(),
            FilterBuilder.of().fir(3.0).build(),
            FilterBuilder.of().fir(4.0).build()).build(),
        new int[][] {{2, 3, 4}, {20, 30, 40}, {200, 300, 400}},
        0.0, 1.0
    }, {
        new int[] {1, 2, 4},
        FilterBuilder.of().fork(FilterBuilder.of().fir(1.0, 2.0).build(), FilterBuilder.of().fir(3.0).build()).build(),
        new int[][] {{2, 0}, {5, 3}, {10, 6}},
        1.0, 1.0
    }};
  }

  @DataProvider(name = "delay")
  public Object[][] delay() {
    return new Object[][] {{
        new int[] {1, 2, 4, 2, 2, 1},
        FilterBuilder.of().fork(
            FilterBuilder.of().fir(1.0).build(),
            FilterBuilder.of().fir(-1.0, 0.0, 1.0).build(),
            FilterBuilder.of().comb(2).build(),
            FilterBuilder.of().rrs(2).build()
        ).build(),
        new int[][] {{0, 1, 1, 0}, {1, 2, 2, 1}, {2, 3, 3, 3}, {4, 0, 0, 3}, {2, -2, -2, 2}, {2, -1, -1, 1}},
        1.0, 1.0
    }, {
        new int[] {1, 2, 4, 2, 2, 1},
        FilterBuilder.of().fork(
            FilterBuilder.of().fir(1.0).build(),
            FilterBuilder.of().fir(-1.0, 0.0, 1.0).build(),
            FilterBuilder.of().comb(2).build()
        ).buildNoDelay(),
        new int[][] {{1, 2, 2}, {2, 3, 3}, {4, 0, 0}, {2, -2, -2}, {2, -1, -1}},
        0.0, 1.0
    }, {
        new int[] {1, 2, 4, 2, 2, 1},
        FilterBuilder.of().comb(1).build(),
        new int[][] {{1}, {1}, {2}, {-2}, {0}, {-1}},
        0.5, 1.0
    }, {
        new int[] {-1, 1, MAX_VALUE, 1, MAX_VALUE},
        FilterBuilder.of().integrate().build(),
        new int[][] {{-1}, {0}, {MAX_VALUE}, {MIN_VALUE}, {-1}},
        0.0, 1.0
    }, {
        new int[] {1, 2, 4, 2, 2, 1},
        FilterBuilder.of().rrs(2).build(),
        new int[][] {{0}, {1}, {3}, {3}, {2}, {1}},
        1.0, 1.0
    }, {
        new int[] {10, 10, 10, 10, 10, 10},
        FilterBuilder.of().decimate(3).build(),
        new int[][] {{10}, {10}},
        -1.0 / 3.0, 1.0 / 3.0
    }, {
        new int[] {10, 10, 10, 10, 10, 10},
        FilterBuilder.of().decimate(3).buildNoDelay(),
        new int[][] {{10}, {10}},
        0.0, 1.0 / 3.0
    }, {
        new int[] {1, 4, 7},
        FilterBuilder.of().interpolate(3).build(),
        new int[][] {{0}, {0}, {1}, {2}, {3}, {4}, {5}, {6}, {7}},
        1.0, 3.0
    }, {
        new int[] {1, 4, 7},
        FilterBuilder.of().interpolate(3).buildNoDelay(),
        new int[][] {{0}, {1}, {2}, {3}, {4}, {5}, {6}, {7}},
        0.0, 3.0
    }, {
        new int[] {1, 4, 7},
        FilterBuilder.of().decimate(3).interpolate(3).buildNoDelay(),
        new int[][] {{1}, {2}, {4}},
        0.0, 1.0
    }};
  }

  @DataProvider(name = "strings")
  public Object[][] strings() {
    return new Object[][] {{
        FilterBuilder.of().build(),
        String.format("NoFilter (delay %.1f)", 0.0),
    }, {
        FilterBuilder.of().decimate(7).build(),
        String.format("LinearDecimationFilter (f / %.1f; delay %.1f)", 7.0, -0.4)
    }, {
        FilterBuilder.of().interpolate(7).buildNoDelay(),
        String.format("NoDelayFilter (compensate %.1f delay) - LinearInterpolationFilter (f · %.1f; delay %.1f)", 3.0, 7.0, 3.0)
    }, {
        FilterBuilder.of().fork(
            FilterBuilder.of().fir(1.0).build(),
            FilterBuilder.of().fir(-1.0, 0.0, 1.0).build(),
            FilterBuilder.of().comb(2).build(),
            FilterBuilder.of().rrs(4).build()
        ).buildNoDelay(),
        String.format(
            "NoDelayFilter (compensate %.1f delay) - FIRFilter (delay %.1f) - DelayFilter (delay %.1f)%n" +
                "                                       FIRFilter (delay %.1f) - DelayFilter (delay %.1f)%n" +
                "                                       CombFilter (delay %.1f) - DelayFilter (delay %.1f)%n" +
                "                                       RRS4 (delay %.1f)",
            2.0, 0.0, 2.0,
            1.0, 1.0,
            1.0, 1.0,
            2.0
        )
    }};
  }

  @Test(dataProvider = "simple")
  public void testWithLostZeroFilter(int[] input, DigitalFilter filter, int[][] result, double delay, double frequencyFactor) {
    filter.accept(0);
    testFilter(input, filter, result, delay, frequencyFactor);
  }

  @Test(dataProvider = "delay")
  public void testFilter(int[] input, DigitalFilter filter, int[][] result, double delay, double frequencyFactor) {
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
    for (int anInput : input) {
      filter.accept(anInput);
    }

    Assert.assertEquals(filteredCounter.get(), result.length, filter.toString());

    Assert.assertEquals(filter.getDelay(), delay, 1.0e-3, filter.toString());
    Assert.assertEquals(filter.getDelay(Quantities.getQuantity(0.2, MetricPrefix.KILO(Units.HERTZ))).getValue().doubleValue(),
        delay / 200.0, 1.0e-3, filter.toString());

    Assert.assertEquals(filter.getFrequencyFactor(), frequencyFactor, 1.0e-3, filter.toString());
    Assert.assertEquals(filter.getFrequency(Quantities.getQuantity(0.1, MetricPrefix.KILO(Units.HERTZ))).getValue().doubleValue(),
        100 * frequencyFactor, 1.0e-3, filter.toString());

    Assert.assertEquals(filter.size(), result[0].length);
  }

  @Test(expectedExceptions = IllegalStateException.class)
  public void testInvalidChain() {
    DigitalFilter filter = FilterBuilder.of().fir(2.0).build();
    FilterBuilder.of().fork(filter, filter).build();
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testInvalidAccept() {
    DigitalFilter filter1 = FilterBuilder.of().fir(1.0).build();
    DigitalFilter filter2 = FilterBuilder.of().fir(1.0).build();
    FilterBuilder.of().fork(filter1, filter2).fir(1.0).build().accept(1);
  }

  @Test(expectedExceptions = IllegalStateException.class)
  public void testInvalidForkLeft() {
    DigitalFilter filter1 = FilterBuilder.of().fir(1.0).build();
    FilterBuilder.of().fork(filter1, FilterBuilder.of().fir(2.0).build()).build();
    filter1.accept(1);
    filter1.accept(2);
  }

  @Test(expectedExceptions = IllegalStateException.class)
  public void testInvalidForkRight() {
    DigitalFilter filter2 = FilterBuilder.of().fir(4.0).build();
    FilterBuilder.of().fork(FilterBuilder.of().fir(3.0).build(), filter2).build();
    filter2.accept(1);
    filter2.accept(2);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testInvalidDecimateFactor() {
    FilterBuilder.of().decimate(0).build();
  }

  @Test(dataProvider = "strings")
  public void testToString(DigitalFilter filter, String toString) {
    Assert.assertEquals(filter.toString(), toString, filter.toString());
  }
}