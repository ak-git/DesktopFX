package com.ak.rsm.inverse;

import com.ak.rsm.apparent.Apparent2Rho;
import com.ak.rsm.relative.RelativeMediumLayers;
import com.ak.rsm.resistance.DerivativeResistance;
import com.ak.rsm.resistance.TetrapolarDerivativeResistance;
import com.ak.rsm.resistance.TetrapolarResistance;
import com.ak.rsm.system.InexactTetrapolarSystem;
import com.ak.rsm.system.Layers;
import com.ak.rsm.system.TetrapolarSystem;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static java.lang.StrictMath.log;

final class DynamicErrors extends AbstractErrors {
  DynamicErrors(@Nonnull Collection<InexactTetrapolarSystem> inexactSystems) {
    super(inexactSystems);
  }

  @Nonnull
  @Override
  public RelativeMediumLayers apply(@Nonnull RelativeMediumLayers layers) {
    double[][] a2 = systems().stream().map(TetrapolarSystem::relativeSystem)
        .map(s -> {
          double denominator2 = Apparent2Rho.newDerivativeApparentByPhiDivRho1(s).applyAsDouble(layers);
          return new double[] {
              Apparent2Rho.newSecondDerivativeApparentByPhiKDivRho1(s).applyAsDouble(layers) / denominator2,
              Apparent2Rho.newSecondDerivativeApparentByPhiPhiDivRho1(s).applyAsDouble(layers) / denominator2
          };
        })
        .toArray(double[][]::new);

    double rho1 = 1.0;
    double rho2 = rho1 / Layers.getRho1ToRho2(layers.k12());
    double h = layers.hToL() * baseL();
    double dh = h * 1.0e-4;

    Function<Collection<TetrapolarSystem>, List<DerivativeResistance>> toMeasurements =
        ts -> {
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
    return new StaticErrors(inexactSystems()).errors(layers, UnaryOperator.identity(),
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
