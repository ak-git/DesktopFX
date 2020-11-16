package com.ak.rsm;

import java.util.Collection;
import java.util.stream.Collectors;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;

import com.ak.util.Builder;
import com.ak.util.Metrics;
import com.ak.util.Strings;

abstract class AbstractMediumLayers<T extends AbstractMediumLayers<T>> implements MediumLayers {
  @Nonnegative
  private final double rho;
  @Nonnull
  private final String toString;

  AbstractMediumLayers(@Nonnull AbstractMediumBuilder<T> builder) {
    rho = builder.rho;
    toString = builder.toString;
  }

  @Override
  public final double rho() {
    return rho;
  }

  @Override
  @OverridingMethodsMustInvokeSuper
  public String toString() {
    return toString;
  }

  abstract static class AbstractMediumBuilder<T extends AbstractMediumLayers<T>> implements Builder<T> {
    @Nonnull
    private final String toString;
    @Nonnegative
    double rho;

    AbstractMediumBuilder(@Nonnull Collection<Prediction> predictions) {
      double l2 = predictions.stream().map(Prediction::getInequalityL2).reduce(StrictMath::hypot).orElse(Double.NaN);
      toString = "L%s = %.2f %% %n%s".formatted(Strings.low(2), Metrics.toPercents(l2),
          predictions.stream().map(Object::toString).collect(Collectors.joining(Strings.NEW_LINE)));
    }
  }
}
