package com.ak.rsm;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.inverse.Inequality;
import com.ak.math.Simplex;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleBounds;

import static com.ak.rsm.Measurements.PLUS_ERRORS;
import static com.ak.rsm.Measurements.SUBTRACT;
import static com.ak.rsm.Measurements.SUBTRACT_MATRIX;
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

    double[] subLogApparent = SUBTRACT.apply(measurements.stream().mapToDouble(d -> log(d.getResistivity())).toArray());
    double[] subLogDiffApparent = SUBTRACT.apply(measurements.stream().mapToDouble(d -> log(abs(d.getDerivativeResistivity()))).toArray());
    var logApparentPredicted = logApparentPredicted(measurements);
    var logDiffApparentPredicted = logDiffApparentPredicted(measurements);

    List<TetrapolarSystem> tetrapolarSystems = measurements.stream().map(Measurement::getSystem).toList();
    PointValuePair kwOptimal = Simplex.optimizeAll(
        kw -> {
          double[] subLogPredicted = SUBTRACT.apply(tetrapolarSystems.stream()
              .mapToDouble(s -> logApparentPredicted.applyAsDouble(s, kw))
              .toArray());
          double[] subLogDiffPredicted = SUBTRACT.apply(tetrapolarSystems.stream()
              .mapToDouble(s -> logDiffApparentPredicted.applyAsDouble(s, kw))
              .toArray());
          Inequality absolute = Inequality.absolute();
          absolute.applyAsDouble(subLogApparent, subLogPredicted);
          absolute.applyAsDouble(subLogDiffApparent, subLogDiffPredicted);
          return absolute.getAsDouble();
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
    double[] logRhoAbsErrors = PLUS_ERRORS.apply(systems.stream().mapToDouble(TetrapolarSystem::getApparentRelativeError).toArray());
    double[] logDiffRhoAbsErrors = PLUS_ERRORS.apply(systems.stream().mapToDouble(TetrapolarSystem::getDiffApparentRelativeError).toArray());

    double[][] a1 = getAMatrix(systems, layers, SUBTRACT_MATRIX);
    double[][] a2 = getAMatrix2(systems, layers);

    RealMatrix a = new Array2DRowRealMatrix(a1.length + a2.length, Math.min(a1[0].length, a2[0].length));
    a.setSubMatrix(a1, 0, 0);
    a.setSubMatrix(a2, a1.length, 0);

    ArrayRealVector b = new ArrayRealVector(logRhoAbsErrors).append(new ArrayRealVector(logDiffRhoAbsErrors));
    return getLayer2RelativeMedium(layers, a.getData(), b.toArray());
  }

  @Nonnull
  @ParametersAreNonnullByDefault
  static double[][] getAMatrix2(Collection<TetrapolarSystem> systems, RelativeMediumLayers layers) {
    return SUBTRACT_MATRIX.apply(systems.stream().map(s -> {
      RelativeTetrapolarSystem system = s.toRelative();
      double denominator2 = Apparent2Rho.newDerivativeApparentByPhi2Rho(system).applyAsDouble(layers);
      return new double[] {
          Apparent2Rho.newSecondDerivativeApparentByPhiK2Rho(system).applyAsDouble(layers) / denominator2,
          Apparent2Rho.newSecondDerivativeApparentByPhiPhi2Rho(system).applyAsDouble(layers) / denominator2
      };
    }).toArray(double[][]::new));
  }
}
