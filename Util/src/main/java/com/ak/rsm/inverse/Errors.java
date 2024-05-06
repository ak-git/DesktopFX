package com.ak.rsm.inverse;

import com.ak.math.ValuePair;
import com.ak.rsm.apparent.Apparent2Rho;
import com.ak.rsm.relative.RelativeMediumLayers;
import com.ak.rsm.resistance.DerivativeResistance;
import com.ak.rsm.resistance.Resistance;
import com.ak.rsm.resistance.TetrapolarDerivativeResistance;
import com.ak.rsm.resistance.TetrapolarResistance;
import com.ak.rsm.system.InexactTetrapolarSystem;
import com.ak.rsm.system.Layers;
import com.ak.rsm.system.RelativeTetrapolarSystem;
import com.ak.rsm.system.TetrapolarSystem;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.SingularValueDecomposition;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.*;

import static java.lang.StrictMath.log;

public interface Errors extends UnaryOperator<RelativeMediumLayers> {
  Collection<InexactTetrapolarSystem> inexactSystems();

  default Collection<TetrapolarSystem> systems() {
    return inexactSystems().stream().map(InexactTetrapolarSystem::system).toList();
  }

  default Collection<RelativeTetrapolarSystem> relativeSystems() {
    return systems().stream().map(TetrapolarSystem::relativeSystem).toList();
  }

  enum Builder {
    STATIC {
      @Override
      public UnaryOperator<RelativeMediumLayers> of(Collection<InexactTetrapolarSystem> inexactSystems) {
        return new Static(inexactSystems);
      }
    },
    DYNAMIC {
      private record Dynamic(Collection<InexactTetrapolarSystem> inexactSystems) implements Errors {

        @Override
        public RelativeMediumLayers apply(RelativeMediumLayers layers) {
          double[][] a2 = systems().stream().map(TetrapolarSystem::relativeSystem)
              .map(s -> {
                double denominator2 = Apparent2Rho.newDerApparentByPhiDivRho1(s).applyAsDouble(layers);
                return new double[] {
                    Apparent2Rho.newSecondDerApparentByPhiKDivRho1(s).applyAsDouble(layers) / denominator2,
                    Apparent2Rho.newSecondDerApparentByPhiPhiDivRho1(s).applyAsDouble(layers) / denominator2
                };
              })
              .toArray(double[][]::new);

          double rho1 = 1.0;
          double rho2 = rho1 / Layers.getRho1ToRho2(layers.k().value());
          double h = layers.hToL().value() * TetrapolarSystem.getBaseL(systems());
          Function<Collection<TetrapolarSystem>, List<DerivativeResistance>> toMeasurements =
              ts -> {
                double dh = h * 1.0e-4;
                Iterator<TetrapolarSystem> iterator = systems().iterator();
                return ts.stream()
                    .map(
                        s -> {
                          double ohmsBefore = TetrapolarResistance.of(s).rho1(rho1).rho2(rho2).h(h).ohms();
                          double ohmsAfter = TetrapolarResistance.of(s).rho1(rho1).rho2(rho2).h(h + dh).ohms();
                          return TetrapolarDerivativeResistance.of(iterator.next()).dh(dh).ofOhms(ohmsBefore, ohmsAfter);
                        }
                    )
                    .toList();
              };

          DoubleUnaryOperator logAbs = x -> log(Math.abs(x));

          var baseM = toMeasurements.apply(systems());
          return new Static(inexactSystems).errors(layers,
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

      @Override
      public UnaryOperator<RelativeMediumLayers> of(Collection<InexactTetrapolarSystem> inexactSystems) {
        return new Dynamic(inexactSystems);
      }
    };

    private record Static(Collection<InexactTetrapolarSystem> inexactSystems) implements Errors {
      private static final BinaryOperator<RelativeMediumLayers> MAX_ERROR = (v1, v2) -> {
        double kEMax = Math.max(v1.k().absError(), v2.k().absError());
        double hToLEMax = Math.max(v1.hToL().absError(), v2.hToL().absError());
        return new RelativeMediumLayers(ValuePair.Name.K12.of(v1.k().value(), kEMax), ValuePair.Name.H_L.of(v1.hToL().value(), hToLEMax));
      };

      @Override
      public RelativeMediumLayers apply(RelativeMediumLayers relativeMediumLayers) {
        return errors(relativeMediumLayers, UnaryOperator.identity(), (ts, b) -> b);
      }

      RelativeMediumLayers errors(RelativeMediumLayers layers, UnaryOperator<double[][]> fixA,
                                  BiFunction<Collection<TetrapolarSystem>, double[], double[]> fixB) {
        double[][] a = getAMatrix(layers, fixA);

        double rho1 = 1.0;
        double rho2 = rho1 / Layers.getRho1ToRho2(layers.k().value());
        double h = layers.hToL().value() * TetrapolarSystem.getBaseL(systems());
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
            .map(b -> {
              DecompositionSolver solver = new SingularValueDecomposition(new Array2DRowRealMatrix(a)).getSolver();
              double[] kwErrors = solver.solve(new ArrayRealVector(b)).toArray();
              return new RelativeMediumLayers(
                  ValuePair.Name.K12.of(layers.k().value(), kwErrors[0]),
                  ValuePair.Name.H_L.of(layers.hToL().value(), kwErrors[1])
              );
            })
            .reduce(MAX_ERROR).orElseThrow();
      }

      private double[][] getAMatrix(RelativeMediumLayers layers, UnaryOperator<double[][]> subtract) {
        return subtract.apply(
            relativeSystems().stream()
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

    public abstract UnaryOperator<RelativeMediumLayers> of(Collection<InexactTetrapolarSystem> inexactSystems);
  }
}
