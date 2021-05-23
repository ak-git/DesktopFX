package com.ak.rsm;

import java.util.Collection;
import java.util.function.ToDoubleBiFunction;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.inverse.Inequality;
import com.ak.math.Simplex;
import com.ak.math.ValuePair;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleBounds;

import static com.ak.rsm.Measurement.getBaseL;
import static com.ak.rsm.RelativeMediumLayers.SINGLE_LAYER;
import static java.lang.StrictMath.exp;

enum Inverse {
  ;

  @Nonnull
  static MediumLayers<ValuePair> inverseStatic(@Nonnull Collection<Measurement> measurements) {
    if (measurements.size() > 2) {
      UnaryOperator<double[]> subtract = values -> {
        var sub = new double[values.length - 1];
        for (var i = 0; i < sub.length; i++) {
          sub[i] = values[i + 1] - values[i];
        }
        return sub;
      };

      RelativeMediumLayers<Double> kw = inverseStaticRelative(measurements, subtract);
      return new Layer2Medium(measurements, kw, getRho1(measurements, kw));
    }
    else {
      return new Layer1Medium(measurements);
    }
  }

  @Nonnull
  static MediumLayers<ValuePair> inverseDynamic(@Nonnull Collection<DerivativeMeasurement> measurements) {
    if (measurements.size() > 1) {
      RelativeMediumLayers<Double> kw = inverseDynamicRelative(measurements);
      return new Layer2Medium(measurements, kw, getRho1(measurements, kw));
    }
    else {
      return inverseStatic(measurements.stream().collect(Collectors.<Measurement>toUnmodifiableList()));
    }
  }

  @Nonnull
  @ParametersAreNonnullByDefault
  static RelativeMediumLayers<Double> inverseDynamicRelative(Collection<? extends DerivativeMeasurement> derivativeMeasurements) {
    var kMinMax = new double[] {-1.0, 1.0};
    if (derivativeMeasurements.stream().allMatch(d -> d.getDerivativeResistivity() > 0)) {
      kMinMax[1] = 0.0;
    }
    else if (derivativeMeasurements.stream().allMatch(d -> d.getDerivativeResistivity() < 0)) {
      kMinMax[0] = 0.0;
    }
    else if (derivativeMeasurements.stream().anyMatch(d -> d.getDerivativeResistivity() > 0) &&
        derivativeMeasurements.stream().anyMatch(d -> d.getDerivativeResistivity() < 0)) {
      return SINGLE_LAYER;
    }
    else {
      return inverseStaticRelative(derivativeMeasurements, UnaryOperator.identity());
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

  @Nonnull
  @ParametersAreNonnullByDefault
  static RelativeMediumLayers<Double> inverseStaticRelative(Collection<? extends Measurement> measurements, UnaryOperator<double[]> subtract) {
    double[] subLogApparent = subtract.apply(measurements.stream().mapToDouble(Measurement::getLogResistivity).toArray());
    var logApparentPredicted = logApparentPredicted(measurements);

    PointValuePair kwOptimal = Simplex.optimizeAll(kw -> {
          double[] subLogApparentPredicted = subtract.apply(
              measurements.stream()
                  .map(measurement -> measurement.getSystem().toExact())
                  .mapToDouble(s -> logApparentPredicted.applyAsDouble(s, kw))
                  .toArray()
          );
          return Inequality.absolute().applyAsDouble(subLogApparent, subLogApparentPredicted);
        },
        new SimpleBounds(new double[] {-1.0, 0.0}, new double[] {1.0, getMaxHToL(measurements)}),
        new double[] {0.01, 0.01}
    );
    return new Layer2RelativeMedium<>(kwOptimal.getPoint()[0], kwOptimal.getPoint()[1]);
  }

  @Nonnegative
  @ParametersAreNonnullByDefault
  private static double getRho1(Collection<? extends Measurement> measurements, RelativeMediumLayers<Double> kw) {
    double sumLogApparent = measurements.stream().mapToDouble(Measurement::getLogResistivity).sum();
    var logApparentPredicted = logApparentPredicted(measurements);
    double sumLogApparentPredicted = measurements.stream()
        .map(measurement -> measurement.getSystem().toExact())
        .mapToDouble(s -> logApparentPredicted.applyAsDouble(s, new double[] {kw.k12(), kw.hToL()})).sum();
    return exp((sumLogApparent - sumLogApparentPredicted) / measurements.size());
  }

  @Nonnegative
  private static double getMaxHToL(@Nonnull Collection<? extends Measurement> measurements) {
    return measurements.parallelStream()
        .mapToDouble(measurement -> measurement.getSystem().getHMax(1.0)).min().orElseThrow() / getBaseL(measurements);
  }

  private static ToDoubleBiFunction<TetrapolarSystem, double[]> logApparentPredicted(@Nonnull Collection<? extends Measurement> measurements) {
    double baseL = getBaseL(measurements);
    return (s, kw) -> new Log1pApparent2Rho(s.toRelative()).value(kw[0], kw[1] * baseL / s.getL());
  }

  private static ToDoubleBiFunction<TetrapolarSystem, double[]> logDiffApparentPredicted(@Nonnull Collection<? extends Measurement> measurements) {
    double baseL = getBaseL(measurements);
    return (s, kw) -> StrictMath.log(Math.abs(new DerivativeApparent2Rho(s.toRelative()).value(kw[0], kw[1] * baseL / s.getL())));
  }
}
