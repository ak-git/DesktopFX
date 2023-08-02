package com.ak.rsm.inverse;

import com.ak.math.Simplex;
import com.ak.math.ValuePair;
import com.ak.rsm.system.InexactTetrapolarSystem;
import com.ak.rsm.system.TetrapolarSystem;
import com.ak.util.Strings;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import javax.annotation.Nonnull;
import java.security.SecureRandom;
import java.util.List;
import java.util.random.RandomGenerator;
import java.util.stream.DoubleStream;

import static java.lang.StrictMath.log;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.assertAll;

class RegularizationTest {
  private static final RandomGenerator RANDOM = new SecureRandom();

  @ParameterizedTest
  @EnumSource(Regularization.Interval.class)
  void hInterval(@Nonnull Regularization.Interval interval) {
    double baseL = 30.0;
    InexactTetrapolarSystem system1 = new InexactTetrapolarSystem(0.1, new TetrapolarSystem(10.0, baseL));
    InexactTetrapolarSystem system2 = new InexactTetrapolarSystem(0.1, new TetrapolarSystem(50.0, baseL));

    double k = RANDOM.nextDouble(-1.0, 1.0);
    double alpha = RANDOM.nextDouble(1.0, 10.0);

    Regularization regularization = interval.of(alpha).apply(List.of(system1, system2));
    Simplex.Bounds hInterval = regularization.hInterval(k);
    assertAll(interval.name(),
        () -> assertThat(hInterval.min()).isCloseTo(Math.max(system1.getHMin(k), system2.getHMin(k)) / baseL, within(0.001)),
        () -> assertThat(hInterval.initialGuess()).isNaN(),
        () -> assertThat(hInterval.max())
            .isCloseTo(Math.min(system1.getHMax(k), system2.getHMax(k)) / baseL, within(0.001))
    );
  }

  @ParameterizedTest
  @EnumSource(Regularization.Interval.class)
  void of(@Nonnull Regularization.Interval interval) {
    double baseL = RANDOM.nextDouble(20, 40);
    InexactTetrapolarSystem system1 = new InexactTetrapolarSystem(0.1, new TetrapolarSystem(10.0, baseL));
    InexactTetrapolarSystem system2 = new InexactTetrapolarSystem(0.1, new TetrapolarSystem(50.0, baseL));

    double k = RANDOM.nextDouble(-1.0, 1.0);
    double alpha = RANDOM.nextDouble(1.0, 10.0);

    Regularization regularization = interval.of(alpha).apply(List.of(system1, system2));
    Simplex.Bounds hInterval = regularization.hInterval(k);
    switch (interval) {
      case ZERO_MAX -> assertAll(interval.name(),
          () -> assertThat(regularization.of(k, Double.POSITIVE_INFINITY)).isEqualTo(Double.POSITIVE_INFINITY),
          () -> assertThat(regularization.of(0.0, 0.0)).isEqualTo(Double.POSITIVE_INFINITY),
          () -> assertThat(regularization.of(k, hInterval.max() / 2.0))
              .isCloseTo(0.0, within(0.001))
      );
      case MAX_K -> assertAll(interval.name(),
          () -> assertThat(regularization.of(k, RANDOM.nextGaussian()))
              .isCloseTo(alpha * log(Math.abs(k)), within(0.001))
      );
    }
  }

  @ParameterizedTest
  @EnumSource(Regularization.Interval.class)
  void toString(@Nonnull Regularization.Interval interval) {
    DoubleStream.concat(DoubleStream.of(0.0), RANDOM.doubles(10, 0.01, 100.0))
        .forEach(a -> assertThat(interval.of(a))
            .hasToString("RegularizationFunction{%s, %s = %s}".formatted(interval, Strings.ALPHA,
                ValuePair.format(a, ValuePair.afterZero(a / 10.0)))
            ));
  }
}