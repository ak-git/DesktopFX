package com.ak.rsm;

import java.util.Collection;
import java.util.List;
import java.util.function.UnaryOperator;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.inverse.Inequality;
import com.ak.math.Simplex;
import com.ak.math.ValuePair;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleBounds;

import static com.ak.rsm.Measurements.getMaxHToL;
import static com.ak.rsm.Measurements.logApparentPredicted;
import static com.ak.rsm.Measurements.logDiffApparentPredicted;
import static com.ak.rsm.RelativeMediumLayers.NAN;
import static java.lang.StrictMath.abs;
import static java.lang.StrictMath.log;

enum InverseDynamic implements Inverseable<DerivativeMeasurement> {
  INSTANCE {
    @Override
    @Nonnull
    @ParametersAreNonnullByDefault
    public RelativeMediumLayers errors(Collection<TetrapolarSystem> systems, RelativeMediumLayers layers) {
      double[][] a2 = systems.stream().map(s -> {
        RelativeTetrapolarSystem system = s.toRelative();
        double denominator2 = Apparent2Rho.newDerivativeApparentByPhi2Rho(system).applyAsDouble(layers);
        return new double[] {
            -Apparent2Rho.newSecondDerivativeApparentByPhiK2Rho(system).applyAsDouble(layers) / denominator2,
            -Apparent2Rho.newSecondDerivativeApparentByPhiPhi2Rho(system).applyAsDouble(layers) / denominator2
        };
      }).toArray(double[][]::new);

      double[] logRhoAbsErrors2 = systems.stream().mapToDouble(TetrapolarSystem::getDiffApparentRelativeError).toArray();

      return InverseStatic.errors(systems, layers, UnaryOperator.identity(),
          a -> new Array2DRowRealMatrix(a).add(new Array2DRowRealMatrix(a2)).getData(),
          b -> new ArrayRealVector(b).subtract(new ArrayRealVector(logRhoAbsErrors2)).toArray()
      );
    }
  },
  HARD {
    @Override
    @Nonnull
    @ParametersAreNonnullByDefault
    public RelativeMediumLayers errors(Collection<TetrapolarSystem> systems, RelativeMediumLayers layers) {
      double rho1 = 1.0;
      double rho2 = rho1 / Layers.getRho1ToRho2(layers.k12());
      double dh = systems.stream().mapToDouble(TetrapolarSystem::getL).max().orElseThrow() * 0.001;

      double[] rOhmsBefore = systems.stream()
          .mapToDouble(s -> new Resistance2Layer(s).value(rho1, rho2, layers.hToL() * s.getL()))
          .toArray();
      double[] rOhmsAfter = systems.stream().mapToDouble(s -> {
        double dRho = Apparent2Rho.newDerivativeApparentByPhi2Rho(s.toRelative())
            .applyAsDouble(new Layer2RelativeMedium(layers.k12(), layers.hToL())) * rho1;
        return new Resistance2Layer(s).value(rho1, rho2,
            layers.hToL() * s.getL()) + new Resistance1Layer(s).value(dRho * (dh / s.getL())
        );
      }).toArray();

      return TetrapolarSystem.getMeasurementsCombination(systems).stream()
          .map(systemList -> INSTANCE.inverseRelative(
                  TetrapolarDerivativeMeasurement.of(systemList.toArray(TetrapolarSystem[]::new), rOhmsBefore, rOhmsAfter, dh)
              )
          )
          .map(relativeMediumLayers -> new Layer2RelativeMedium(
                  ValuePair.Name.K12.of(layers.k12(), relativeMediumLayers.k12() - layers.k12()),
                  ValuePair.Name.H_L.of(layers.hToL(), relativeMediumLayers.hToL() - layers.hToL())
              )
          )
          .reduce(InverseStatic.MAX_ERROR).orElseThrow();
    }
  };

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
}
