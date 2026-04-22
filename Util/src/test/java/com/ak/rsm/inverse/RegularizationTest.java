package com.ak.rsm.inverse;

import com.ak.math.Simplex;
import com.ak.math.ValuePair;
import com.ak.rsm.system.InexactTetrapolarSystem;
import com.ak.rsm.system.TetrapolarSystem;
import com.ak.util.Strings;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.security.SecureRandom;
import java.util.List;
import java.util.random.RandomGenerator;
import java.util.stream.DoubleStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.assertAll;

class RegularizationTest {
  private static final RandomGenerator RANDOM = new SecureRandom();

  @ParameterizedTest
  @EnumSource(Regularization.Interval.class)
  void hInterval(Regularization.Interval interval) {
    double baseL = 30.0;
    InexactTetrapolarSystem system1 = new InexactTetrapolarSystem(0.1, new TetrapolarSystem(10.0, baseL));
    InexactTetrapolarSystem system2 = new InexactTetrapolarSystem(0.1, new TetrapolarSystem(50.0, baseL));

    double k = RANDOM.nextDouble(0.1, 1.0) * Math.signum(RANDOM.nextGaussian());
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
  void of(Regularization.Interval interval) {
    double baseL = RANDOM.nextDouble(20, 40);
    InexactTetrapolarSystem system1 = new InexactTetrapolarSystem(0.1, new TetrapolarSystem(10.0, baseL));
    InexactTetrapolarSystem system2 = new InexactTetrapolarSystem(0.1, new TetrapolarSystem(50.0, baseL));

    double k = RANDOM.nextDouble(0.1, 1.0) * Math.signum(RANDOM.nextGaussian());
    double alpha = RANDOM.nextDouble(1.0, 10.0);

    Regularization regularization = interval.of(alpha).apply(List.of(system1, system2));
    Simplex.Bounds hInterval = regularization.hInterval(k);
    switch (interval) {
      case ZERO_MAX -> assertAll(interval.name(),
          () -> assertThat(regularization.of(k, Double.POSITIVE_INFINITY)).isInfinite(),
          () -> assertThat(regularization.of(0.0, 0.0)).isInfinite(),
          () -> assertThat(regularization.of(k, (hInterval.max() + hInterval.min()) / 2.0)).isCloseTo(0.0, within(0.001))
      );
      case ZERO_MAX_LOG -> assertAll(interval.name(),
          () -> assertThat(regularization.of(k, Double.POSITIVE_INFINITY)).isInfinite(),
          () -> assertThat(regularization.of(0.0, 0.0)).isInfinite(),
          () -> assertThat(Math.abs(regularization.of(k, (hInterval.max() + hInterval.min()) / 2.0))).isGreaterThan(0.0),
          () -> assertThat(Math.abs(regularization.of(k, 0))).isGreaterThan(0.0)
      );
      case MAX_K -> assertAll(interval.name(),
          () -> assertThat(regularization.of(1.0, RANDOM.nextGaussian())).isZero(),
          () -> assertThat(regularization.of(-1.0, RANDOM.nextGaussian())).isZero(),
          () -> assertThat(regularization.of(0.0, RANDOM.nextGaussian())).isInfinite()
      );
    }
  }

  @ParameterizedTest
  @EnumSource(Regularization.Interval.class)
  void toString(Regularization.Interval interval) {
    DoubleStream.concat(DoubleStream.of(0.0), RANDOM.doubles(10, 0.01, 100.0))
        .forEach(a -> assertThat(interval.of(a))
            .hasToString("RegularizationFunction{%s, %s = %s}".formatted(interval, Strings.ALPHA,
                ValuePair.format(a, ValuePair.afterZero(a / 10.0)))
            ));
  }
}