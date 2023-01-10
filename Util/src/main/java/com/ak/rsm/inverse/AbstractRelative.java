package com.ak.rsm.inverse;

import com.ak.rsm.measurement.Measurement;
import com.ak.rsm.system.InexactTetrapolarSystem;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.interpolation.AkimaSplineInterpolator;
import org.apache.commons.math3.analysis.interpolation.UnivariateInterpolator;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.Collections;
import java.util.function.DoubleUnaryOperator;
import java.util.function.ToDoubleBiFunction;
import java.util.function.ToDoubleFunction;
import java.util.stream.DoubleStream;

abstract class AbstractRelative<M extends Measurement, L> extends AbstractErrors implements Inverse<L> {
  @Nonnull
  private final Collection<M> measurements;
  private final HToL max = new HToL(InexactTetrapolarSystem::getHMax, value -> value.max().orElseThrow());
  private final HToL min = new HToL(InexactTetrapolarSystem::getHMin, value -> value.min().orElseThrow());

  AbstractRelative(@Nonnull Collection<? extends M> measurements) {
    super(measurements.stream().map(Measurement::inexact).toList());
    this.measurements = Collections.unmodifiableCollection(measurements);
  }

  @Nonnegative
  final double getMaxHToL(double k) {
    return max.extremum(k);
  }

  @Nonnegative
  final double getMinHToL(double k) {
    return min.extremum(k);
  }

  @Nonnull
  final Collection<M> measurements() {
    return measurements;
  }

  private final class HToL {
    private static final double STEP = 0.1;
    private final UnivariateFunction positiveInterpolator;
    private final UnivariateFunction negativeInterpolator;
    private final DoubleUnaryOperator mergeHorizons;

    @ParametersAreNonnullByDefault
    HToL(ToDoubleBiFunction<InexactTetrapolarSystem, Double> toHorizon, ToDoubleFunction<DoubleStream> selector) {
      double[] xp = DoubleStream.iterate(STEP, k -> k < 1.0 + STEP / 2, k -> k + STEP).toArray();
      mergeHorizons =
          k -> selector.applyAsDouble(
              inexactSystems().stream().mapToDouble(system -> toHorizon.applyAsDouble(system, k))
          ) / baseL();
      UnivariateInterpolator interpolator = new AkimaSplineInterpolator();
      positiveInterpolator = interpolator.interpolate(xp, DoubleStream.of(xp).map(mergeHorizons).toArray());
      double[] xm = DoubleStream.of(xp).map(operand -> operand - 1.0).toArray();
      negativeInterpolator = interpolator.interpolate(xm, DoubleStream.of(xm).map(mergeHorizons).toArray());
    }

    double extremum(double k) {
      if (Math.abs(k) < STEP * 2 || Math.abs(k) > 1.0 - STEP * 2) {
        return mergeHorizons.applyAsDouble(k);
      }
      else if (k > 0) {
        return positiveInterpolator.value(k);
      }
      else {
        return negativeInterpolator.value(k);
      }
    }
  }
}
