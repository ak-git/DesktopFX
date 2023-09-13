package com.ak.rsm.inverse;

import com.ak.math.Simplex;
import com.ak.math.ValuePair;
import com.ak.rsm.system.InexactTetrapolarSystem;
import com.ak.util.Strings;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.Collections;
import java.util.OptionalDouble;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;
import java.util.function.ToDoubleBiFunction;
import java.util.stream.DoubleStream;

import static java.lang.StrictMath.log;

public sealed interface Regularization permits Regularization.AbstractRegularization {
  enum Interval {
    ZERO_MAX {
      @Nonnull
      @Override
      Regularization innerOf(@Nonnegative double alpha, @Nonnull Collection<InexactTetrapolarSystem> inexactSystems) {
        return new AbstractRegularization(inexactSystems) {
          @Nonnull
          @Override
          public double of(@Nonnull double... kw) {
            double k = kw[0];
            double hToL = kw[1];

            Simplex.Bounds hInterval = hInterval(k);
            if (hInterval.isIn(hToL)) {
              return alpha * (log(hInterval.max() - hToL) - log(hToL - hInterval.min()));
            }
            else {
              return Double.POSITIVE_INFINITY;
            }
          }
        };
      }
    },
    MAX_K {
      @Nonnull
      @Override
      Regularization innerOf(@Nonnegative double alpha, @Nonnull Collection<InexactTetrapolarSystem> inexactSystems) {
        return new AbstractRegularization(inexactSystems) {
          @Nonnull
          @Override
          public double of(@Nonnull double... kw) {
            double k = Math.abs(kw[0]);
            return alpha * (log(2 - k) - log(k));
          }
        };
      }
    };

    @Nonnull
    public final Function<Collection<InexactTetrapolarSystem>, Regularization> of(@Nonnegative double alpha) {
      return new Function<>() {
        @Override
        public Regularization apply(@Nonnull Collection<InexactTetrapolarSystem> inexactSystems) {
          return innerOf(alpha, inexactSystems);
        }

        @Override
        public String toString() {
          return "RegularizationFunction{%s, %s = %s}".formatted(name(), Strings.ALPHA,
              ValuePair.format(alpha, ValuePair.afterZero(alpha / 10.0)));
        }
      };
    }

    @Nonnull
    abstract Regularization innerOf(@Nonnegative double alpha, @Nonnull Collection<InexactTetrapolarSystem> inexactSystems);
  }

  abstract non-sealed class AbstractRegularization implements Regularization {
    @Nonnull
    private final Collection<InexactTetrapolarSystem> inexactSystems;
    @Nonnull
    private final DoubleUnaryOperator max;
    @Nonnull
    private final DoubleUnaryOperator min;

    private AbstractRegularization(@Nonnull Collection<InexactTetrapolarSystem> inexactSystems) {
      this.inexactSystems = Collections.unmodifiableCollection(inexactSystems);
      max = newMergeHorizons(InexactTetrapolarSystem::getHMax, DoubleStream::min);
      min = newMergeHorizons(InexactTetrapolarSystem::getHMin, DoubleStream::max);
    }

    @Override
    public final Simplex.Bounds hInterval(double k) {
      return new Simplex.Bounds(min.applyAsDouble(k), max.applyAsDouble(k));
    }

    @ParametersAreNonnullByDefault
    private DoubleUnaryOperator newMergeHorizons(ToDoubleBiFunction<InexactTetrapolarSystem, Double> toHorizon,
                                                 Function<DoubleStream, OptionalDouble> selector) {
      double baseL = InexactTetrapolarSystem.getBaseL(inexactSystems);
      return k -> selector
          .apply(inexactSystems.stream().mapToDouble(system -> toHorizon.applyAsDouble(system, k)))
          .orElseThrow() / baseL;
    }
  }

  @Nonnull
  double of(@Nonnull double... kw);

  @Nonnull
  Simplex.Bounds hInterval(double k);
}
