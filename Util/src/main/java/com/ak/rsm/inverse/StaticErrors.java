package com.ak.rsm.inverse;

import com.ak.math.ValuePair;
import com.ak.rsm.apparent.Apparent2Rho;
import com.ak.rsm.relative.Layer2RelativeMedium;
import com.ak.rsm.relative.RelativeMediumLayers;
import com.ak.rsm.resistance.Resistance;
import com.ak.rsm.resistance.TetrapolarResistance;
import com.ak.rsm.system.InexactTetrapolarSystem;
import com.ak.rsm.system.Layers;
import com.ak.rsm.system.RelativeTetrapolarSystem;
import com.ak.rsm.system.TetrapolarSystem;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.SingularValueDecomposition;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static java.lang.StrictMath.log;

final class StaticErrors extends AbstractErrors implements UnaryOperator<RelativeMediumLayers> {
  static final UnaryOperator<double[]> SUBTRACT = values -> {
    var sub = new double[values.length - 1];
    for (var i = 0; i < sub.length; i++) {
      sub[i] = values[i + 1] - values[i];
    }
    return sub;
  };

  private static final BinaryOperator<Layer2RelativeMedium> MAX_ERROR = (v1, v2) -> {
    double kEMax = Math.max(v1.k12AbsError(), v2.k12AbsError());
    double hToLEMax = Math.max(v1.hToLAbsError(), v2.hToLAbsError());
    return new Layer2RelativeMedium(ValuePair.Name.K12.of(v1.k12(), kEMax), ValuePair.Name.H_L.of(v1.hToL(), hToLEMax));
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

  StaticErrors(@Nonnull Collection<InexactTetrapolarSystem> inexactSystems) {
    super(inexactSystems);
  }

  @Nonnull
  @Override
  public RelativeMediumLayers apply(@Nonnull RelativeMediumLayers layers) {
    return errors(layers, UnaryOperator.identity(), UnaryOperator.identity(), (ts, b) -> b);
  }

  @Nonnull
  @ParametersAreNonnullByDefault
  RelativeMediumLayers errors(RelativeMediumLayers layers,
                              UnaryOperator<double[]> subtract, UnaryOperator<double[][]> fixA,
                              BiFunction<Collection<TetrapolarSystem>, double[], double[]> fixB) {
    double[][] a = getAMatrix(
        systems().stream().map(TetrapolarSystem::relativeSystem).toList(),
        layers, subtract.equals(SUBTRACT) ? SUBTRACT_MATRIX : fixA
    );

    double rho1 = 1.0;
    double rho2 = rho1 / Layers.getRho1ToRho2(layers.k12());
    double h = layers.hToL() * baseL();
    Function<Collection<TetrapolarSystem>, List<Resistance>> toMeasurements =
        ts -> {
          Iterator<TetrapolarSystem> iterator = systems().iterator();
          return ts.stream().map(
              s -> {
                double ohms = TetrapolarResistance.of(s).rho1(rho1).rho2(rho2).h(h).ohms();
                return TetrapolarResistance.of(iterator.next()).ofOhms(ohms);
              }
          ).toList();
        };

    var baseM = toMeasurements.apply(systems());
    return InexactTetrapolarSystem.getMeasurementsCombination(inexactSystems()).stream()
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
          return new Layer2RelativeMedium(
              ValuePair.Name.K12.of(layers.k12(), kwErrors[0]),
              ValuePair.Name.H_L.of(layers.hToL(), kwErrors[1])
          );
        })
        .reduce(MAX_ERROR).orElseThrow();
  }

  @Nonnull
  @ParametersAreNonnullByDefault
  private static double[][] getAMatrix(Collection<RelativeTetrapolarSystem> systems, RelativeMediumLayers layers,
                                       UnaryOperator<double[][]> subtract) {
    return subtract.apply(
        systems.stream()
            .map(s -> {
              double denominator = Apparent2Rho.newApparentDivRho1(s).applyAsDouble(layers);
              return new double[] {
                  Apparent2Rho.newDerApparentByKDivRho1(s).applyAsDouble(layers) / denominator,
                  Apparent2Rho.newDerApparentByPhiDivRho1(s).applyAsDouble(layers) / denominator
              };
            })
            .toArray(double[][]::new)
    );
  }
}
