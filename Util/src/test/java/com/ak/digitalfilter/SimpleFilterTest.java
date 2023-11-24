package com.ak.digitalfilter;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.byLessThan;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class SimpleFilterTest {
  static Stream<Arguments> data() {
    return Stream.of(
        arguments(
            FilterBuilder.of().fir(() -> new double[] {1.0, 1.0, 1.0}).build(),
            new int[] {1, 2, 3, 4, 5, 6},
            new int[] {1, 3, 6, 9, 12, 15},
            1.0
        ),
        arguments(
            FilterBuilder.of().smoothingImpulsive(3).build(),
            new int[] {1, 2, 3, 4, 5, 6},
            new int[] {0, 1, 2, 3, 4, 5},
            1.0
        ),
        arguments(
            FilterBuilder.of().smoothingImpulsive(10).build(),
            new int[] {10, 10, 11, 9, 10, 10, 13, 10, 100, -1},
            new int[] {0, 0, 0, 0, 1, 1, 1, 1, 1, 2},
            24.5
        ),
        arguments(
            FilterBuilder.of().decimate(3).build(),
            new int[] {9, 9, 9, 9, 9, 9},
            new int[] {9, 9},
            0.0
        ),
        arguments(
            FilterBuilder.of().decimate(() -> new double[] {1.0 / 3.0, 1.0 / 3.0, 1.0 / 3.0}, 3).build(),
            new int[] {9, 9, 9, 9, 9, 9},
            new int[] {9, 9},
            0.0
        ),
        arguments(
            FilterBuilder.of().interpolate(3).build(),
            new int[] {9, 9, 9},
            new int[] {3, 6, 9, 9, 9, 9, 9, 9, 9},
            1.0
        ),
        arguments(
            FilterBuilder.of().interpolate(3, () -> new double[] {1.0 / 3.0, 1.0 / 3.0, 1.0 / 3.0}).build(),
            new int[] {9, 9, 9},
            new int[] {3, 6, 9, 9, 9, 9, 9, 9, 9},
            1.0
        ),
        arguments(
            FilterBuilder.of().decimate(4).build(),
            new int[] {9, 9, 9, 9, 10, 10, 10, 10},
            new int[] {4, 9},
            0.5
        ),
        arguments(
            FilterBuilder.of().interpolate(1).decimate(1).build(),
            new int[] {100, 110, 120, 130, 140, 150, 160, 170},
            new int[] {100, 110, 120, 130, 140, 150, 160, 170},
            0.0
        ),
        arguments(
            FilterBuilder.of().interpolate(2).decimate(2).build(),
            new int[] {100, 110, 120, 130, 140, 150, 160, 170},
            new int[] {75, 107, 117, 127, 137, 147, 157, 167},
            0.25
        ),
        arguments(
            FilterBuilder.of().interpolate(3).decimate(3).build(),
            new int[] {100, 110, 120, 130, 140, 150, 160, 170},
            new int[] {66, 106, 116, 126, 136, 146, 156, 166},
            1.0 / 3.0
        ),
        arguments(
            FilterBuilder.of().interpolate(4).decimate(4).build(),
            new int[] {100, 110, 120, 130, 140, 150, 160, 170},
            new int[] {15, 57, 97, 116, 126, 136, 146, 156},
            1.375
        ),
        arguments(
            FilterBuilder.of().decimate(1).interpolate(1).build(),
            new int[] {100, 110, 120, 130, 140, 150, 160, 170},
            new int[] {100, 110, 120, 130, 140, 150, 160, 170},
            0.0
        ),
        arguments(
            FilterBuilder.of().decimate(2).interpolate(2).build(),
            new int[] {100, 110, 120, 130, 140, 150, 160, 170},
            new int[] {52, 105, 115, 125, 135, 145, 155, 165},
            0.5
        ),
        arguments(
            FilterBuilder.of().decimate(3).interpolate(3).build(),
            new int[] {100, 110, 120, 130, 140, 150, 160, 170, 180},
            new int[] {36, 73, 110, 120, 130, 140, 150, 160, 170},
            1.0
        ),
        arguments(
            FilterBuilder.of().decimate(4).interpolate(4).build(),
            new int[] {100, 110, 120, 130, 140, 150, 160, 170},
            new int[] {7, 14, 21, 28, 45, 62, 79, 96},
            5.5
        ),
        arguments(
            FilterBuilder.of().expSum().build(),
            new int[] {100, 110, 120, 130, 140, 150, 160, 170},
            new int[] {100, 100, 100, 100, 101, 102, 103, 104},
            0.0
        ),
        arguments(
            FilterBuilder.of().average(3).build(),
            new int[] {1, 2, 3, 4, 5, 6},
            new int[] {0, 1, 2, 3, 4, 5},
            1.0
        ),
        arguments(
            FilterBuilder.of().sharpingDecimate(1).build(),
            new int[] {1, 2, 3},
            new int[] {1, 2, 3},
            0.0
        ),
        arguments(
            FilterBuilder.of().sharpingDecimate(2).build(),
            new int[] {1, 2, 3},
            new int[] {2},
            0.0
        ),
        arguments(
            FilterBuilder.of().sharpingDecimate(2).build(),
            new int[] {1, 1, 2, 0, 2, -1, 20, -1},
            new int[] {1, 0, 2, 20},
            0.0
        ),
        arguments(
            FilterBuilder.of().sharpingDecimate(3).build(),
            new int[] {1, 1, 2, 0, 2, -1, 20, -1},
            new int[] {2, -1},
            0.0
        ),
        arguments(
            FilterBuilder.of().sharpingDecimate(4).build(),
            new int[] {1, 1, -2, 0, 2, -1, 20, -1},
            new int[] {-2, 20},
            0.0
        ),
        arguments(
            FilterBuilder.of().peakToPeak(3).build(),
            new int[] {1, 2, 3, 4, 5, 6, 7, 8},
            new int[] {1, 2, 2, 2, 2, 2, 2, 2},
            0.0
        ),
        arguments(
            FilterBuilder.of().peakToPeak(3).build(),
            new int[] {-1, -2, -3, -4, -5, -6, -7, -8},
            new int[] {1, 2, 2, 2, 2, 2, 2, 2},
            0.0
        ),
        arguments(
            FilterBuilder.of().peakToPeak(3).build(),
            new int[] {1, -2, 3, -4, 5, -6, 7, -8},
            new int[] {1, 3, 5, 7, 9, 11, 13, 15},
            0.0
        ),
        arguments(
            FilterBuilder.of().rrs().build(),
            new int[] {10, 11, 9, 11, 9, 11, 9},
            new int[] {10, 11, 10, 10, 10, 10, 10},
            0.0
        ),
        arguments(
            FilterBuilder.of().recursiveMean(7).build(),
            new int[] {-10, -30, -50, -70, -90, -110, -130, -70 - 10, -70 - 30},
            new int[] {-10, -20, -30, -40, -50, -60, -70, -80, -90},
            0.0
        ),
        arguments(
            FilterBuilder.of().recursiveStd(4).build(),
            new int[] {100, -100, 100, -100, 100, -100, 100, -100, 100, -100, 100, -100, 100, -100},
            new int[] {0, 70, 69, 78, 92, 92, 100, 100, 100, 100, 100, 100, 100, 100},
            0.0
        ),
        arguments(
            FilterBuilder.of().recursiveMeanAndStd(7).biOperator(() -> (mean, std) -> mean).build(),
            new int[] {-10, -30, -50, -70, -90, -110, -130, -70 - 10, -70 - 30},
            new int[] {-10, -20, -30, -40, -50, -60, -70, -80, -90},
            0.0
        ),
        arguments(
            FilterBuilder.of().recursiveMeanAndStd(4).biOperator(() -> (mean, std) -> std).build(),
            new int[] {100, -100, 100, -100, 100, -100, 100, -100, 100, -100, 100, -100, 100, -100},
            new int[] {0, 70, 69, 78, 92, 92, 100, 100, 100, 100, 100, 100, 100, 100},
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
            FilterBuilder.of().fork(FilterBuilder.of().expSum().build(), FilterBuilder.of().fir(new double[] {1.0, 1.0, 1.0}).build()).buildNoDelay(),
            new int[] {100, 110, 120, 130, 140, 150, 160, 170},
            new int[][] {
                {
                    100, 100, 100, 101, 102, 103,
                    100, 100, 100, 101, 102, 103
                },
                {
                    330, 360, 390, 420, 450, 480,
                    330, 360, 390, 420, 450, 480
                }
            }
        ),
        arguments(
            FilterBuilder.of().rrs().build(),
            new int[] {10, 11, 9, 11, 9, 11, 9},
            new int[][] {
                {
                    10, 11, 10, 10, 10, 10, 10,
                    10, 11, 10, 10, 10, 10, 10
                }
            }
        ),
        arguments(
            FilterBuilder.of().recursiveMean(4).build(),
            new int[] {4, 2, 0, 2},
            new int[][] {
                {
                    4, 3, 2, 2, 0, 0, 0, 0
                }
            }
        ),
        arguments(
            FilterBuilder.of().recursiveStd(5).build(),
            new int[] {100, -100, 100, -100, 0, -100, 100, -100, 100},
            new int[][] {
                {
                    0, 70, 69, 78, 69, 74, 74, 73, 73,
                    35, 58, 52, 69, 53, 48, 37, 29, 0
                }
            }
        ),
        arguments(
            FilterBuilder.parallel(
                Collections.singletonList(new int[] {0}),
                FilterBuilder.of().fir(new double[] {1.0, 1.0, 1.0}).build()
            ),
            new int[] {100, -100, 100, -100, 0, -100, 100, -100, 100},
            new int[][] {
                {
                    100, -100, 0, -200, 0, -100, 100,
                    100, -100, 0, -200, 0, -100, 100
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