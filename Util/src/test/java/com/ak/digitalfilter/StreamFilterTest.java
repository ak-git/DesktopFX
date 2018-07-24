package com.ak.digitalfilter;

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
    }, {
        FilterBuilder.of().peakToPeak(2).build(),
        IntStream.of(-2, -1, 0, 1, 0, 1, 2, 1, 1, 2, 3, 4, 3), 0, 1
    }, {
        FilterBuilder.of().peakToPeak(1000).build(),
        IntStream.generate(() -> 1000).limit(5000), 0, 0
    }, {
        FilterBuilder.of().peakToPeak(1000).build(),
        IntStream.generate(() -> RANDOM.nextInt(1000)).limit(3000), 900, 1000
    }, {
        FilterBuilder.of().peakToPeak(2000).build(),
        IntStream.generate(() -> (int) Math.round(RANDOM.nextGaussian() * 1000)).limit(4000), 5000, 9000
    }, {
        FilterBuilder.of().rrs().build(),
        IntStream.generate(() -> 1000).limit(3000), 1000, 1000
    }, {
        FilterBuilder.of().rrs().build(),
        IntStream.generate(() -> RANDOM.nextInt(1000)).limit(3000), 400, 600
    }, {
        FilterBuilder.of().rrs().build(),
        IntStream.generate(() -> (int) Math.round(RANDOM.nextGaussian() * 1000 + 1000)).limit(4000).limit(3000), 900, 1100
    }};
  }


  @Test(dataProvider = "stream", successPercentage = 80, invocationCount = 100)
  public static void testFilter(@Nonnull DigitalFilter filter, @Nonnull IntStream data, int min, int max) {
    AtomicInteger lastValue = new AtomicInteger();
    filter.forEach(values -> {
      Assert.assertEquals(values.length, 1);
      lastValue.set(values[0]);
    });
    data.forEach(filter::accept);
    Assert.assertTrue(lastValue.get() >= min && lastValue.get() <= max, String.format("%d - %s - %d", min, lastValue.get(), max));
  }
}