package com.ak.rsm.system;

import com.ak.math.Simplex;
import org.apache.commons.math3.optim.PointValuePair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.byLessThan;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class RelativeTetrapolarSystemTest {
  static Stream<Arguments> tetrapolarSystems() {
    RelativeTetrapolarSystem ts = new RelativeTetrapolarSystem(2.0);
    return Stream.of(
        arguments(ts, ts, true),
        arguments(ts, new RelativeTetrapolarSystem(1.0 / 2.0), true),
        arguments(new RelativeTetrapolarSystem(1.0 / 2.0), new RelativeTetrapolarSystem(1.0 / 3.0), false)
    );
  }

  @ParameterizedTest
  @MethodSource("tetrapolarSystems")
  void testEquals(RelativeTetrapolarSystem system1, RelativeTetrapolarSystem system2, boolean equals) {
    assertThat(system1.equals(system2)).withFailMessage("%s compared with %s", system1, system2).isEqualTo(equals);
    assertThat(system1.hashCode() == system2.hashCode())
        .withFailMessage("%s compared with %s", system1, system2).isEqualTo(equals);
    assertThat(system1).isNotEqualTo(new Object());
    assertThat(new Object()).isNotEqualTo(system1);
  }

  @Test
  void testNotEquals() {
    RelativeTetrapolarSystem system = new RelativeTetrapolarSystem(1.0 / 2.0);
    assertThat(system).isNotEqualTo(new Object());
    assertThat(new Object()).isNotEqualTo(system);
  }

  static DoubleStream relativeTetrapolarSystems() {
    return DoubleStream.of(2.0, 0.5, 1.0 / 3.0, 3.0);
  }

  @ParameterizedTest
  @MethodSource("relativeTetrapolarSystems")
  void testToString(double sToL) {
    RelativeTetrapolarSystem system = new RelativeTetrapolarSystem(sToL);
    assertThat(system).hasToString("s / L = %.3f".formatted(sToL));
  }

  @ParameterizedTest
  @MethodSource("relativeTetrapolarSystems")
  void testHash(double sToL) {
    RelativeTetrapolarSystem system = new RelativeTetrapolarSystem(sToL);
    assertThat(system).hasSameHashCodeAs(Double.hashCode(Math.min(sToL, 1.0 / sToL)));
  }

  @ParameterizedTest
  @MethodSource("relativeTetrapolarSystems")
  void tesFactor(double sToL) {
    RelativeTetrapolarSystem system = new RelativeTetrapolarSystem(sToL);
    assertThat(system.factor(1.0)).isCloseTo(Math.abs(1.0 + sToL), byLessThan(0.1));
    assertThat(system.factor(-1.0)).isCloseTo(Math.abs(1.0 - sToL), byLessThan(0.1));
  }

  @ParameterizedTest
  @MethodSource("relativeTetrapolarSystems")
  void testErrorFactor(double sToL) {
    RelativeTetrapolarSystem system = new RelativeTetrapolarSystem(sToL);
    assertThat(system.errorFactor()).isCloseTo(6.0, byLessThan(0.1));
  }

  @Test
  void testHMaxFactor() {
    double random = new Random().nextDouble(0.5, 1.0);
    PointValuePair pair = Simplex.optimizeAll(x -> 1.0 - new RelativeTetrapolarSystem(x[0]).hMaxFactor(random),
        new Simplex.Bounds(0.0, 1.0)
    );
    assertThat(pair.getPoint()[0]).withFailMessage(Arrays.toString(pair.getPoint()))
        .isCloseTo(1.0 / 3.0, byLessThan(0.001));
  }

  @Test
  void testHMinFactor() {
    PointValuePair pair = Simplex.optimizeAll(ks -> new RelativeTetrapolarSystem(ks[1]).hMinFactor(ks[0]),
        new Simplex.Bounds(-1.0, 0.0), new Simplex.Bounds(0.0, 0.9)
    );
    assertThat(pair.getPoint()[0]).withFailMessage(Arrays.toString(pair.getPoint())).isCloseTo(-1.0, byLessThan(0.01));
  }
}