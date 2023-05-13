package com.ak.rsm.inverse;

import com.ak.math.Simplex;
import com.ak.rsm.system.InexactTetrapolarSystem;
import com.ak.rsm.system.TetrapolarSystem;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import javax.annotation.Nonnull;
import java.security.SecureRandom;
import java.util.List;
import java.util.OptionalDouble;
import java.util.random.RandomGenerator;

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
        () -> assertThat(hInterval.initialGuess()).isNaN(),
        () -> assertThat(hInterval.max())
            .isCloseTo(Math.min(system1.getHMax(k), system2.getHMax(k)) / baseL, within(0.001))
    );

    switch (interval) {
      case ZERO_MAX -> assertAll(interval.name(),
          () -> assertThat(hInterval.min()).isZero()
      );
      case MAX_K -> assertAll(interval.name(),
          () -> assertThat(hInterval.min())
              .isCloseTo(Math.max(system1.getHMin(k), system2.getHMin(k)) / baseL, within(0.001))
      );
    }
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
    Simplex.Bounds hInterval = regularization.hInterval(Math.signum(k));
    switch (interval) {
      case ZERO_MAX -> assertAll(interval.name(),
          () -> assertThat(regularization.of(new double[] {k, Double.POSITIVE_INFINITY})).isEqualTo(OptionalDouble.empty()),
          () -> assertThat(regularization.of(new double[] {0.0, 0.0})).isEqualTo(OptionalDouble.empty()),
          () -> assertThat(regularization.of(new double[] {k, (hInterval.max() + hInterval.min()) / 2.0}).orElseThrow())
              .isCloseTo(0.0, within(0.001))
      );
      case MAX_K -> assertAll(interval.name(),
          () -> assertThat(regularization.of(new double[] {k, RANDOM.nextGaussian()}).orElseThrow())
              .isCloseTo(alpha * (log(2.0 - Math.abs(k)) - log(Math.abs(k))), within(0.001))
      );
    }
  }

  @ParameterizedTest
  @EnumSource(Regularization.Interval.class)
  void toString(@Nonnull Regularization.Interval interval) {
    double alpha = RANDOM.nextDouble(1.0, 10.0);
    assertThat(interval.of(alpha)).hasToString("RegularizationFunction{%s, alpha = %.1f}".formatted(interval, alpha));
  }
}