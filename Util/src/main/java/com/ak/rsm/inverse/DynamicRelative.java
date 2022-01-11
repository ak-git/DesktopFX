package com.ak.rsm.inverse;

import java.util.Collection;

import javax.annotation.Nonnull;

import com.ak.inverse.Inequality;
import com.ak.math.Simplex;
import com.ak.rsm.measurement.DerivativeMeasurement;
import com.ak.rsm.relative.Layer2RelativeMedium;
import com.ak.rsm.relative.RelativeMediumLayers;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleBounds;

import static com.ak.rsm.relative.RelativeMediumLayers.NAN;
import static java.lang.StrictMath.abs;
import static java.lang.StrictMath.log;

final class DynamicRelative extends AbstractInverse<DerivativeMeasurement, RelativeMediumLayers> {
  DynamicRelative(@Nonnull Collection<? extends DerivativeMeasurement> measurements) {
    super(measurements);
  }

  @Nonnull
  @Override
  public RelativeMediumLayers apply(@Nonnull RelativeMediumLayers layers) {
    return new DynamicErrors(inexactSystems()).apply(layers);
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

    double[] subLog = measurements().stream().mapToDouble(d -> log(d.resistivity()) - log(abs(d.derivativeResistivity()))).toArray();

    var logApparentPredicted = logApparentPredicted();
    var logDiffApparentPredicted = logDiffApparentPredicted();

    PointValuePair kwOptimal = Simplex.optimizeAll(
        kw -> {
          double[] subLogPredicted = systems().stream()
              .mapToDouble(s -> logApparentPredicted.applyAsDouble(s, kw) - logDiffApparentPredicted.applyAsDouble(s, kw))
              .toArray();
          return Inequality.absolute().applyAsDouble(subLog, subLogPredicted);
        },
        new SimpleBounds(new double[] {kMinMax[0], 0.0}, new double[] {kMinMax[1], getMaxHToL()}),
        new double[] {0.01, 0.01}
    );
    return apply(new Layer2RelativeMedium(kwOptimal.getPoint()));
  }
}
