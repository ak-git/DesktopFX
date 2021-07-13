package com.ak.rsm;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.DoubleSupplier;
import java.util.function.ToDoubleBiFunction;
import java.util.function.UnaryOperator;
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
import static java.lang.StrictMath.abs;
import static java.lang.StrictMath.log;

enum InverseStatic implements Inverseable<Measurement> {
  INSTANCE;

  private static final UnaryOperator<double[]> SUBTRACT = values -> {
    var sub = new double[values.length - 1];
    for (var i = 0; i < sub.length; i++) {
      sub[i] = values[i + 1] - values[i];
    }
    return sub;
  };

  @Nonnull
  @Override
  public MediumLayers inverse(@Nonnull Collection<? extends Measurement> measurements) {
    if (measurements.size() > 2) {
      return new Layer2Medium(measurements, inverseRelative(measurements, SUBTRACT));
    }
    else {
      return new Layer1Medium(measurements);
    }
  }

  @Nonnull
  @Override
  public RelativeMediumLayers inverseRelative(@Nonnull Collection<? extends Measurement> measurements) {
    return inverseRelative(measurements, UnaryOperator.identity());
  }

  @Nonnull
  @ParametersAreNonnullByDefault
  private static RelativeMediumLayers inverseRelative(Collection<? extends Measurement> measurements, UnaryOperator<double[]> subtract) {
    double[] subLogApparent = subtract.apply(measurements.stream().mapToDouble(x -> log(x.getResistivity())).toArray());
    var logApparentPredicted = logApparentPredicted(measurements);

    Collection<TetrapolarSystem> tetrapolarSystems = measurements.stream().map(Measurement::getSystem).toList();
    PointValuePair kwOptimal = Simplex.optimizeAll(kw -> {
          double[] subLogApparentPredicted = subtract.apply(
              tetrapolarSystems.stream()
                  .mapToDouble(s -> logApparentPredicted.applyAsDouble(s, kw))
                  .toArray()
          );
          return Inequality.absolute().applyAsDouble(subLogApparent, subLogApparentPredicted);
        },
        new SimpleBounds(new double[] {-1.0, 0.0}, new double[] {1.0, getMaxHToL(measurements)}),
        new double[] {0.01, 0.01}
    );
    return errors(tetrapolarSystems, new Layer2RelativeMedium(kwOptimal.getPoint()), subtract);
  }

  @Nonnull
  @ParametersAreNonnullByDefault
  private static RelativeMediumLayers errors(Collection<TetrapolarSystem> systems, RelativeMediumLayers layers,
                                             UnaryOperator<double[]> subtract) {
    double[] logRhoAbsErrors = subtract.apply(systems.stream().mapToDouble(TetrapolarSystem::getApparentRelativeError).toArray());

    double[] kwErrors = IntStream.range(0, 1 << (logRhoAbsErrors.length - 1))
        .mapToObj(n -> {
          var b = Arrays.copyOf(logRhoAbsErrors, logRhoAbsErrors.length);
          for (var i = 0; i < logRhoAbsErrors.length; i++) {
            if ((n & (1 << i)) == 1) {
              b[i] *= -1.0;
            }
          }

          ToDoubleBiFunction<RelativeTetrapolarSystem, DoubleSupplier> function =
              (system, doubleSupplier) -> doubleSupplier.getAsDouble() / new NormalizedApparent2Rho(system).value(layers.k12(), layers.hToL());

          double[] derivativeApparentByK2Rho = subtract.apply(systems.stream()
              .map(TetrapolarSystem::toRelative)
              .mapToDouble(system ->
                  function.applyAsDouble(system, () -> new DerivativeApparentByK2Rho(system).value(layers.k12(), layers.hToL())))
              .toArray());

          double[] derivativeApparentByPhi2Rho = subtract.apply(systems.stream()
              .map(TetrapolarSystem::toRelative)
              .mapToDouble(system ->
                  function.applyAsDouble(system, () -> new DerivativeApparentByPhi2Rho(system).value(layers.k12(), layers.hToL())))
              .toArray());

          RealMatrix a = new Array2DRowRealMatrix(logRhoAbsErrors.length, 2);
          for (var i = 0; i < logRhoAbsErrors.length; i++) {
            a.setEntry(i, 0, abs(derivativeApparentByK2Rho[i]));
            a.setEntry(i, 1, abs(derivativeApparentByPhi2Rho[i]));
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
