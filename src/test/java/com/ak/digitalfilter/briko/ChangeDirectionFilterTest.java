package com.ak.digitalfilter.briko;

import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.FilterBuilder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;

class ChangeDirectionFilterTest {
  static Stream<Arguments> data() {
    return Stream.of(
        arguments(
            FilterBuilder.of().chain(new ChangeDirectionFilter(1)).build(),
            new int[] {1, 2, 3, 3, 3, 4, 5},
            new int[] {0, 0, 0, 0, 0, 1, 0},
            0.0
        ),
        arguments(
            FilterBuilder.of().chain(new ChangeDirectionFilter(0)).build(),
            new int[] {1, 2, 3, 4, 5, 6},
            new int[] {0, 1, 0, 0, 0, 0},
            0.0
        )
    );
  }

  @ParameterizedTest
  @MethodSource("data")
  @ParametersAreNonnullByDefault
  void testFilter(DigitalFilter filter, int[] data, int[] expected, double delay) {
    SimpleFilterUtils.testFilter(filter, data, expected, delay);
  }

  static Stream<Arguments> dataWithReset() {
    return Stream.of(
        arguments(
            FilterBuilder.of().chain(new ChangeDirectionFilter(2)).build(),
            new int[] {1, 2, 3, 3, 3, 3, 4, 5},
            new int[][] {
                {
                    0, 0, 0, 0, 0, 0, 1, 0,
                    0, 0, 0, 0, 0, 0, 1, 0
                }
            }
        ),
        arguments(
            FilterBuilder.of().chain(new ChangeDirectionFilter(0)).build(),
            new int[] {1, 2, 3, 4, 5, 6},
            new int[][] {
                {
                    0, 1, 0, 0, 0, 0,
                    0, 1, 0, 0, 0, 0
                }
            }
        )
    );
  }

  @ParameterizedTest
  @MethodSource("dataWithReset")
  @ParametersAreNonnullByDefault
  void testFilterWithReset(DigitalFilter filter, int[] data, int[][] expected) {
    SimpleFilterUtils.testFilterWithReset(filter, data, expected);
  }
}