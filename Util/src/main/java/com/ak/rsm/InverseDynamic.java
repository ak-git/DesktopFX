package com.ak.rsm;

import java.util.Collection;

import javax.annotation.Nonnull;

import com.ak.inverse.Inequality;
import com.ak.math.Simplex;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleBounds;

import static com.ak.rsm.Measurements.getMaxHToL;
import static com.ak.rsm.Measurements.logApparentPredicted;
import static com.ak.rsm.Measurements.logDiffApparentPredicted;
import static com.ak.rsm.RelativeMediumLayers.NAN;

enum InverseDynamic implements Inverseable<DerivativeMeasurement> {
  INSTANCE;

  @Nonnull
  @Override
  public MediumLayers inverse(@Nonnull Collection<? extends DerivativeMeasurement> measurements) {
    if (measurements.size() > 1) {
      RelativeMediumLayers<Double> kw = inverseRelative(measurements);
      return new Layer2Medium(measurements, kw);
    }
    else {
      return InverseStatic.INSTANCE.inverse(measurements);
    }
  }

  @Nonnull
  @Override
  public RelativeMediumLayers<Double> inverseRelative(@Nonnull Collection<? extends DerivativeMeasurement> derivativeMeasurements) {
    var kMinMax = new double[] {-1.0, 1.0};
    if (derivativeMeasurements.stream().allMatch(d -> d.getDerivativeResistivity() > 0)) {
      kMinMax[1] = 0.0;
    }
    else if (derivativeMeasurements.stream().allMatch(d -> d.getDerivativeResistivity() < 0)) {
      kMinMax[0] = 0.0;
    }
    else if (derivativeMeasurements.stream().anyMatch(d -> d.getDerivativeResistivity() > 0) &&
        derivativeMeasurements.stream().anyMatch(d -> d.getDerivativeResistivity() < 0)) {
      return NAN;
    }
    else {
      return InverseStatic.INSTANCE.inverseRelative(derivativeMeasurements);
    }

    double[] subLog = derivativeMeasurements.stream().mapToDouble(d -> d.getLogResistivity() - d.getDerivativeLogResistivity()).toArray();
    var logApparentPredicted = logApparentPredicted(derivativeMeasurements);
    var logDiffApparentPredicted = logDiffApparentPredicted(derivativeMeasurements);

    PointValuePair kwOptimal = Simplex.optimizeAll(
        kw -> {
          double[] subLogPredicted = derivativeMeasurements.stream()
              .map(measurement -> measurement.getSystem().toExact())
              .mapToDouble(s -> logApparentPredicted.applyAsDouble(s, kw) - logDiffApparentPredicted.applyAsDouble(s, kw))
              .toArray();
          return Inequality.absolute().applyAsDouble(subLog, subLogPredicted);
        },
        new SimpleBounds(new double[] {kMinMax[0], 0.0}, new double[] {kMinMax[1], getMaxHToL(derivativeMeasurements)}),
        new double[] {0.01, 0.01}
    );
    return new Layer2RelativeMedium<>(kwOptimal.getPoint()[0], kwOptimal.getPoint()[1]);
  }
}
