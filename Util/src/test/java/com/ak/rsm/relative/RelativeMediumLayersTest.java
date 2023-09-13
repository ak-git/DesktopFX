package com.ak.rsm.relative;

import com.ak.math.ValuePair;
import com.ak.util.Metrics;
import com.ak.util.Strings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class RelativeMediumLayersTest {
  static Stream<Arguments> layer2Medium() {
    return Stream.of(
        arguments(new RelativeMediumLayers(1.0, Metrics.fromMilli(5.0)),
            new ValuePair[] {ValuePair.Name.K12.of(1.0, 0.0), ValuePair.Name.H_L.of(Metrics.fromMilli(5.0), 0.0)}
        ),
        arguments(
            new RelativeMediumLayers(new double[] {1.0, Double.POSITIVE_INFINITY}, Metrics.fromMilli(5.0)),
            new ValuePair[] {ValuePair.Name.K12.of(1.0, 0.0), ValuePair.Name.H_L.of(Metrics.fromMilli(5.0), 0.0)}
        ),
        arguments(
            new RelativeMediumLayers(new double[] {-1.0, Metrics.fromMilli(5.0)}),
            new ValuePair[] {ValuePair.Name.K12.of(-1.0, 0.0), ValuePair.Name.H_L.of(Metrics.fromMilli(5.0), 0.0)}
        ),
        arguments(
            new RelativeMediumLayers(ValuePair.Name.NONE.of(1.0, 0.0), ValuePair.Name.NONE.of(Metrics.fromMilli(5.0), Metrics.fromMilli(0.1))),
            new ValuePair[] {ValuePair.Name.NONE.of(1.0, 0.0), ValuePair.Name.NONE.of(Metrics.fromMilli(5.0), Metrics.fromMilli(0.1))}
        )
    );
  }

  @ParameterizedTest
  @MethodSource("layer2Medium")
  @ParametersAreNonnullByDefault
  void test(RelativeMediumLayers layers, ValuePair[] expected) {
    assertThat(layers.k().value()).isEqualTo(expected[0].value());
    assertThat(layers.k().absError()).isEqualTo(expected[0].absError());
    assertThat(layers.hToL().value()).isEqualTo(expected[1].value());
    assertThat(layers.hToL().absError()).isEqualTo(expected[1].absError());
    assertThat(layers.size()).isEqualTo(2);
  }

  @ParameterizedTest
  @MethodSource("layer2Medium")
  @ParametersAreNonnullByDefault
  <T> void testToString(RelativeMediumLayers layers, T[] expected) {
    assertThat(layers.toString()).contains(expected[0].toString());
    assertThat(layers.toString()).contains(expected[1].toString());
  }

  @Test
  void testSingleLayer() {
    assertThat(RelativeMediumLayers.SINGLE_LAYER.k().value()).isZero();
    assertThat(RelativeMediumLayers.SINGLE_LAYER.k().absError()).isZero();
    assertThat(RelativeMediumLayers.SINGLE_LAYER.hToL().value()).isNaN();
    assertThat(RelativeMediumLayers.SINGLE_LAYER.hToL().absError()).isNaN();
    assertThat(RelativeMediumLayers.SINGLE_LAYER).hasToString(Strings.EMPTY);
    assertThat(RelativeMediumLayers.SINGLE_LAYER.size()).isEqualTo(1);
  }

  @Test
  void testNaN() {
    assertThat(RelativeMediumLayers.NAN.k().value()).isNaN();
    assertThat(RelativeMediumLayers.NAN.k().absError()).isNaN();
    assertThat(RelativeMediumLayers.NAN.hToL().value()).isNaN();
    assertThat(RelativeMediumLayers.NAN.hToL().absError()).isNaN();
    assertThat(RelativeMediumLayers.NAN.size()).isEqualTo(1);
    assertThat(Double.toString(Double.NaN)).hasToString(RelativeMediumLayers.NAN.toString());
  }
}