package com.ak.rsm;

import java.util.Collection;
import java.util.List;
import java.util.function.UnaryOperator;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.inverse.Inequality;
import com.ak.math.Simplex;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleBounds;

import static com.ak.rsm.Measurements.getAMatrix;
import static com.ak.rsm.Measurements.getLayer2RelativeMedium;
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
    if (measurements.stream().allMatch(d -> d.getDerivativeResistivity() > 0)) {
      kMinMax[1] = 0.0;
    }
    else if (measurements.stream().allMatch(d -> d.getDerivativeResistivity() < 0)) {
      kMinMax[0] = 0.0;
    }
    else if (measurements.stream().anyMatch(d -> d.getDerivativeResistivity() > 0) &&
        measurements.stream().anyMatch(d -> d.getDerivativeResistivity() < 0)) {
      return NAN;
    }
    else {
      return InverseStatic.INSTANCE.inverseRelative(measurements);
    }

    double[] subLog = measurements.stream().mapToDouble(d -> log(d.getResistivity()) - log(abs(d.getDerivativeResistivity()))).toArray();
    var logApparentPredicted = logApparentPredicted(measurements);
    var logDiffApparentPredicted = logDiffApparentPredicted(measurements);

    List<TetrapolarSystem> tetrapolarSystems = measurements.stream().map(Measurement::getSystem).toList();
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
    double[] logRhoAbsErrors = systems.stream().mapToDouble(s -> s.getApparentRelativeError() + s.getDiffApparentRelativeError()).toArray();
    double[][] a2 = systems.stream().map(s -> {
      RelativeTetrapolarSystem system = s.toRelative();
      double denominator2 = Apparent2Rho.newDerivativeApparentByPhi2Rho(system).applyAsDouble(layers);
      return new double[] {
          -Apparent2Rho.newSecondDerivativeApparentByPhiK2Rho(system).applyAsDouble(layers) / denominator2,
          -Apparent2Rho.newSecondDerivativeApparentByPhiPhi2Rho(system).applyAsDouble(layers) / denominator2
      };
    }).toArray(double[][]::new);

    double[][] a = new Array2DRowRealMatrix(getAMatrix(systems, layers, UnaryOperator.identity()), false)
        .add(new Array2DRowRealMatrix(a2, false)).getData();
    return getLayer2RelativeMedium(layers, a, logRhoAbsErrors);
  }
}
