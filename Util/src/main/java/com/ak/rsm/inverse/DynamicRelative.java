package com.ak.rsm.inverse;

import com.ak.math.Simplex;
import com.ak.rsm.measurement.DerivativeMeasurement;
import com.ak.rsm.measurement.Measurements;
import com.ak.rsm.relative.Layer2RelativeMedium;
import com.ak.rsm.relative.RelativeMediumLayers;
import com.ak.rsm.system.InexactTetrapolarSystem;
import org.apache.commons.math3.optim.PointValuePair;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.function.Function;

import static com.ak.rsm.relative.Layer1RelativeMedium.NAN;
import static java.lang.StrictMath.hypot;

final class DynamicRelative extends AbstractRelative<DerivativeMeasurement, RelativeMediumLayers> {
  @ParametersAreNonnullByDefault
  DynamicRelative(Collection<? extends DerivativeMeasurement> measurements,
                  Function<Collection<InexactTetrapolarSystem>, Regularization> regularizationFunction) {
    super(measurements, DynamicInverse.of(measurements), regularizationFunction,
        new DynamicErrors(Measurements.inexact(measurements)));
  }

  @Nonnull
  @Override
  public RelativeMediumLayers get() {
    Simplex.Bounds kMinMax;
    if (measurements().stream().allMatch(d -> d.derivativeResistivity() > 0)) {
      kMinMax = new Simplex.Bounds(-1.0, 0.0);
    }
    else if (measurements().stream().allMatch(d -> d.derivativeResistivity() < 0)) {
      kMinMax = new Simplex.Bounds(0.0, 1.0);
    }
    else if (measurements().stream().anyMatch(d -> d.derivativeResistivity() > 0) &&
        measurements().stream().anyMatch(d -> d.derivativeResistivity() < 0)) {
      return NAN;
    }
    else {
      return new StaticRelative(measurements()).get();
    }

    PointValuePair kwOptimal = Simplex.optimizeAll(kw ->
            regularization().of(kw).stream().map(Math::sqrt)
                .map(regularizing -> hypot(applyAsDouble(kw), regularizing))
                .findAny().orElse(Double.NaN),
        kMinMax, regularization().hInterval(1.0)
    );
    return apply(new Layer2RelativeMedium(kwOptimal.getPoint()));
  }
}
