package com.ak.rsm.inverse;

import java.util.Collection;
import java.util.function.UnaryOperator;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.math.Simplex;
import com.ak.rsm.measurement.Measurement;
import com.ak.rsm.relative.Layer2RelativeMedium;
import com.ak.rsm.relative.RelativeMediumLayers;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleBounds;

final class StaticRelative extends AbstractRelative<Measurement, RelativeMediumLayers> {
  @Nonnull
  private final StaticInverse staticInverse;
  @Nonnull
  private final StaticErrors staticErrors;

  StaticRelative(@Nonnull Collection<? extends Measurement> measurements) {
    this(measurements, UnaryOperator.identity());
  }

  @ParametersAreNonnullByDefault
  StaticRelative(Collection<? extends Measurement> measurements, UnaryOperator<double[]> subtract) {
    super(measurements);
    staticInverse = new StaticInverse(measurements(), subtract);
    staticErrors = new StaticErrors(inexactSystems());
  }

  @Nonnull
  @Override
  public RelativeMediumLayers get() {
    PointValuePair kwOptimal = Simplex.optimize(staticInverse::applyAsDouble,
        new SimpleBounds(new double[] {-1.0, 0.0}, new double[] {1.0, getMaxHToL()})
    );
    return staticErrors.errors(new Layer2RelativeMedium(kwOptimal.getPoint()), staticInverse.subtract(),
        UnaryOperator.identity(), (ts, b) -> b);
  }

  @Nonnull
  @Override
  public RelativeMediumLayers apply(@Nonnull RelativeMediumLayers layers) {
    return staticErrors.apply(layers);
  }
}
