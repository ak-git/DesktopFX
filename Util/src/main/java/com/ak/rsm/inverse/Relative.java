package com.ak.rsm.inverse;

import com.ak.math.Simplex;
import com.ak.rsm.measurement.DerivativeMeasurement;
import com.ak.rsm.measurement.Measurement;
import com.ak.rsm.relative.RelativeMediumLayers;
import com.ak.rsm.system.InexactTetrapolarSystem;
import org.apache.commons.math3.optim.PointValuePair;

import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.util.Collection;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.lang.StrictMath.hypot;

interface Relative<M extends Measurement> extends Function<Collection<? extends M>, RelativeMediumLayers> {
  @Override
  @OverridingMethodsMustInvokeSuper
  default RelativeMediumLayers apply(Collection<? extends M> measurements) {
    PointValuePair kwOptimal = Simplex.optimizeAll(kw -> {
          double regularizing = regularization().of(kw);
          if (Double.isFinite(regularizing)) {
            return hypot(inverse().applyAsDouble(kw), regularizing);
          }
          return regularizing;
        },
        bounds(measurements)
    );
    return inverse().apply(new RelativeMediumLayers(kwOptimal.getPoint()));
  }

  InverseFunction inverse();

  Regularization regularization();

  @OverridingMethodsMustInvokeSuper
  default Simplex.Bounds[] bounds(Collection<? extends M> measurements) {
    return new Simplex.Bounds[] {new Simplex.Bounds(-1.0, 1.0), regularization().hInterval(1.0)};
  }

  enum Static {
    ;

    private record StaticRelative(InverseFunction inverse,
                                  Regularization regularization)
        implements Relative<Measurement> {
    }

    static RelativeMediumLayers solve(Collection<? extends Measurement> measurements) {
      return new StaticRelative(new StaticInverse(measurements),
          Regularization.Interval.ZERO_MAX.of(0.0).apply(Measurement.toInexact(measurements)))
          .apply(measurements);
    }
  }

  enum Dynamic {
    ;

    private record DynamicRelative(InverseFunction inverse,
                                   Regularization regularization)
        implements Relative<DerivativeMeasurement> {

      @Override
      public RelativeMediumLayers apply(Collection<? extends DerivativeMeasurement> measurements) {
        Predicate<DerivativeMeasurement> gtZero = d -> d.derivativeResistivity() > 0;
        Predicate<DerivativeMeasurement> ltZero = d -> d.derivativeResistivity() < 0;
        if (measurements.stream().allMatch(gtZero) || measurements.stream().allMatch(ltZero)) {
          return Relative.super.apply(measurements);
        }
        else if (measurements.stream().anyMatch(gtZero) && measurements.stream().anyMatch(ltZero)) {
          return RelativeMediumLayers.NAN;
        }
        return Static.solve(measurements);
      }

      @Override
      public Simplex.Bounds[] bounds(Collection<? extends DerivativeMeasurement> measurements) {
        Simplex.Bounds[] bounds = Relative.super.bounds(measurements);
        if (measurements.stream().allMatch(d -> d.derivativeResistivity() > 0)) {
          bounds[0] = new Simplex.Bounds(-1.0, 0.0);
        }
        else if (measurements.stream().allMatch(d -> d.derivativeResistivity() < 0)) {
          bounds[0] = new Simplex.Bounds(0.0, 1.0);
        }
        return bounds;
      }
    }

    static RelativeMediumLayers solve(Collection<? extends DerivativeMeasurement> measurements,
                                      Function<Collection<InexactTetrapolarSystem>, Regularization> regularizationFunction) {
      return new DynamicRelative(DynamicInverse.of(measurements), regularizationFunction.apply(Measurement.toInexact(measurements)))
          .apply(measurements);
    }
  }
}
