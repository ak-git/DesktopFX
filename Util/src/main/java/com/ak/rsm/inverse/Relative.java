package com.ak.rsm.inverse;

import com.ak.math.Simplex;
import com.ak.rsm.measurement.DerivativeMeasurement;
import com.ak.rsm.measurement.Measurement;
import com.ak.rsm.relative.RelativeMediumLayers;
import com.ak.rsm.system.InexactTetrapolarSystem;
import org.apache.commons.math3.optim.PointValuePair;

import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.lang.StrictMath.hypot;

interface Relative<M extends Measurement> extends Function<Collection<? extends M>, RelativeMediumLayers> {
  @Override
  @Nonnull
  @OverridingMethodsMustInvokeSuper
  default RelativeMediumLayers apply(@Nonnull Collection<? extends M> measurements) {
    Regularization regularization = regularizationFunction().apply(Measurement.inexact(measurements));
    PointValuePair kwOptimal = Simplex.optimizeAll(kw -> {
          double regularizing = regularization.of(kw);
          if (Double.isFinite(regularizing)) {
            return hypot(inverse().applyAsDouble(kw), regularizing);
          }
          return regularizing;
        },
        kInterval(), regularization.hInterval(1.0)
    );
    return inverse().apply(new RelativeMediumLayers(kwOptimal.getPoint()));
  }

  @Nonnull
  InverseFunction inverse();

  @Nonnull
  Function<Collection<InexactTetrapolarSystem>, Regularization> regularizationFunction();

  @OverridingMethodsMustInvokeSuper
  @Nonnull
  default Simplex.Bounds kInterval() {
    return new Simplex.Bounds(-1.0, 1.0);
  }

  enum Static {
    ;

    @ParametersAreNonnullByDefault
    private record StaticRelative(Collection<? extends Measurement> measurements, InverseFunction inverse,
                                  Function<Collection<InexactTetrapolarSystem>, Regularization> regularizationFunction)
        implements Relative<Measurement> {
    }

    @Nonnull
    static RelativeMediumLayers solve(@Nonnull Collection<? extends Measurement> measurements) {
      return new StaticRelative(measurements, new StaticInverse(measurements), Regularization.Interval.ZERO_MAX.of(0.0))
          .apply(measurements);
    }
  }

  enum Dynamic {
    ;

    @ParametersAreNonnullByDefault
    private record DynamicRelative(Collection<? extends DerivativeMeasurement> measurements, InverseFunction inverse,
                                   Function<Collection<InexactTetrapolarSystem>, Regularization> regularizationFunction)
        implements Relative<DerivativeMeasurement> {

      @Nonnull
      @Override
      public RelativeMediumLayers apply(@Nonnull Collection<? extends DerivativeMeasurement> measurements) {
        Predicate<DerivativeMeasurement> gtZero = d -> d.derivativeResistivity() > 0;
        Predicate<DerivativeMeasurement> ltZero = d -> d.derivativeResistivity() < 0;
        if (measurements().stream().allMatch(gtZero) || measurements().stream().allMatch(ltZero)) {
          return Relative.super.apply(measurements);
        }
        else if (measurements().stream().anyMatch(gtZero) && measurements().stream().anyMatch(ltZero)) {
          return RelativeMediumLayers.NAN;
        }
        return Static.solve(measurements);
      }

      @Override
      @Nonnull
      public Simplex.Bounds kInterval() {
        Simplex.Bounds kMinMax = Relative.super.kInterval();
        if (measurements().stream().allMatch(d -> d.derivativeResistivity() > 0)) {
          kMinMax = new Simplex.Bounds(-1.0, 0.0);
        }
        else if (measurements().stream().allMatch(d -> d.derivativeResistivity() < 0)) {
          kMinMax = new Simplex.Bounds(0.0, 1.0);
        }
        return kMinMax;
      }
    }

    @ParametersAreNonnullByDefault
    @Nonnull
    static RelativeMediumLayers solve(Collection<? extends DerivativeMeasurement> measurements,
                                      Function<Collection<InexactTetrapolarSystem>, Regularization> regularizationFunction) {
      return new DynamicRelative(measurements, DynamicInverse.of(measurements), regularizationFunction).apply(measurements);
    }
  }
}
