package com.ak.digitalfilter;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nonnull;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class DecimationAndInterpolationFilterTest {
  private DecimationAndInterpolationFilterTest() {
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
        new int[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10},
        4.5
    }, {
        FilterBuilder.of().decimate(3).build(),
        new int[] {9, 9, 9, 9, 9, 9},
        new int[] {9, 9},
        0.0
    }, {
        FilterBuilder.of().interpolate(3).build(),
        new int[] {9, 9, 9},
        new int[] {3, 6, 9, 9, 9, 9, 9, 9, 9},
        1.0
    }, {
        FilterBuilder.of().decimate(4).build(),
        new int[] {9, 9, 9, 9, 10, 10, 10, 10},
        new int[] {9, 10},
        0.0
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
        new int[] {62, 106, 116, 126, 136, 146, 156, 166},
        0.375
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
        new int[] {28, 57, 86, 115, 125, 135, 145, 155},
        1.5
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
}