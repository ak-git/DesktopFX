package com.ak.rsm;

import java.util.Collection;
import java.util.function.DoubleBinaryOperator;
import java.util.function.ToDoubleBiFunction;
import java.util.function.UnaryOperator;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.math.ValuePair;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.SingularValueDecomposition;

import static java.lang.StrictMath.log;
import static java.lang.StrictMath.pow;

enum Measurements {
  ;

  static final UnaryOperator<double[]> SUBTRACT = newSubtract((left, right) -> left - right);
  static final UnaryOperator<double[][]> SUBTRACT_MATRIX = newMatrixSubtract((left, right) -> left - right);
  static final UnaryOperator<double[]> PLUS_ERRORS = newSubtract((left, right) -> Math.abs(left) + Math.abs(right));

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
  static double[][] getAMatrix(Collection<TetrapolarSystem> systems, RelativeMediumLayers layers, UnaryOperator<double[][]> subtract) {
    return subtract.apply(systems.stream().map(s -> {
      RelativeTetrapolarSystem system = s.toRelative();
      double denominator = Apparent2Rho.newNormalizedApparent2Rho(system).applyAsDouble(layers);
      return new double[] {
          Apparent2Rho.newDerivativeApparentByK2Rho(system).applyAsDouble(layers) / denominator,
          Apparent2Rho.newDerivativeApparentByPhi2Rho(system).applyAsDouble(layers) / denominator
      };
    }).toArray(double[][]::new));
  }

  @Nonnull
  @ParametersAreNonnullByDefault
  static Layer2RelativeMedium getLayer2RelativeMedium(RelativeMediumLayers layers, double[][] a, double[] logRhoAbsErrors) {
    DecompositionSolver solver = new SingularValueDecomposition(new Array2DRowRealMatrix(a)).getSolver();
    double[] kwErrors = solver.solve(new ArrayRealVector(logRhoAbsErrors)).toArray();
    return new Layer2RelativeMedium(ValuePair.Name.K12.of(layers.k12(), kwErrors[0]), ValuePair.Name.H_L.of(layers.hToL(), kwErrors[1]));
  }

  @Nonnull
  private static UnaryOperator<double[]> newSubtract(@Nonnull DoubleBinaryOperator operator) {
    return values -> {
      var sub = new double[values.length - 1];
      for (var i = 0; i < sub.length; i++) {
        sub[i] = operator.applyAsDouble(values[i + 1], values[i]);
      }
      return sub;
    };
  }

  @Nonnull
  private static UnaryOperator<double[][]> newMatrixSubtract(@Nonnull DoubleBinaryOperator operator) {
    return values -> {
      var sub = new double[values.length - 1][values[0].length];
      for (var i = 0; i < sub.length; i++) {
        for (var j = 0; j < sub[0].length; j++) {
          sub[i][j] = operator.applyAsDouble(values[i + 1][j], values[i][j]);
        }
      }
      return sub;
    };
  }
}
