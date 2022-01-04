package com.ak.rsm.inverse;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.inverse.Inequality;
import com.ak.math.Simplex;
import com.ak.rsm.apparent.Apparent2Rho;
import com.ak.rsm.measurement.DerivativeMeasurement;
import com.ak.rsm.measurement.Measurement;
import com.ak.rsm.measurement.Measurements;
import com.ak.rsm.medium.Layer2Medium;
import com.ak.rsm.medium.MediumLayers;
import com.ak.rsm.relative.Layer2RelativeMedium;
import com.ak.rsm.relative.RelativeMediumLayers;
import com.ak.rsm.resistance.DerivativeResistance;
import com.ak.rsm.resistance.TetrapolarDerivativeResistance;
import com.ak.rsm.system.InexactTetrapolarSystem;
import com.ak.rsm.system.Layers;
import com.ak.rsm.system.TetrapolarSystem;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleBounds;

import static com.ak.rsm.measurement.Measurements.logApparentPredicted;
import static com.ak.rsm.measurement.Measurements.logDiffApparentPredicted;
import static com.ak.rsm.relative.RelativeMediumLayers.NAN;
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

    List<InexactTetrapolarSystem> inexactSystems = measurements.stream().map(Measurement::inexact).toList();
    Collection<TetrapolarSystem> systems = inexactSystems.stream().map(InexactTetrapolarSystem::system).toList();
    var logApparentPredicted = logApparentPredicted(systems);
    var logDiffApparentPredicted = logDiffApparentPredicted(systems);

    PointValuePair kwOptimal = Simplex.optimizeAll(
        kw -> {
          double[] subLogPredicted = systems.stream()
              .mapToDouble(s -> logApparentPredicted.applyAsDouble(s, kw) - logDiffApparentPredicted.applyAsDouble(s, kw))
              .toArray();
          return Inequality.absolute().applyAsDouble(subLog, subLogPredicted);
        },
        new SimpleBounds(new double[] {kMinMax[0], 0.0}, new double[] {kMinMax[1], Measurements.getMaxHToL(inexactSystems)}),
        new double[] {0.01, 0.01}
    );
    return errors(inexactSystems, new Layer2RelativeMedium(kwOptimal.getPoint()));
  }

  @Override
  @Nonnull
  @ParametersAreNonnullByDefault
  public RelativeMediumLayers errors(Collection<InexactTetrapolarSystem> inexactSystems, RelativeMediumLayers layers) {
    Collection<TetrapolarSystem> systems = inexactSystems.stream().map(InexactTetrapolarSystem::system).toList();
    double[][] a2 = systems.stream().map(TetrapolarSystem::relativeSystem)
        .map(s -> {
          double denominator2 = Apparent2Rho.newDerivativeApparentByPhi2Rho(s).applyAsDouble(layers);
          return new double[] {
              Apparent2Rho.newSecondDerivativeApparentByPhiK2Rho(s).applyAsDouble(layers) / denominator2,
              Apparent2Rho.newSecondDerivativeApparentByPhiPhi2Rho(s).applyAsDouble(layers) / denominator2
          };
        }).toArray(double[][]::new);

    double baseL = Measurements.getBaseL(systems);
    double rho1 = 1.0;
    double rho2 = rho1 / Layers.getRho1ToRho2(layers.k12());
    double h = layers.hToL() * baseL;
    double dh = h * 1.0e-4;

    Function<Collection<TetrapolarSystem>, List<DerivativeResistance>> toMeasurements =
        ts -> {
          Iterator<TetrapolarSystem> iterator = systems.iterator();
          return ts.stream().map(
              s -> {
                double ohms = TetrapolarDerivativeResistance.of(s).dh(dh).rho1(rho1).rho2(rho2).h(h).ohms();
                return TetrapolarDerivativeResistance.of(iterator.next()).dh(dh).ofOhms(ohms);
              }
          ).toList();
        };

    DoubleUnaryOperator logAbs = x -> log(Math.abs(x));

    var baseM = toMeasurements.apply(systems);
    return InverseStatic.errors(inexactSystems, layers, UnaryOperator.identity(),
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

