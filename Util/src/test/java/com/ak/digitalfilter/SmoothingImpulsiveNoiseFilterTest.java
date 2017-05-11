package com.ak.digitalfilter;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class SmoothingImpulsiveNoiseFilterTest {
  private SmoothingImpulsiveNoiseFilterTest() {
  }

  @DataProvider(name = "data")
  public static Object[][] data() {
    return new Object[][] {{
        new int[] {1, 2, 3, 4, 5, 6}, 3, new int[] {0, 0, 1, 2, 3, 4}
    }, {
        new int[] {10, 10, 11, 9, 10, 10, 13, 10, 10, 10, 100, -1}, 10, new int[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10}
    }};
  }


  @Test(dataProvider = "data")
  public static void testGetSorted(@Nonnull int[] data, @Nonnegative int size, @Nonnull int[] expected) {
    SmoothingImpulsiveNoiseFilter filter = new SmoothingImpulsiveNoiseFilter(size);

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
  }

  @Test
  public static void testDelay() {
    Assert.assertEquals(new SmoothingImpulsiveNoiseFilter(5).getDelay(), 1.0);
  }
}