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
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleBounds;

import static java.lang.StrictMath.exp;

enum Inverse {
  ;

  private static final ToDoubleBiFunction<? super TetrapolarSystem, double[]> LOG_APPARENT_PREDICTED =
      (s, kh) -> new Log1pApparent2Rho(s.toRelative()).value(kh[0], kh[1] / s.getL());

  private static final ToDoubleBiFunction<? super TetrapolarSystem, double[]> LOG_DIFF_APPARENT_PREDICTED =
      (s, kh) -> StrictMath.log(Math.abs(new DerivativeApparent2Rho(s).value(kh[0], kh[1] / s.getL())));


  @Nonnull
  public static MediumLayers inverseStatic(@Nonnull Collection<Measurement> measurements) {
    if (measurements.size() > 2) {
      RelativeMediumLayers kh = inverseStaticRelative(measurements, values -> {
        double[] sub = new double[values.length - 1];
        for (int i = 0; i < sub.length; i++) {
          sub[i] = values[i + 1] - values[i];
        }
        return sub;
      });
      double rho1 = getRho1(measurements, kh);
      return new Layer2Medium.Layer2MediumBuilder(
          measurements.stream()
              .map(m -> TetrapolarPrediction.of(m, kh, rho1))
              .collect(Collectors.toUnmodifiableList()))
          .layer1(rho1, kh.h()).layer2(rho1 / Layers.getRho1ToRho2(kh.k12())).build();
    }
    else {
      Measurement average = measurements.stream().reduce(Measurement::merge).orElseThrow();
      return new Layer1Medium.Layer1MediumBuilder(
          measurements.stream()
              .map(m -> new TetrapolarPrediction(m, average.getResistivity()))
              .collect(Collectors.toUnmodifiableList()))
          .layer1(average.getResistivity()).build();
    }
  }

  @Nonnull
  public static MediumLayers inverseDynamic(@Nonnull Collection<DerivativeMeasurement> measurements) {
    if (measurements.size() > 1) {
      RelativeMediumLayers initial = new RelativeMediumLayers() {
        @Override
        public double k12() {
          return measurements.stream().allMatch(d -> d.getDerivativeResistivity() > 0) ? -1.0 : 1.0;
        }

        @Override
        public double h() {
          return getMaxL(measurements);
        }
      };
      RelativeMediumLayers kh = inverseDynamicRelative(measurements, initial);
      double rho1 = getRho1(measurements, kh);
      return new Layer2Medium.Layer2MediumBuilder(
          measurements.stream()
              .map(m -> TetrapolarDerivativePrediction.of(m, kh, rho1))
              .collect(Collectors.toUnmodifiableList()))
          .layer1(rho1, kh.h()).layer2(rho1 / Layers.getRho1ToRho2(kh.k12())).build();
    }
    else {
      return inverseStatic(measurements.stream().collect(Collectors.<Measurement>toUnmodifiableList()));
    }
  }

  @Nonnull
  @ParametersAreNonnullByDefault
  public static RelativeMediumLayers inverseDynamicRelative(Collection<DerivativeMeasurement> derivativeMeasurements,
                                                            RelativeMediumLayers initial) {
    double[] kMinMax = {-1.0, 1.0};
    if (initial.k12() > 0.0) {
      kMinMax = new double[] {0.0, 1.0};
    }
    else if (initial.k12() < 0.0) {
      kMinMax = new double[] {-1.0, 0.0};
    }

    double[] subLog = derivativeMeasurements.stream().mapToDouble(d -> d.getLogResistivity() - d.getDerivativeLogResistivity()).toArray();
    double maxL = getMaxL(derivativeMeasurements);
    PointValuePair kwOptimal = Simplex.optimize("", kw -> {
          double[] subLogPredicted = derivativeMeasurements.stream()
              .map(Measurement::getSystem)
              .mapToDouble(s -> {
                double[] kh = {kw[0], kw[1] * maxL};
                return LOG_APPARENT_PREDICTED.applyAsDouble(s.toExact(), kh) -
                    LOG_DIFF_APPARENT_PREDICTED.applyAsDouble(s.toExact(), kh);
              })
              .toArray();
          return Inequality.absolute().applyAsDouble(subLog, subLogPredicted);
        },
        new SimpleBounds(new double[] {kMinMax[0], 0.0}, new double[] {kMinMax[1], 1.0}),
        new double[] {initial.k12(), initial.h() / maxL}, new double[] {0.01, initial.h() / maxL / 100.0}
    );
    return new Layer2RelativeMedium(kwOptimal.getPoint()[0], kwOptimal.getPoint()[1] * maxL);
  }

  @Nonnull
  @ParametersAreNonnullByDefault
  public static RelativeMediumLayers inverseStaticRelative(Collection<? extends Measurement> measurements,
                                                           UnaryOperator<double[]> subtract) {
    double[] subLogApparent = subtract.apply(measurements.stream().mapToDouble(Measurement::getLogResistivity).toArray());
    double maxL = getMaxL(measurements);
    PointValuePair kwOptimal = Simplex.optimizeCMAES(kw -> {
          double[] subLogApparentPredicted = subtract.apply(measurements.stream()
              .map(Measurement::getSystem)
              .mapToDouble(s -> LOG_APPARENT_PREDICTED.applyAsDouble(s.toExact(), new double[] {kw[0], kw[1] * maxL}))
              .toArray()
          );
          return Inequality.absolute().applyAsDouble(subLogApparent, subLogApparentPredicted);
        },
        new SimpleBounds(new double[] {-1.0, 0.0}, new double[] {1.0, 1.0}),
        new double[] {0.0, 0.1}, new double[] {0.01, 0.01}
    );
    return new Layer2RelativeMedium(kwOptimal.getPoint()[0], kwOptimal.getPoint()[1] * maxL);
  }

  @Nonnegative
  @ParametersAreNonnullByDefault
  private static double getRho1(Collection<? extends Measurement> measurements, RelativeMediumLayers kh) {
    double sumLogApparent = measurements.stream().mapToDouble(Measurement::getLogResistivity).sum();
    double sumLogApparentPredicted = measurements.stream()
        .map(Measurement::getSystem)
        .mapToDouble(s -> LOG_APPARENT_PREDICTED.applyAsDouble(s.toExact(), new double[] {kh.k12(), kh.h()})).sum();
    return exp((sumLogApparent - sumLogApparentPredicted) / measurements.size());
  }

  @Nonnegative
  @ParametersAreNonnullByDefault
  private static double getMaxL(Collection<? extends Measurement> measurements) {
    return measurements.stream().mapToDouble(m -> m.getSystem().toExact().getL()).max().orElseThrow();
  }
}
