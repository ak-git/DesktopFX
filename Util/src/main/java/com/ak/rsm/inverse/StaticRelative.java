package com.ak.rsm.inverse;

import com.ak.math.Simplex;
import com.ak.rsm.measurement.Measurement;
import com.ak.rsm.relative.Layer2RelativeMedium;
import com.ak.rsm.relative.RelativeMediumLayers;
import org.apache.commons.math3.optim.PointValuePair;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.function.ToDoubleFunction;

final class StaticRelative extends AbstractRelative<Measurement, RelativeMediumLayers> {
  @Nonnull
  private final ToDoubleFunction<double[]> staticInverse;
  @Nonnull
  private final StaticErrors staticErrors;

  StaticRelative(@Nonnull Collection<? extends Measurement> measurements) {
    super(measurements, Regularization.Interval.ZERO_MAX.of(0.0));
    staticInverse = new StaticInverse(measurements());
    staticErrors = new StaticErrors(inexactSystems());
  }

  @Nonnull
  @Override
  public RelativeMediumLayers get() {
    PointValuePair kwOptimal = Simplex.optimizeAll(staticInverse::applyAsDouble,
        new Simplex.Bounds(-1.0, 1.0), regularization().hInterval(1.0)
    );
    return apply(new Layer2RelativeMedium(kwOptimal.getPoint()));
  }

  @Nonnull
  @Override
  public RelativeMediumLayers apply(@Nonnull RelativeMediumLayers layers) {
    return staticErrors.apply(layers);
  }
}
