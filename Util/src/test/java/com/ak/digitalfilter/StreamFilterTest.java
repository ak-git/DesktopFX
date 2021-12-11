package com.ak.digitalfilter;

import java.security.SecureRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.random.RandomGenerator;
import java.util.stream.IntStream;

import javax.annotation.Nonnull;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class StreamFilterTest {
  private static final RandomGenerator RANDOM = new SecureRandom();

  @DataProvider(name = "stream")
  public static Object[][] data() {
    return new Object[][] {{
        FilterBuilder.of().recursiveMean(1000).build(),
        IntStream.generate(() -> 1000).limit(5000), 1000, 1000
    }, {
        FilterBuilder.of().recursiveMean(1000).build(),
        IntStream.generate(() -> RANDOM.nextInt(1000)).limit(3000), 450, 550
    }, {
        FilterBuilder.of().recursiveMean(2000).build(),
        IntStream.generate(() -> (int) Math.round(RANDOM.nextGaussian() * 1000 + 1000)).limit(4000), 900, 1100
    }, {
        FilterBuilder.of().recursiveStd(1000).build(),
        IntStream.generate(() -> 1000).limit(5000), -1, 1
    }, {
        FilterBuilder.of().recursiveStd(1000).build(),
        IntStream.generate(() -> RANDOM.nextInt(1000)).limit(3000), 270, 310
    }, {
        FilterBuilder.of().recursiveStd(2000).build(),
        IntStream.generate(() -> (int) Math.round(RANDOM.nextGaussian() * 1000 + 1000)).limit(4000), 900, 1100
    }, {
        FilterBuilder.of().recursiveMeanAndStd(1000).biOperator(() -> (mean, std) -> mean).build(),
        IntStream.generate(() -> 1000).limit(5000), 1000, 1000
    }, {
        FilterBuilder.of().recursiveMeanAndStd(1000).biOperator(() -> (mean, std) -> mean).build(),
        IntStream.generate(() -> RANDOM.nextInt(1000)).limit(3000), 450, 550
    }, {
        FilterBuilder.of().recursiveMeanAndStd(2000).biOperator(() -> (mean, std) -> mean).build(),
        IntStream.generate(() -> (int) Math.round(RANDOM.nextGaussian() * 1000 + 1000)).limit(4000), 900, 1100
    }, {
        FilterBuilder.of().recursiveMeanAndStd(1000).biOperator(() -> (mean, std) -> std).build(),
        IntStream.generate(() -> 1000).limit(5000), -1, 1
    }, {
        FilterBuilder.of().recursiveMeanAndStd(1000).biOperator(() -> (mean, std) -> std).build(),
        IntStream.generate(() -> RANDOM.nextInt(1000)).limit(3000), 270, 310
    }, {
        FilterBuilder.of().recursiveMeanAndStd(2000).biOperator(() -> (mean, std) -> std).build(),
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
        IntStream.generate(() -> (int) Math.round(RANDOM.nextGaussian() * 1000)).limit(4000), 5000, 10000
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


  @Test(dataProvider = "stream", invocationCount = 100)
  public void testFilter(@Nonnull DigitalFilter filter, @Nonnull IntStream data, int min, int max) {
    AtomicInteger lastValue = new AtomicInteger();
    filter.forEach(values -> {
      Assert.assertEquals(values.length, 1);
      lastValue.set(values[0]);
    });
    data.forEach(filter::accept);
    Assert.assertTrue(lastValue.get() >= min && lastValue.get() <= max, "%d - %s - %d".formatted(min, lastValue.get(), max));
  }
}