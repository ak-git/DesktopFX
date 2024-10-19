package com.ak.rsm.inverse;

import com.ak.math.Simplex;
import com.ak.math.ValuePair;
import com.ak.rsm.system.InexactTetrapolarSystem;
import com.ak.util.Strings;

import javax.annotation.Nonnegative;
import java.util.Collection;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;
import java.util.function.ToDoubleBiFunction;
import java.util.stream.DoubleStream;

import static java.lang.StrictMath.log;
import static java.lang.StrictMath.log1p;

public sealed interface Regularization permits Regularization.AbstractRegularization {
  enum Interval {
    ZERO_MAX {
      @Override
      Regularization innerOf(@Nonnegative double alpha, Collection<InexactTetrapolarSystem> inexactSystems) {
        return new AbstractRegularization(inexactSystems) {
          @Override
          public double of(double... kw) {
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
    ZERO_MAX_LOG1P {
      @Override
      Regularization innerOf(@Nonnegative double alpha, Collection<InexactTetrapolarSystem> inexactSystems) {
        return new AbstractRegularization(inexactSystems) {
          @Override
          public double of(double... kw) {
            double k = kw[0];
            double hToL = kw[1];

            Simplex.Bounds hInterval = hInterval(k);
            if (hInterval.isIn(hToL)) {
              double x = log1p(hToL);
              return alpha * (log(log1p(hInterval.max()) - x) - log(x - log1p(hInterval.min())));
            }
            else {
              return Double.POSITIVE_INFINITY;
            }
          }
        };
      }
    },
    MAX_K {
      @Override
      Regularization innerOf(@Nonnegative double alpha, Collection<InexactTetrapolarSystem> inexactSystems) {
        return new AbstractRegularization(inexactSystems) {
          @Override
          public double of(double... kw) {
            double k = Math.abs(kw[0]);
            return alpha * (log(2 - k) - log(k));
          }
        };
      }
    };

    public final Function<Collection<InexactTetrapolarSystem>, Regularization> of(@Nonnegative double alpha) {
      return new Function<>() {
        @Override
        public Regularization apply(Collection<InexactTetrapolarSystem> inexactSystems) {
          return innerOf(alpha, inexactSystems);
        }

        @Override
        public String toString() {
          return "RegularizationFunction{%s, %s = %s}".formatted(name(), Strings.ALPHA,
              ValuePair.format(alpha, ValuePair.afterZero(alpha / 10.0)));
        }
      };
    }

    abstract Regularization innerOf(@Nonnegative double alpha, Collection<InexactTetrapolarSystem> inexactSystems);
  }

  abstract non-sealed class AbstractRegularization implements Regularization {
    private final Collection<InexactTetrapolarSystem> inexactSystems;
    private final DoubleUnaryOperator max;
    private final DoubleUnaryOperator min;

    private AbstractRegularization(Collection<InexactTetrapolarSystem> inexactSystems) {
      this.inexactSystems = Set.copyOf(inexactSystems);
      max = newMergeHorizons(InexactTetrapolarSystem::getHMax, DoubleStream::min);
      min = newMergeHorizons(InexactTetrapolarSystem::getHMin, DoubleStream::max);
    }

    @Override
    public final Simplex.Bounds hInterval(double k) {
      return new Simplex.Bounds(min.applyAsDouble(k), max.applyAsDouble(k));
    }

    private DoubleUnaryOperator newMergeHorizons(ToDoubleBiFunction<InexactTetrapolarSystem, Double> toHorizon,
                                                 Function<DoubleStream, OptionalDouble> selector) {
      double baseL = InexactTetrapolarSystem.getBaseL(inexactSystems);
      return k -> selector
          .apply(inexactSystems.stream().mapToDouble(system -> toHorizon.applyAsDouble(system, k)))
          .orElseThrow() / baseL;
    }
  }

  double of(double... kw);

  Simplex.Bounds hInterval(double k);
}
