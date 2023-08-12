package com.ak.rsm.system;

import com.ak.util.Metrics;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tec.uom.se.unit.MetricPrefix;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static tec.uom.se.unit.Units.METRE;

class TetrapolarSystemTest {
  static Stream<Arguments> tetrapolarSystems() {
    TetrapolarSystem ts1 = new TetrapolarSystem(2.0, 1.0);
    TetrapolarSystem ts2 = new TetrapolarSystem(1.0, 2.0);
    TetrapolarSystem ts3 = new TetrapolarSystem(1.0, 3.0);
    return Stream.of(
        arguments(ts1, ts1, true),
        arguments(ts1, ts2, true),
        arguments(ts1, ts3, false),
        arguments(ts1, new Object(), false),
        arguments(new Object(), ts1, false)
    );
  }

  @ParameterizedTest
  @MethodSource("tetrapolarSystems")
  @ParametersAreNonnullByDefault
  void testEquals(Object system1, Object system2, boolean equals) {
    assertThat(system1.equals(system2))
        .withFailMessage("%s compared with %s", system1, system2).isEqualTo(equals);
    assertThat(system1.hashCode() == system2.hashCode())
        .withFailMessage("%s compared with %s", system1, system2).isEqualTo(equals);
    assertThat(system1).isNotEqualTo(null);
  }

  @Test
  void testL() {
    assertThat(new TetrapolarSystem(2.0, 1.0).lCC()).isEqualTo(1.0);
    assertThat(new TetrapolarSystem(1.0, 2.0).lCC()).isEqualTo(2.0);
  }

  @Test
  void testDim() {
    assertThat(new TetrapolarSystem(2.0, 1.0).getDim()).isEqualTo(2.0);
    assertThat(new TetrapolarSystem(1.0, 2.0).getDim()).isEqualTo(2.0);
  }

  @Test
  void testToRelative() {
    assertThat(new TetrapolarSystem(2.0, 1.0).relativeSystem()).isEqualTo(new RelativeTetrapolarSystem(2.0));
    assertThat(new TetrapolarSystem(1.0, 2.0).relativeSystem()).isEqualTo(new RelativeTetrapolarSystem(0.5));
  }

  @Test
  void testBaseL() {
    assertThat(
        TetrapolarSystem.getBaseL(
            List.of(new TetrapolarSystem(1.0, 2.0), new TetrapolarSystem(3.0, 2.0))
        )
    ).isCloseTo(2.0, within(0.01));

    assertThat(
        TetrapolarSystem.getBaseL(
            List.of(new TetrapolarSystem(1.0, 2.0), new TetrapolarSystem(2.0, 3.0))
        )
    ).isCloseTo(3.0, within(0.01));
  }

  @Test
  void testToString() {
    TetrapolarSystem ts = new TetrapolarSystem(Metrics.fromMilli(20.0), Metrics.fromMilli(15.0));
    assertThat(ts).hasToString("%2.3f x %2.3f %s".formatted(20.0, 15.0, MetricPrefix.MILLI(METRE)));
  }
}