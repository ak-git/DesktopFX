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
        return new AbstractRegularizationFunction(name(), alpha) {
          @Override
          public Regularization apply(@Nonnull Collection<InexactTetrapolarSystem> inexactSystems) {
            return new AbstractRegularization(inexactSystems) {
              @Nonnull
              @Override
              public OptionalDouble of(@Nonnull double[] kw) {
                return innerOf(kw, alpha);
              }
            };
          }
        };
      }
    },
    MAX_K {
      @Nonnull
      @Override
      public Function<Collection<InexactTetrapolarSystem>, Regularization> of(@Nonnegative double alpha) {
        return new AbstractRegularizationFunction(name(), alpha) {
          @Override
          public Regularization apply(@Nonnull Collection<InexactTetrapolarSystem> inexactSystems) {
            return new AbstractRegularization(inexactSystems) {
              @Nonnull
              @Override
              public OptionalDouble of(@Nonnull double[] kw) {
                double k = Math.abs(kw[0]);
                return OptionalDouble.of(alpha * log(k));
              }
            };
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

    @Override
    public final Simplex.Bounds hInterval(double k) {
      return new Simplex.Bounds(min.applyAsDouble(k), max.applyAsDouble(k));
    }

    final OptionalDouble innerOf(@Nonnull double[] kw, @Nonnegative double alpha) {
      double k = kw[0];
      double hToL = kw[1];

      Simplex.Bounds hInterval = hInterval(k);
      if (hInterval.isIn(hToL)) {
        Simplex.Bounds bounds = new Simplex.Bounds(0, hInterval.max());
        return OptionalDouble.of(alpha * (log(bounds.max() - hToL) - log(hToL - bounds.min())));
      }
      else {
        return OptionalDouble.empty();
      }
    }

    @ParametersAreNonnullByDefault
    private DoubleUnaryOperator newMergeHorizons(ToDoubleBiFunction<InexactTetrapolarSystem, Double> toHorizon,
                                                 Function<DoubleStream, OptionalDouble> selector) {
      return k -> selector
          .apply(inexactSystems().stream().mapToDouble(system -> toHorizon.applyAsDouble(system, k)))
          .orElseThrow() / baseL();
    }
  }

  abstract class AbstractRegularizationFunction implements Function<Collection<InexactTetrapolarSystem>, Regularization> {
    @Nonnull
    private final String name;
    @Nonnegative
    private final double alpha;

    private AbstractRegularizationFunction(@Nonnull String name, @Nonnegative double alpha) {
      this.name = name;
      this.alpha = alpha;
    }

    @Override
    public final String toString() {
      return "RegularizationFunction{%s, alpha = %.1f}".formatted(name, alpha);
    }
  }

  @Nonnull
  OptionalDouble of(@Nonnull double[] kw);

  @Nonnull
  Simplex.Bounds hInterval(double k);
}
