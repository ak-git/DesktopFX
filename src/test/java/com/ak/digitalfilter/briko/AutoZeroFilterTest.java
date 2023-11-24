package com.ak.digitalfilter.briko;

import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.FilterBuilder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.byLessThan;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class AutoZeroFilterTest {
  static Stream<Arguments> data() {
    return Stream.of(
        arguments(
            FilterBuilder.of().chain(new AutoZeroFilter(2)).build(),
            new int[] {1, 2, 3, 4, 5, 6},
            new int[] {0, 0, 0, 0, 1, 2},
            0.0
        ),
        arguments(
            FilterBuilder.of().chain(new AutoZeroFilter(0)).build(),
            new int[] {1, 2, 3, 4, 5, 6},
            new int[] {1, 2, 3, 4, 5, 6},
            0.0
        )
    );
  }

  @ParameterizedTest
  @MethodSource("data")
  @ParametersAreNonnullByDefault
  void testFilter(DigitalFilter filter, int[] data, int[] expected, double delay) {
    AtomicInteger index = new AtomicInteger();
    int[] actual = new int[expected.length];
    filter.forEach(values -> {
      assertThat(values).hasSize(1);
      actual[index.getAndIncrement()] = values[0];
    });
    for (int n : data) {
      filter.accept(n);
    }
    assertThat(index.get()).isEqualTo(expected.length);
    assertThat(actual).containsExactly(expected);
    assertThat(filter.getDelay()).isCloseTo(delay, byLessThan(0.001));
  }

  static Stream<Arguments> dataWithReset() {
    return Stream.of(
        arguments(
            FilterBuilder.of().chain(new AutoZeroFilter(2)).build(),
            new int[] {1, 2, 3, 4, 5, 6},
            new int[][] {
                {
                    0, 0, 0, 0, 1, 2,
                    0, 0, 0, 0, 1, 2
                }
            }
        ),
        arguments(
            FilterBuilder.of().chain(new AutoZeroFilter(0)).build(),
            new int[] {1, 2, 3, 4, 5, 6},
            new int[][] {
                {
                    1, 2, 3, 4, 5, 6,
                    1, 2, 3, 4, 5, 6
                }
            }
        )
    );
  }

  @ParameterizedTest
  @MethodSource("dataWithReset")
  void testFilterWithReset(DigitalFilter filter, int[] data, int[][] expected) {
    AtomicInteger index = new AtomicInteger();
    int[][] actual = new int[expected.length][expected[0].length];
    filter.forEach(values -> {
      assertThat(values).hasSameSizeAs(expected);
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
    assertThat(index.get()).isEqualTo(expected[0].length);
    assertThat(actual).isEqualTo(expected);
  }
}