package com.ak.digitalfilter;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import tec.uom.se.quantity.Quantities;
import tec.uom.se.unit.MetricPrefix;
import tec.uom.se.unit.Units;

public class FIRFilterTest {
  @DataProvider(name = "simple")
  public Object[][] simple() {
    return new Object[][] {{
        new int[] {1, 2, 4, 8, 5, 2, 1},
        FilterBuilder.of().build(),
        new int[][] {{1}, {2}, {4}, {8}, {5}, {2}, {1}},
        0.0
    }, {
        new int[] {1, 2, 4, 8, 5, 2, 1},
        FilterBuilder.of().fir(-1.0, 0.0, 1.0).build(),
        new int[][] {{1}, {2}, {4 - 1}, {8 - 2}, {5 - 4}, {2 - 8}, {1 - 5}},
        1.0
    }, {
        new int[] {1, 2, 4, 8, 5, 2, 1},
        FilterBuilder.of().fir(1.0, 2.0).build(),
        new int[][] {{2}, {5}, {10}, {20}, {18}, {9}, {4}},
        0.5
    }, {
        new int[] {1, 2, 4},
        FilterBuilder.of().fir(-1.0, 0.0, 1.0).fir(1.0, 2.0).fir(2.0).fir(3.0).build(),
        new int[][] {{12}, {30}, {16 * 3}},
        1.5
    }, {
        new int[] {1, 2, 4},
        FilterBuilder.of().fork(FilterBuilder.of().fir(2.0).build(), FilterBuilder.of().fir(3.0).build()).build(),
        new int[][] {{2, 3}, {4, 6}, {8, 12}},
        0.0
    }, {
        new int[] {1, 2, 4},
        FilterBuilder.of().fork(FilterBuilder.of().fir(1.0, 2.0).build(), FilterBuilder.of().fir(3.0).build()).build(),
        new int[][] {{2, 0}, {5, 3}, {10, 6}},
        1.0
    }, {
        new int[] {1, 2, 4},
        FilterBuilder.of().fork(FilterBuilder.of().fir(3.0).build(), FilterBuilder.of().fir(1.0, 2.0).build()).build(),
        new int[][] {{0, 2}, {3, 5}, {6, 10}},
        1.0
    }
    };
  }

  @Test(dataProvider = "simple")
  public void testSimpleFilter(int[] input, DigitalFilter filter, int[][] result, double delay) {
    AtomicInteger filteredCounter = new AtomicInteger();
    filter.accept(0);
    filter.forEach(new IntsAcceptor() {
      int i;

      @Override
      public void accept(int... value) {
        filteredCounter.incrementAndGet();
        Assert.assertEquals(value, result[i],
            String.format("Actual %s, expected %s", Arrays.toString(value), Arrays.toString(result[i])));
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
}