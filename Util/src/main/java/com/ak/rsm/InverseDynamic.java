package com.ak.rsm;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.DoubleSupplier;
import java.util.function.ToDoubleBiFunction;
import java.util.stream.IntStream;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.inverse.Inequality;
import com.ak.math.Simplex;
import com.ak.math.ValuePair;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;
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

    Collection<TetrapolarSystem> tetrapolarSystems = measurements.stream().map(Measurement::getSystem).toList();
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

  @Nonnull
  @ParametersAreNonnullByDefault
  private static RelativeMediumLayers errors(Collection<TetrapolarSystem> systems, RelativeMediumLayers layers) {
    double[] logRhoAbsErrors = systems.stream().mapToDouble(TetrapolarSystem::getApparentRelativeError).toArray();

    double[] kwErrors = IntStream.range(0, 1 << (logRhoAbsErrors.length - 1))
        .mapToObj(n -> {
          var b = Arrays.copyOf(logRhoAbsErrors, logRhoAbsErrors.length);
          for (var i = 0; i < logRhoAbsErrors.length; i++) {
            if ((n & (1 << i)) == 1) {
              b[i] *= -1.0;
            }
          }

          ToDoubleBiFunction<RelativeTetrapolarSystem, DoubleSupplier> function =
              (system, doubleSupplier) -> doubleSupplier.getAsDouble() / Apparent2Rho.newNormalizedApparent2Rho(system).applyAsDouble(layers);
          ToDoubleBiFunction<RelativeTetrapolarSystem, DoubleSupplier> function2 =
              (system, doubleSupplier) -> doubleSupplier.getAsDouble() / Apparent2Rho.newDerivativeApparentByPhi2Rho(system).applyAsDouble(layers);

          double[] derivativeByK2Rho = systems.stream()
              .map(TetrapolarSystem::toRelative)
              .mapToDouble(system ->
                  function.applyAsDouble(system, () -> Apparent2Rho.newDerivativeApparentByK2Rho(system).applyAsDouble(layers)) -
                      function2.applyAsDouble(system, () -> Apparent2Rho.newSecondDerivativeApparentByPhiK2Rho(system).applyAsDouble(layers))
              )
              .toArray();

          double[] derivativeByPhi2Rho = systems.stream()
              .map(TetrapolarSystem::toRelative)
              .mapToDouble(system ->
                  function.applyAsDouble(system, () -> Apparent2Rho.newDerivativeApparentByPhi2Rho(system).applyAsDouble(layers)) -
                      function2.applyAsDouble(system, () -> Apparent2Rho.newSecondDerivativeApparentByPhiPhi2Rho(system).applyAsDouble(layers))
              )
              .toArray();

          RealMatrix a = new Array2DRowRealMatrix(logRhoAbsErrors.length, 2);
          for (var i = 0; i < logRhoAbsErrors.length; i++) {
            a.setEntry(i, 0, abs(derivativeByK2Rho[i]));
            a.setEntry(i, 1, abs(derivativeByPhi2Rho[i]));
          }
          return new SingularValueDecomposition(a).getSolver().solve(new ArrayRealVector(b)).toArray();
        })
        .reduce((v1, v2) -> {
          var max = new double[v1.length];
          for (var i = 0; i < max.length; i++) {
            max[i] = Math.max(Math.abs(v1[i]), Math.abs(v2[i]));
          }
          return max;
        })
        .orElseThrow();
    return new Layer2RelativeMedium(ValuePair.Name.K12.of(layers.k12(), kwErrors[0]), ValuePair.Name.H_L.of(layers.hToL(), kwErrors[1]));
  }
}
