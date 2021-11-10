package com.ak.rsm;

import java.util.Collection;
import java.util.List;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.inverse.Inequality;
import com.ak.math.Simplex;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleBounds;

import static com.ak.rsm.Measurements.getMaxHToL;
import static com.ak.rsm.Measurements.logApparentPredicted;
import static com.ak.rsm.Measurements.logDiffApparentPredicted;
import static com.ak.rsm.RelativeMediumLayers.NAN;
import static java.lang.StrictMath.abs;
import static java.lang.StrictMath.log;

enum InverseDynamic implements Inverseable<DerivativeMeasurement> {
  INSTANCE;

  @Nonnull
  @Override
  public MediumLayers inverse(@Nonnull Collection<? extends DerivativeMeasurement> measurements) {
    if (measurements.size() > 1) {
      return new Layer2Medium(measurements, inverseRelative(measurements));
    }
    else {
      return InverseStatic.INSTANCE.inverse(measurements);
    }
  }

  @Nonnull
  @Override
  public RelativeMediumLayers inverseRelative(@Nonnull Collection<? extends DerivativeMeasurement> measurements) {
    var kMinMax = new double[] {-1.0, 1.0};
    if (measurements.stream().allMatch(d -> d.derivativeResistivity() > 0)) {
      kMinMax[1] = 0.0;
    }
    else if (measurements.stream().allMatch(d -> d.derivativeResistivity() < 0)) {
      kMinMax[0] = 0.0;
    }
    else if (measurements.stream().anyMatch(d -> d.derivativeResistivity() > 0) &&
        measurements.stream().anyMatch(d -> d.derivativeResistivity() < 0)) {
      return NAN;
    }
    else {
      return InverseStatic.INSTANCE.inverseRelative(measurements);
    }

    double[] subLog = measurements.stream().mapToDouble(d -> log(d.resistivity()) - log(abs(d.derivativeResistivity()))).toArray();
    var logApparentPredicted = logApparentPredicted(measurements);
    var logDiffApparentPredicted = logDiffApparentPredicted(measurements);

    List<TetrapolarSystem> tetrapolarSystems = measurements.stream().map(Measurement::system).toList();
    PointValuePair kwOptimal = Simplex.optimizeAll(
        kw -> {
          double[] subLogPredicted = tetrapolarSystems.stream()
              .mapToDouble(s -> logApparentPredicted.applyAsDouble(s, kw) - logDiffApparentPredicted.applyAsDouble(s, kw))
              .toArray();
          return Inequality.absolute().applyAsDouble(subLog, subLogPredicted);
        },
        new SimpleBounds(new double[] {kMinMax[0], 0.0}, new double[] {kMinMax[1], getMaxHToL(measurements)}),
        new double[] {0.01, 0.01}
    );
    return errors(tetrapolarSystems, new Layer2RelativeMedium(kwOptimal.getPoint()));
  }

  @Override
  @Nonnull
  @ParametersAreNonnullByDefault
  public RelativeMediumLayers errors(Collection<TetrapolarSystem> systems, RelativeMediumLayers layers) {
    double[][] a2 = systems.stream().map(s -> {
      RelativeTetrapolarSystem system = s.toRelative();
      double denominator2 = Apparent2Rho.newDerivativeApparentByPhi2Rho(system).applyAsDouble(layers);
      return new double[] {
          Apparent2Rho.newSecondDerivativeApparentByPhiK2Rho(system).applyAsDouble(layers) / denominator2,
          Apparent2Rho.newSecondDerivativeApparentByPhiPhi2Rho(system).applyAsDouble(layers) / denominator2
      };
    }).toArray(double[][]::new);

    double baseL = systems.stream().mapToDouble(TetrapolarSystem::getL).max().orElseThrow();
    double rho1 = 1.0;
    double rho2 = rho1 / Layers.getRho1ToRho2(layers.k12());
    double h = layers.hToL() * baseL;
    double dh = h * 1.0e-4;

    Function<Collection<TetrapolarSystem>, List<DerivativeMeasurement>> toMeasurements =
        ts -> TetrapolarDerivativeMeasurement.of(systems.toArray(TetrapolarSystem[]::new),
            ts.stream()
                .mapToDouble(s -> new Resistance2Layer(s).value(rho1, rho2, h)).toArray(),
            ts.stream()
                .mapToDouble(s -> new Resistance2Layer(s).value(rho1, rho2, h + dh)).toArray(),
            dh
        );

    DoubleUnaryOperator logAbs = x -> log(Math.abs(x));

    var baseM = toMeasurements.apply(systems);
    return InverseStatic.errors(systems, layers, UnaryOperator.identity(),
        a -> new Array2DRowRealMatrix(a).subtract(new Array2DRowRealMatrix(a2)).getData(),
        (ts, b) -> {
          var measurements = toMeasurements.apply(ts);
          double[] b2 = new double[baseM.size()];
          for (int i = 0; i < baseM.size(); i++) {
            b2[i] = b[i];
            b2[i] -= logAbs.applyAsDouble(measurements.get(i).derivativeResistivity()) -
                logAbs.applyAsDouble(baseM.get(i).derivativeResistivity());
          }
          return b2;
        }
    );
  }
}
