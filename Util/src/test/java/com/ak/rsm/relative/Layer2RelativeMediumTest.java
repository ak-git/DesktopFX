package com.ak.rsm.relative;

import java.util.stream.Stream;

import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.math.ValuePair;
import com.ak.util.Metrics;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class Layer2RelativeMediumTest {
  static Stream<Arguments> layer2Medium() {
    return Stream.of(
        arguments(new Layer2RelativeMedium(1.0, Metrics.fromMilli(5.0)),
            new ValuePair[] {ValuePair.Name.K12.of(1.0, 0.0), ValuePair.Name.H_L.of(Metrics.fromMilli(5.0), 0.0)}
        ),
        arguments(
            new Layer2RelativeMedium(new double[] {1.0, Double.POSITIVE_INFINITY}, Metrics.fromMilli(5.0)),
            new ValuePair[] {ValuePair.Name.K12.of(1.0, 0.0), ValuePair.Name.H_L.of(Metrics.fromMilli(5.0), 0.0)}
        ),
        arguments(
            new Layer2RelativeMedium(new double[] {-1.0, Metrics.fromMilli(5.0)}),
            new ValuePair[] {ValuePair.Name.K12.of(-1.0, 0.0), ValuePair.Name.H_L.of(Metrics.fromMilli(5.0), 0.0)}
        ),
        arguments(
            new Layer2RelativeMedium(ValuePair.Name.NONE.of(1.0, 0.0), ValuePair.Name.NONE.of(Metrics.fromMilli(5.0), Metrics.fromMilli(0.1))),
            new ValuePair[] {ValuePair.Name.NONE.of(1.0, 0.0), ValuePair.Name.NONE.of(Metrics.fromMilli(5.0), Metrics.fromMilli(0.1))}
        )
    );
  }

  @ParameterizedTest
  @MethodSource("layer2Medium")
  @ParametersAreNonnullByDefault
  <T> void test(RelativeMediumLayers layers, ValuePair[] expected) {
    assertThat(layers.k12()).isEqualTo(expected[0].value());
    assertThat(layers.k12AbsError()).isEqualTo(expected[0].absError());
    assertThat(layers.hToL()).isEqualTo(expected[1].value());
    assertThat(layers.hToLAbsError()).isEqualTo(expected[1].absError());
  }

  @ParameterizedTest
  @MethodSource("layer2Medium")
  @ParametersAreNonnullByDefault
  <T> void testToString(RelativeMediumLayers layers, T[] expected) {
    assertThat(layers.toString()).contains(expected[0].toString());
    assertThat(layers.toString()).contains(expected[1].toString());
  }

  @ParameterizedTest
  @MethodSource("layer2Medium")
  @ParametersAreNonnullByDefault
  void testEquals(RelativeMediumLayers layers, ValuePair[] expected) {
    assertThat(layers).isEqualTo(layers).hasSameHashCodeAs(layers);

    RelativeMediumLayers copy = new Layer2RelativeMedium(expected[0], expected[1]);
    assertThat(layers).isEqualTo(copy);
    assertThat(copy).isEqualTo(layers);
    assertThat(layers).isNotEqualTo(null);
    assertThat(new Object()).isNotEqualTo(layers);
    assertThat(layers).isNotEqualTo(new Object());
  }
}