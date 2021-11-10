package com.ak.rsm;

import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.inverse.Inequality;
import com.ak.math.Simplex;
import com.ak.math.ValuePair;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.SingularValueDecomposition;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleBounds;

import static com.ak.rsm.Measurements.getMaxHToL;
import static com.ak.rsm.Measurements.logApparentPredicted;
import static java.lang.StrictMath.log;
import static java.util.function.UnaryOperator.identity;

enum InverseStatic implements Inverseable<Measurement> {
  INSTANCE;

  static final BinaryOperator<Layer2RelativeMedium> MAX_ERROR = (v1, v2) -> {
    double kEMax = Math.max(v1.k12AbsError(), v2.k12AbsError());
    double hToLEMax = Math.max(v1.hToLAbsError(), v2.hToLAbsError());
    return new Layer2RelativeMedium(ValuePair.Name.K12.of(v1.k12(), kEMax), ValuePair.Name.H_L.of(v1.hToL(), hToLEMax));
  };

  private static final UnaryOperator<double[]> SUBTRACT = values -> {
    var sub = new double[values.length - 1];
    for (var i = 0; i < sub.length; i++) {
      sub[i] = values[i + 1] - values[i];
    }
    return sub;
  };

  private static final UnaryOperator<double[][]> SUBTRACT_MATRIX = values -> {
    var sub = new double[values.length - 1][values[0].length];
    for (var i = 0; i < sub.length; i++) {
      for (var j = 0; j < sub[0].length; j++) {
        sub[i][j] = values[i + 1][j] - values[i][j];
      }
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
    return inverseRelative(measurements, identity());
  }

  @Nonnull
  @ParametersAreNonnullByDefault
  private static RelativeMediumLayers inverseRelative(Collection<? extends Measurement> measurements, UnaryOperator<double[]> subtract) {
    double[] subLogApparent = subtract.apply(measurements.stream().mapToDouble(x -> log(x.resistivity())).toArray());
    var logApparentPredicted = logApparentPredicted(measurements);

    List<TetrapolarSystem> tetrapolarSystems = measurements.stream().map(Measurement::system).toList();
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
    return errors(tetrapolarSystems, new Layer2RelativeMedium(kwOptimal.getPoint()), subtract, identity(), (ts, b) -> b);
  }

  @Override
  @Nonnull
  @ParametersAreNonnullByDefault
  public RelativeMediumLayers errors(Collection<TetrapolarSystem> systems, RelativeMediumLayers layers) {
    return errors(systems, layers, identity(), identity(), (ts, b) -> b);
  }

  @Nonnull
  @ParametersAreNonnullByDefault
  static RelativeMediumLayers errors(Collection<TetrapolarSystem> systems, RelativeMediumLayers layers,
                                     UnaryOperator<double[]> subtract, UnaryOperator<double[][]> fixA,
                                     BiFunction<Collection<TetrapolarSystem>, double[], double[]> fixB) {
    double[][] a = getAMatrix(systems, layers, subtract.equals(SUBTRACT) ? SUBTRACT_MATRIX : fixA);

    double baseL = systems.stream().mapToDouble(TetrapolarSystem::getL).max().orElseThrow();
    double rho2 = 1.0 / Layers.getRho1ToRho2(layers.k12());
    double h = layers.hToL() * baseL;

    Function<Collection<TetrapolarSystem>, List<Measurement>> toMeasurements =
        ts -> TetrapolarMeasurement.of(
            systems.toArray(TetrapolarSystem[]::new),
            ts.stream().mapToDouble(s -> new Resistance2Layer(s).value(1.0, rho2, h)).toArray()
        );

    var baseM = toMeasurements.apply(systems);
    return TetrapolarSystem.getMeasurementsCombination(systems).stream()
        .map(systemList -> {
          var measurements = toMeasurements.apply(systemList);
          double[] b = new double[baseM.size()];
          for (int i = 0; i < baseM.size(); i++) {
            b[i] = log(measurements.get(i).resistivity()) - log(baseM.get(i).resistivity());
          }
          return fixB.apply(systemList, b);
        })
        .map(subtract)
        .map(b -> {
          DecompositionSolver solver = new SingularValueDecomposition(new Array2DRowRealMatrix(a)).getSolver();
          double[] kwErrors = solver.solve(new ArrayRealVector(b)).toArray();
          return new Layer2RelativeMedium(ValuePair.Name.K12.of(layers.k12(), kwErrors[0]), ValuePair.Name.H_L.of(layers.hToL(), kwErrors[1]));
        })
        .reduce(MAX_ERROR).orElseThrow();
  }

  @Nonnull
  @ParametersAreNonnullByDefault
  private static double[][] getAMatrix(Collection<TetrapolarSystem> systems, RelativeMediumLayers layers, UnaryOperator<double[][]> subtract) {
    return subtract.apply(systems.stream().map(s -> {
      RelativeTetrapolarSystem system = s.toRelative();
      double denominator = Apparent2Rho.newNormalizedApparent2Rho(system).applyAsDouble(layers);
      return new double[] {
          Apparent2Rho.newDerivativeApparentByK2Rho(system).applyAsDouble(layers) / denominator,
          Apparent2Rho.newDerivativeApparentByPhi2Rho(system).applyAsDouble(layers) / denominator
      };
    }).toArray(double[][]::new));
  }
}
