package com.ak.digitalfilter.briko;

import com.ak.digitalfilter.DigitalFilter;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.byLessThan;

class SimpleFilterUtils {
  private SimpleFilterUtils() {
  }

  @ParametersAreNonnullByDefault
  static void testFilter(DigitalFilter filter, int[] data, int[] expected, double delay) {
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

  @ParametersAreNonnullByDefault
  static void testFilterWithReset(DigitalFilter filter, int[] data, int[][] expected) {
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
