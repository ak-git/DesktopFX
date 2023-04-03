package com.ak.rsm.inverse;

import com.ak.math.Simplex;
import com.ak.rsm.system.InexactTetrapolarSystem;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
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
      public Function<Collection<InexactTetrapolarSystem>, Regularization> of(@Nonnegative double alpha) {
        return inexactSystems -> new AbstractRegularization(inexactSystems) {
          @Override
          public Simplex.Bounds hInterval(double k) {
            return new Simplex.Bounds(0, getMax(k));
          }

          @Nonnull
          @Override
          public OptionalDouble of(@Nonnull double[] kw) {
            double k = kw[0];
            double hToL = kw[1];

            Simplex.Bounds bounds = hInterval(k);
            if (bounds.min() < hToL && hToL < bounds.max()) {
              return OptionalDouble.of(alpha * (log(bounds.max() - hToL) - log(hToL - bounds.min())));
            }
            else {
              return OptionalDouble.empty();
            }
          }
        };
      }
    },
    MAX_K {
      @Nonnull
      @Override
      public Function<Collection<InexactTetrapolarSystem>, Regularization> of(@Nonnegative double alpha) {
        return inexactSystems -> new AbstractRegularization(inexactSystems) {
          @Override
          public Simplex.Bounds hInterval(double k) {
            return new Simplex.Bounds(getMin(k), getMax(k));
          }

          @Nonnull
          @Override
          public OptionalDouble of(@Nonnull double[] kw) {
            double k = Math.abs(kw[0]);
            return OptionalDouble.of(alpha * (log(2.0 - k) - log(k)));
          }
        };
      }
    };

    @Nonnull
    public abstract Function<Collection<InexactTetrapolarSystem>, Regularization> of(@Nonnegative double alpha);
  }

  abstract non-sealed class AbstractRegularization extends AbstractErrors implements Regularization {
    private final DoubleUnaryOperator max = newMergeHorizons(InexactTetrapolarSystem::getHMax, DoubleStream::min);
    private final DoubleUnaryOperator min = newMergeHorizons(InexactTetrapolarSystem::getHMin, DoubleStream::max);

    private AbstractRegularization(@Nonnull Collection<InexactTetrapolarSystem> inexactSystems) {
      super(inexactSystems);
    }

    final double getMin(double k) {
      return min.applyAsDouble(k);
    }

    final double getMax(double k) {
      return max.applyAsDouble(k);
    }

    @ParametersAreNonnullByDefault
    private DoubleUnaryOperator newMergeHorizons(ToDoubleBiFunction<InexactTetrapolarSystem, Double> toHorizon,
                                                 Function<DoubleStream, OptionalDouble> selector) {
      return k -> selector
          .apply(inexactSystems().stream().mapToDouble(system -> toHorizon.applyAsDouble(system, k)))
          .orElseThrow() / baseL();
    }
  }

  @Nonnull
  OptionalDouble of(@Nonnull double[] kw);

  @Nonnull
  Simplex.Bounds hInterval(double k);
}