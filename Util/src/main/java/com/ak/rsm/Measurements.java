package com.ak.rsm;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.ToDoubleBiFunction;
import java.util.stream.IntStream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.math.ValuePair;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;

import static java.lang.StrictMath.log;
import static java.lang.StrictMath.pow;

enum Measurements {
  ;

  @Nonnegative
  static double getBaseL(@Nonnull Collection<? extends Measurement> measurements) {
    return measurements.parallelStream().mapToDouble(m -> m.getSystem().getL()).max().orElseThrow();
  }

  @Nonnegative
  static double getMaxHToL(@Nonnull Collection<? extends Measurement> measurements) {
    return measurements.parallelStream()
        .mapToDouble(measurement -> measurement.getSystem().getHMax(1.0)).min().orElseThrow() / getBaseL(measurements);
  }

  @Nonnull
  static ToDoubleBiFunction<TetrapolarSystem, double[]> logApparentPredicted(@Nonnull Collection<? extends Measurement> measurements) {
    double baseL = getBaseL(measurements);
    return (s, kw) -> Apparent2Rho.newLog1pApparent2Rho(s.toRelative()).applyAsDouble(new Layer2RelativeMedium(kw[0], kw[1] * baseL / s.getL()));
  }

  @Nonnull
  static ToDoubleBiFunction<TetrapolarSystem, double[]> logDiffApparentPredicted(@Nonnull Collection<? extends Measurement> measurements) {
    double baseL = getBaseL(measurements);
    return (s, kw) -> log(Math.abs(Apparent2Rho.newDerivativeApparentByPhi2Rho(s.toRelative()).applyAsDouble(new Layer2RelativeMedium(kw[0], kw[1] * baseL / s.getL()))));
  }

  @Nonnull
  @ParametersAreNonnullByDefault
  static ValuePair getRho1(Collection<? extends Measurement> measurements, RelativeMediumLayers kw) {
    if (RelativeMediumLayers.SINGLE_LAYER.equals(kw)) {
      Measurement average = measurements.stream().map(Measurement.class::cast).reduce(Measurement::merge).orElseThrow();
      double rho = average.getResistivity();
      return ValuePair.Name.RHO_1.of(rho, rho * average.getSystem().getApparentRelativeError());
    }
    else {
      double baseL = getBaseL(measurements);
      return measurements.stream().parallel()
          .map(measurement -> {
            TetrapolarSystem s = measurement.getSystem();
            double normApparent = Apparent2Rho.newNormalizedApparent2Rho(s.toRelative()).applyAsDouble(new Layer2RelativeMedium(kw.k12(), kw.hToL() * baseL / s.getL()));

            double fK = Math.abs(Apparent2Rho.newDerivativeApparentByK2Rho(s.toRelative()).applyAsDouble(kw) * kw.k12AbsError());
            double fPhi = Math.abs(Apparent2Rho.newDerivativeApparentByPhi2Rho(s.toRelative()).applyAsDouble(kw) * kw.hToLAbsError());

            return ValuePair.Name.RHO_1.of(measurement.getResistivity() / normApparent,
                (fK + fPhi) * measurement.getResistivity() / pow(normApparent, 2.0)
            );
          })
          .reduce(ValuePair::mergeWith).orElseThrow();
    }
  }

  @Nonnull
  @ParametersAreNonnullByDefault
  static Layer2RelativeMedium getLayer2RelativeMedium(RelativeMediumLayers layers, RealMatrix a, double[] logRhoAbsErrors) {
    for (int i = 0; i < a.getRowDimension(); i++) {
      for (int j = 0; j < a.getColumnDimension(); j++) {
        a.setEntry(i, j, Math.abs(a.getEntry(i, j)));
      }
    }
    DecompositionSolver solver = new SingularValueDecomposition(a).getSolver();
    double[] kwErrors = IntStream.range(0, 1 << (logRhoAbsErrors.length - 1))
        .mapToObj(n -> {
          var b = Arrays.copyOf(logRhoAbsErrors, logRhoAbsErrors.length);
          for (var i = 0; i < logRhoAbsErrors.length; i++) {
            if ((n & (1 << i)) == 0) {
              b[i] *= -1.0;
            }
          }
          return solver.solve(new ArrayRealVector(b)).toArray();
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
