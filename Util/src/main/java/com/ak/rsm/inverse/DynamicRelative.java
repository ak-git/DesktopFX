package com.ak.rsm.inverse;

import java.util.Collection;
import java.util.function.ToDoubleFunction;
import java.util.function.UnaryOperator;

import javax.annotation.Nonnull;

import com.ak.math.Simplex;
import com.ak.rsm.measurement.DerivativeMeasurement;
import com.ak.rsm.relative.Layer2RelativeMedium;
import com.ak.rsm.relative.RelativeMediumLayers;
import org.apache.commons.math3.optim.PointValuePair;

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
    var kMinMax = new double[] {-1.0, 1.0};
    if (measurements().stream().allMatch(d -> d.derivativeResistivity() > 0)) {
      kMinMax[1] = 0.0;
    }
    else if (measurements().stream().allMatch(d -> d.derivativeResistivity() < 0)) {
      kMinMax[0] = 0.0;
    }
    else if (measurements().stream().anyMatch(d -> d.derivativeResistivity() > 0) &&
        measurements().stream().anyMatch(d -> d.derivativeResistivity() < 0)) {
      return NAN;
    }
    else {
      return new StaticRelative(measurements()).get();
    }

    PointValuePair kwOptimal = Simplex.optimizeAll(dynamicInverse::applyAsDouble,
        kMinMax, new double[] {0.0, getMaxHToL()}
    );
    return apply(new Layer2RelativeMedium(kwOptimal.getPoint()));
  }

  @Nonnull
  @Override
  public RelativeMediumLayers apply(@Nonnull RelativeMediumLayers layers) {
    return dynamicErrors.apply(layers);
  }
}
