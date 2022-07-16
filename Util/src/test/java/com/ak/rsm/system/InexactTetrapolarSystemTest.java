package com.ak.rsm.system;

import java.util.Collection;
import java.util.List;
import java.util.function.DoubleUnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.inverse.Inequality;
import com.ak.math.Simplex;
import com.ak.rsm.resistance.TetrapolarResistance;
import com.ak.util.Metrics;
import com.ak.util.Strings;
import org.apache.commons.math3.optim.PointValuePair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.byLessThan;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class InexactTetrapolarSystemTest {
  static Stream<Arguments> tetrapolarSystems() {
    InexactTetrapolarSystem ts1 = new InexactTetrapolarSystem(0.1, new TetrapolarSystem(10.0, 20.0));
    InexactTetrapolarSystem ts2 = new InexactTetrapolarSystem(0.1, new TetrapolarSystem(20.0, 10.0));
    InexactTetrapolarSystem ts3 = new InexactTetrapolarSystem(0.2, new TetrapolarSystem(20.0, 30.0));
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

  static Stream<Arguments> inexactTetrapolarSystems() {
    return Stream.of(
        arguments(new InexactTetrapolarSystem(0.1, new TetrapolarSystem(10.0, 30.0))),
        arguments(new InexactTetrapolarSystem(0.1, new TetrapolarSystem(30.0, 10.0)))
    );
  }

  @ParameterizedTest
  @MethodSource("inexactTetrapolarSystems")
  void testToString(@Nonnull Object system) {
    assertThat(system.toString()).contains("%.1f".formatted(Metrics.toMilli(0.1)));
  }

  @Test
  void testToString() {
    TetrapolarSystem system = new TetrapolarSystem(10.0, 30.0);
    InexactTetrapolarSystem s = new InexactTetrapolarSystem(0.0, system);
    assertThat(s).hasToString(system.toString());
  }

  @ParameterizedTest
  @MethodSource("inexactTetrapolarSystems")
  void testAbsError(@Nonnull InexactTetrapolarSystem system) {
    assertThat(system.absError()).withFailMessage(system::toString).isCloseTo(0.1, byLessThan(0.01));
  }

  @ParameterizedTest
  @MethodSource("inexactTetrapolarSystems")
  void testGetDeltaApparent(@Nonnull InexactTetrapolarSystem system) {
    assertThat(system.getApparentRelativeError())
        .withFailMessage(system::toString).isCloseTo(6.0 * 0.1 / 30.0, byLessThan(1.0e-6));
  }

  @ParameterizedTest
  @MethodSource("inexactTetrapolarSystems")
  void testGetHMax(@Nonnull InexactTetrapolarSystem system) {
    assertThat(system.getHMax(1.0))
        .withFailMessage(system::toString).isCloseTo(0.177 * 30.0 / StrictMath.pow(0.1 / 30.0, 1.0 / 3.0), byLessThan(0.1));
  }

  static Stream<Arguments> rho1rho2() {
    return Stream.of(
        arguments(1.0, Double.POSITIVE_INFINITY),
        arguments(10.0, Double.POSITIVE_INFINITY),
        arguments(0.1, Double.POSITIVE_INFINITY),
        arguments(1.0, 2.0),
        arguments(2.0, 1.0)
    );
  }

  @ParameterizedTest
  @MethodSource("rho1rho2")
  void testHMax(@Nonnegative double rho1, @Nonnegative double rho2) {
    InexactTetrapolarSystem system = new InexactTetrapolarSystem(0.1, new TetrapolarSystem(10.0, 30.0));
    DoubleUnaryOperator rhoAtHMax = sign -> (1.0 + Math.signum(sign) * system.getApparentRelativeError()) * rho1;
    PointValuePair optimize = Simplex.optimizeAll(hToL -> {
          double rhoApparent = TetrapolarResistance
              .of(system.system()).rho1(rho1).rho2(rho2).h(hToL[0] * system.system().lCC()).resistivity();
          return DoubleStream.of(-1.0, 1.0)
              .map(sign -> Inequality.absolute().applyAsDouble(rhoAtHMax.applyAsDouble(sign), rhoApparent))
              .min().orElseThrow();
        },
        new Simplex.Bounds(0.0, system.getHMax(1.0) / system.system().lCC())
    );
    assertThat(optimize.getPoint()[0]).withFailMessage(system::toString)
        .isCloseTo(system.getHMax(Layers.getK12(rho1, rho2)) / system.system().lCC(), byLessThan(0.1));
  }

  @ParameterizedTest
  @MethodSource("rho1rho2")
  void testHMin(@Nonnegative double rho1, @Nonnegative double rho2) {
    if (Double.isFinite(rho2)) {
      InexactTetrapolarSystem system = new InexactTetrapolarSystem(0.1, new TetrapolarSystem(10.0, 30.0));
      DoubleUnaryOperator rhoAtHMin = sign -> (1.0 + Math.signum(sign) * system.getApparentRelativeError()) * rho2;
      PointValuePair optimize = Simplex.optimizeAll(hToL -> {
            double rhoApparent = TetrapolarResistance
                .of(system.system()).rho1(rho1).rho2(rho2).h(hToL[0] * system.system().lCC()).resistivity();
            return DoubleStream.of(-1.0, 1.0)
                .map(sign -> Inequality.absolute().applyAsDouble(rhoAtHMin.applyAsDouble(sign), rhoApparent))
                .min().orElseThrow();
          },
          new Simplex.Bounds(0.0, system.getHMax(1.0) / system.system().lCC())
      );
      assertThat(optimize.getPoint()[0]).withFailMessage(system::toString)
          .isCloseTo(system.getHMin(Layers.getK12(rho1, rho2)) / system.system().lCC(), byLessThan(0.01));
    }
  }

  static Stream<Arguments> combinations() {
    return Stream.of(
        arguments(
            List.of(
                new InexactTetrapolarSystem(0.1, new TetrapolarSystem(10.0, 30.0)),
                new InexactTetrapolarSystem(0.1, new TetrapolarSystem(50.0, 30.0))
            ),
            2
        )
    );
  }

  @ParameterizedTest
  @MethodSource("combinations")
  void testCombinations(@Nonnull Collection<InexactTetrapolarSystem> systems, @Nonnegative int expected) {
    Collection<List<TetrapolarSystem>> c = InexactTetrapolarSystem.getMeasurementsCombination(systems);
    assertThat(c).withFailMessage(c.stream().map(Object::toString).collect(Collectors.joining(Strings.NEW_LINE))).hasSize(expected);
  }
}