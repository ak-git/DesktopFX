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

import static com.ak.rsm.RelativeMediumLayers.SINGLE_LAYER;
import static java.lang.StrictMath.exp;

enum Inverse {
  ;

  private static final ToDoubleBiFunction<RelativeTetrapolarSystem, double[]> LOG_APPARENT_PREDICTED =
      (s, kw) -> new Log1pApparent2Rho(s).value(kw[0], kw[1]);

  private static final ToDoubleBiFunction<RelativeTetrapolarSystem, double[]> LOG_DIFF_APPARENT_PREDICTED =
      (s, kw) -> StrictMath.log(Math.abs(new DerivativeApparent2Rho(s).value(kw[0], kw[1])));

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
      double rho1 = getRho1(measurements, kw);
      double rho2 = rho1 / Layers.getRho1ToRho2(kw.k12());

      return new Layer2Medium.Layer2MediumBuilder<ValuePair>(
          measurements.stream().map(m -> new TetrapolarPrediction(m, kw, rho1)).collect(Collectors.toUnmodifiableList()))
          .layer1(
              new ValuePair(rho1, 0.0),
              new ValuePair(kw.hToL() * getBaseL(measurements), 0.0)
          )
          .layer2(new ValuePair(rho2, 0.0))
          .build();
    }
    else {
      Measurement average = measurements.stream().reduce(Measurement::merge).orElseThrow();
      return new Layer1Medium.Layer1MediumBuilder(
          measurements.stream()
              .map(m -> new TetrapolarPrediction(m, SINGLE_LAYER, average.getResistivity()))
              .collect(Collectors.toUnmodifiableList()))
          .layer1(average).build();
    }
  }

  @Nonnull
  static MediumLayers<ValuePair> inverseDynamic(@Nonnull Collection<DerivativeMeasurement> measurements) {
    if (measurements.size() > 1) {
      RelativeMediumLayers<Double> kw = inverseDynamicRelative(measurements);
      double rho1 = getRho1(measurements, kw);
      double rho2 = rho1 / Layers.getRho1ToRho2(kw.k12());

      return new Layer2Medium.Layer2MediumBuilder<ValuePair>(
          measurements.stream().map(m -> new TetrapolarDerivativePrediction(m, kw, rho1)).collect(Collectors.toUnmodifiableList()))
          .layer1(
              new ValuePair(rho1, 0.0),
              new ValuePair(kw.hToL() * getBaseL(measurements), 0.0)
          )
          .layer2(new ValuePair(rho2, 0.0))
          .build();
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
    double baseL = getBaseL(derivativeMeasurements);
    PointValuePair kwOptimal = Simplex.optimizeAll(
        kw -> {
          double[] subLogPredicted = derivativeMeasurements.stream()
              .map(measurement -> measurement.getSystem().toExact())
              .mapToDouble(s -> {
                var p = new double[] {kw[0], kw[1] * baseL / s.getL()};
                return LOG_APPARENT_PREDICTED.applyAsDouble(s.toRelative(), p) - LOG_DIFF_APPARENT_PREDICTED.applyAsDouble(s.toRelative(), p);
              })
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
    double baseL = getBaseL(measurements);
    PointValuePair kwOptimal = Simplex.optimizeAll(kw -> {
          double[] subLogApparentPredicted = subtract.apply(
              measurements.stream()
                  .map(measurement -> measurement.getSystem().toExact())
                  .mapToDouble(s ->
                      LOG_APPARENT_PREDICTED.applyAsDouble(s.toRelative(), new double[] {kw[0], kw[1] * baseL / s.getL()})
                  )
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
  private static double getRho1(Collection<? extends Measurement> measurements, RelativeMediumLayers<Double> kh) {
    double sumLogApparent = measurements.stream().mapToDouble(Measurement::getLogResistivity).sum();
    double baseL = getBaseL(measurements);
    double sumLogApparentPredicted = measurements.stream()
        .map(measurement -> measurement.getSystem().toExact())
        .mapToDouble(s -> LOG_APPARENT_PREDICTED.applyAsDouble(s.toRelative(), new double[] {kh.k12(), kh.hToL() * baseL / s.getL()})).sum();
    return exp((sumLogApparent - sumLogApparentPredicted) / measurements.size());
  }

  @Nonnegative
  private static double getMaxHToL(@Nonnull Collection<? extends Measurement> measurements) {
    return measurements.parallelStream()
        .mapToDouble(measurement -> measurement.getSystem().getHMax(1.0)).min().orElseThrow() / getBaseL(measurements);
  }

  @Nonnegative
  private static double getBaseL(@Nonnull Collection<? extends Measurement> measurements) {
    return measurements.parallelStream().mapToDouble(m -> m.getSystem().toExact().getL()).max().orElseThrow();
  }
}
