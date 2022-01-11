package com.ak.rsm.inverse;

import java.util.Collection;
import java.util.function.UnaryOperator;

import javax.annotation.Nonnull;

import com.ak.inverse.Inequality;
import com.ak.math.Simplex;
import com.ak.rsm.measurement.Measurement;
import com.ak.rsm.relative.Layer2RelativeMedium;
import com.ak.rsm.relative.RelativeMediumLayers;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleBounds;

final class StaticRelative extends AbstractInverse<Measurement, RelativeMediumLayers> {
  @Nonnull
  private final StaticErrors staticErrors;

  StaticRelative(@Nonnull Collection<? extends Measurement> measurements) {
    super(measurements);
    staticErrors = new StaticErrors(inexactSystems());
  }

  @Nonnull
  @Override
  public RelativeMediumLayers apply(@Nonnull RelativeMediumLayers layers) {
    return staticErrors.apply(layers);
  }

  @Override
  public RelativeMediumLayers get() {
    return inverseRelative(UnaryOperator.identity());
  }

  @Nonnull
  RelativeMediumLayers inverseRelative(@Nonnull UnaryOperator<double[]> subtract) {
    double[] subLogApparent = subtract.apply(measurements().stream().map(Measurement::resistivity).mapToDouble(StrictMath::log).toArray());
    var logApparentPredicted = logApparentPredicted();

    PointValuePair kwOptimal = Simplex.optimizeAll(kw -> {
          double[] subLogApparentPredicted = subtract.apply(
              systems().stream()
                  .mapToDouble(s -> logApparentPredicted.applyAsDouble(s, kw))
                  .toArray()
          );
          return Inequality.absolute().applyAsDouble(subLogApparent, subLogApparentPredicted);
        },
        new SimpleBounds(new double[] {-1.0, 0.0}, new double[] {1.0, getMaxHToL()}),
        new double[] {0.01, 0.01}
    );
    return staticErrors.errors(new Layer2RelativeMedium(kwOptimal.getPoint()), subtract, UnaryOperator.identity(), (ts, b) -> b);
  }
}
