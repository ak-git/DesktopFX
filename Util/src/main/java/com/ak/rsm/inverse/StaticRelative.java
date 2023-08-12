package com.ak.rsm.inverse;

import com.ak.math.Simplex;
import com.ak.rsm.measurement.Measurement;
import com.ak.rsm.relative.RelativeMediumLayers;
import org.apache.commons.math3.optim.PointValuePair;

import javax.annotation.Nonnull;
import java.util.Collection;

final class StaticRelative extends AbstractRelative<Measurement, RelativeMediumLayers> {
  StaticRelative(@Nonnull Collection<? extends Measurement> measurements) {
    super(measurements, new StaticInverse(measurements), Regularization.Interval.ZERO_MAX.of(0.0),
        new StaticErrors(Measurement.inexact(measurements)));
  }

  @Nonnull
  @Override
  public RelativeMediumLayers get() {
    PointValuePair kwOptimal = Simplex.optimizeAll(this::applyAsDouble,
        new Simplex.Bounds(-1.0, 1.0), regularization().hInterval(1.0)
    );
    return apply(new RelativeMediumLayers(kwOptimal.getPoint()));
  }
}
