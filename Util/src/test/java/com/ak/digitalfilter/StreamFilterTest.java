package com.ak.digitalfilter;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import javax.annotation.Nonnull;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class StreamFilterTest {
  private static final Random RANDOM = new Random();

  private StreamFilterTest() {
  }

  @DataProvider(name = "stream")
  public static Object[][] data() {
    return new Object[][] {{
        FilterBuilder.of().std(1000).build(),
        IntStream.generate(() -> 1000).limit(5000), -1, 1
    }, {
        FilterBuilder.of().std(1000).build(),
        IntStream.generate(() -> RANDOM.nextInt(1000)).limit(3000), 270, 310
    }, {
        FilterBuilder.of().std(2000).build(),
        IntStream.generate(() -> (int) Math.round(RANDOM.nextGaussian() * 1000 + 1000)).limit(4000), 900, 1100
    }};
  }


  @Test(dataProvider = "stream", successPercentage = 80, invocationCount = 100)
  public static void testFilter(@Nonnull DigitalFilter filter, @Nonnull IntStream data, int min, int max) {
    AtomicInteger counter = new AtomicInteger();
    filter.forEach(values -> {
      Assert.assertEquals(values.length, 1);
      if (counter.incrementAndGet() > filter.getDelay() * 2) {
        Assert.assertTrue(values[0] >= min && values[0] <= max, String.format("%d - %s - %d", min, Arrays.toString(values), max));
      }
    });
    data.forEach(filter::accept);
  }
}