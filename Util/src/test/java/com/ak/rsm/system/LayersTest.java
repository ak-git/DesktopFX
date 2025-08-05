package com.ak.rsm.system;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.byLessThan;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class LayersTest {
  static Stream<Arguments> k() {
    return Stream.of(
        arguments(1.0, 1.0, 0.0),
        arguments(1.0, Double.POSITIVE_INFINITY, 1.0),
        arguments(1.0, 0.0, -1.0),
        arguments(Double.POSITIVE_INFINITY, 1.0, -1.0),
        arguments(10, 1.0, -9.0 / 11.0),
        arguments(1.0, 10.0, 9.0 / 11.0)
    );
  }

  @ParameterizedTest
  @MethodSource("k")
  void testGetK12(double rho1, double rho2, double k) {
    assertThat(Layers.getK12(rho1, rho2)).isEqualTo(k);
  }

  static Stream<Arguments> rho() {
    return Stream.of(
        arguments(1.0, 0.0),
        arguments(0.0, 1.0),
        arguments(-1.0, Double.POSITIVE_INFINITY),
        arguments(-9.0 / 11.0, 10.0),
        arguments(9.0 / 11.0, 1.0 / 10.0)
    );
  }

  @ParameterizedTest
  @MethodSource("rho")
  void testGetRho1ToRho2(double k, double rho1ToRho2) {
    assertThat(Layers.getRho1ToRho2(k)).isCloseTo(rho1ToRho2, byLessThan(0.001));
  }

  @Test
  void testSum() {
    assertThat(Layers.sum(x -> x)).isEqualTo((1 << 14) * ((1 << 15) + 1.0));
  }

  static Stream<Arguments> qn() {
    return Stream.of(
        arguments(0, 0, 1, 4, new double[] {0.0, 0.0, 0.0, 0.0, 0.0}),
        arguments(-1, 0, 1, 0, new double[] {-1.0}),
        arguments(-1, 0.2, 1, 0, new double[] {-1.0}),
        arguments(-1, -1, 1, 2, new double[] {-1.0, 1.0, -1.0}),
        arguments(-1, 1, 1, 1, new double[] {-1.0, 1.0}),
        arguments(-1, 1, 1, 2, new double[] {-1.0, 1.0, -1.0}),
        arguments(-1, 1, 2, 2, new double[] {0.0, -1.0, 0.0, 1.0}),
        arguments(-0.5, 0.5, 1, 4, new double[] {-0.5, 0.25, -0.125, 0.0625, 0.34375}),
        arguments(0.5, -0.5, 1, 1, new double[] {0.5, -0.125})
    );
  }

  @ParameterizedTest
  @MethodSource("qn")
  void testQ(double k12, double k23, int p1, int p2mp1, double[] expected) {
    double[] actual = Arrays.copyOfRange(Layers.qn(k12, k23, p1, p2mp1), 1, p1 + p2mp1 + 1);
    assertThat(actual).containsExactly(expected, byLessThan(1.0e-6));
  }
}