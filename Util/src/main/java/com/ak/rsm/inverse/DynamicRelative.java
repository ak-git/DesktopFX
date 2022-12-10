package com.ak.rsm.inverse;

import com.ak.math.Simplex;
import com.ak.rsm.measurement.DerivativeMeasurement;
import com.ak.rsm.relative.Layer2RelativeMedium;
import com.ak.rsm.relative.RelativeMediumLayers;
import org.apache.commons.math3.optim.PointValuePair;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.function.ToDoubleFunction;
import java.util.function.UnaryOperator;
import java.util.logging.Logger;

import static com.ak.rsm.relative.RelativeMediumLayers.NAN;

final class DynamicRelative extends AbstractRelative<DerivativeMeasurement, RelativeMediumLayers> {
  @Nonnull
  private final ToDoubleFunction<double[]> dynamicInverse;
  @Nonnull
  private final UnaryOperator<RelativeMediumLayers> dynamicErrors;

  DynamicRelative(@Nonnull Collection<? extends DerivativeMeasurement> measurements) {
    super(measurements);
    dynamicInverse = DynamicInverse.of(measurements);
    dynamicErrors = new DynamicErrors(inexactSystems());
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

    double alpha = 0.0;
    Logger.getLogger(getClass().getName()).info(() -> "alpha = %.2f".formatted(alpha));

    PointValuePair kwOptimal = Simplex.optimizeAll(kw -> {
          double k = kw[0];
          double hToL = kw[1];
          double min = getMinHToL(k);
          double max = getMaxHToL(k);
          if (min < hToL && hToL < max) {
            return StrictMath.hypot(dynamicInverse.applyAsDouble(kw), alpha * (StrictMath.log(hToL) - StrictMath.log(min)));
          }
          else {
            return Double.MAX_VALUE;
          }
        },
        kMinMax, new Simplex.Bounds(0.0, getMaxHToL(1.0))
    );
    return apply(new Layer2RelativeMedium(kwOptimal.getPoint()));
  }

  @Nonnull
  @Override
  public RelativeMediumLayers apply(@Nonnull RelativeMediumLayers layers) {
    return dynamicErrors.apply(layers);
  }
}
