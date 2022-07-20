package com.ak.digitalfilter;

import java.security.SecureRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.random.RandomGenerator;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.annotation.ParametersAreNonnullByDefault;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class StreamFilterTest {
  private static final RandomGenerator RANDOM = new SecureRandom();

  static Stream<Arguments> data() {
    return Stream.of(
        arguments(
            FilterBuilder.of().recursiveMean(1000).build(),
            IntStream.generate(() -> 1000).limit(5000), 1000, 1000),
        arguments(
            FilterBuilder.of().recursiveMean(1000).build(),
            IntStream.generate(() -> RANDOM.nextInt(1000)).limit(3000), 450, 550
        ),
        arguments(
            FilterBuilder.of().recursiveMean(2000).build(),
            IntStream.generate(() -> (int) Math.round(RANDOM.nextGaussian() * 1000 + 1000)).limit(4000), 900, 1100
        ),
        arguments(
            FilterBuilder.of().recursiveStd(1000).build(),
            IntStream.generate(() -> 1000).limit(5000), -1, 1
        ),
        arguments(
            FilterBuilder.of().recursiveStd(1000).build(),
            IntStream.generate(() -> RANDOM.nextInt(1000)).limit(3000), 270, 310
        ),
        arguments(
            FilterBuilder.of().recursiveStd(2000).build(),
            IntStream.generate(() -> (int) Math.round(RANDOM.nextGaussian() * 1000 + 1000)).limit(4000), 900, 1100
        ),
        arguments(
            FilterBuilder.of().recursiveMeanAndStd(1000).biOperator(() -> (mean, std) -> mean).build(),
            IntStream.generate(() -> 1000).limit(5000), 1000, 1000
        ),
        arguments(
            FilterBuilder.of().recursiveMeanAndStd(1000).biOperator(() -> (mean, std) -> mean).build(),
            IntStream.generate(() -> RANDOM.nextInt(1000)).limit(3000), 450, 550
        ),
        arguments(
            FilterBuilder.of().recursiveMeanAndStd(2000).biOperator(() -> (mean, std) -> mean).build(),
            IntStream.generate(() -> (int) Math.round(RANDOM.nextGaussian() * 1000 + 1000)).limit(4000), 900, 1100
        ),
        arguments(
            FilterBuilder.of().recursiveMeanAndStd(1000).biOperator(() -> (mean, std) -> std).build(),
            IntStream.generate(() -> 1000).limit(5000), -1, 1
        ),
        arguments(
            FilterBuilder.of().recursiveMeanAndStd(1000).biOperator(() -> (mean, std) -> std).build(),
            IntStream.generate(() -> RANDOM.nextInt(1000)).limit(3000), 270, 310
        ),
        arguments(
            FilterBuilder.of().recursiveMeanAndStd(2000).biOperator(() -> (mean, std) -> std).build(),
            IntStream.generate(() -> (int) Math.round(RANDOM.nextGaussian() * 1000 + 1000)).limit(4000), 900, 1100
        ),
        arguments(
            FilterBuilder.of().peakToPeak(2).build(),
            IntStream.of(-2, -1, 0, 1, 0, 1, 2, 1, 1, 2, 3, 4, 3), 0, 1
        ),
        arguments(
            FilterBuilder.of().peakToPeak(1000).build(),
            IntStream.generate(() -> 1000).limit(5000), 0, 0
        ),
        arguments(
            FilterBuilder.of().peakToPeak(1000).build(),
            IntStream.generate(() -> RANDOM.nextInt(1000)).limit(3000), 900, 1000
        ),
        arguments(
            FilterBuilder.of().peakToPeak(2000).build(),
            IntStream.generate(() -> (int) Math.round(RANDOM.nextGaussian() * 1000)).limit(4000), 5000, 10000
        ),
        arguments(
            FilterBuilder.of().rrs().build(),
            IntStream.generate(() -> 1000).limit(3000), 1000, 1000
        ),
        arguments(
            FilterBuilder.of().rrs().build(),
            IntStream.generate(() -> RANDOM.nextInt(1000)).limit(3000), 400, 600
        ),
        arguments(
            FilterBuilder.of().rrs().build(),
            IntStream.generate(() -> (int) Math.round(RANDOM.nextGaussian() * 1000 + 1000)).limit(4000).limit(3000), 900, 1100
        )
    );
  }

  @ParameterizedTest
  @MethodSource("data")
  @ParametersAreNonnullByDefault
  void testFilter(DigitalFilter filter, IntStream data, int min, int max) {
    AtomicInteger lastValue = new AtomicInteger();
    filter.forEach(values -> {
      assertThat(values).hasSize(1);
      lastValue.set(values[0]);
    });
    data.forEach(filter::accept);
    assertTrue(lastValue.get() >= min && lastValue.get() <= max, "%d - %s - %d".formatted(min, lastValue.get(), max));
  }
}