package com.ak.rsm;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.inverse.Inequality;
import com.ak.math.Simplex;
import com.ak.math.ValuePair;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleBounds;

import static com.ak.rsm.Measurements.PLUS_ERRORS;
import static com.ak.rsm.Measurements.SUBTRACT;
import static com.ak.rsm.Measurements.SUBTRACT_MATRIX;
import static com.ak.rsm.Measurements.getAMatrix;
import static com.ak.rsm.Measurements.getLayer2RelativeMedium;
import static com.ak.rsm.Measurements.getMaxHToL;
import static com.ak.rsm.Measurements.logApparentPredicted;
import static java.lang.StrictMath.log;

enum InverseStatic implements Inverseable<Measurement> {
  INSTANCE;

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

    List<TetrapolarSystem> tetrapolarSystems = measurements.stream().map(Measurement::getSystem).toList();
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

  @Override
  @Nonnull
  @ParametersAreNonnullByDefault
  public RelativeMediumLayers errors(Collection<TetrapolarSystem> systems, RelativeMediumLayers layers) {
    return errors(systems, layers, UnaryOperator.identity());
  }

  @Nonnull
  @ParametersAreNonnullByDefault
  private static RelativeMediumLayers errors(Collection<TetrapolarSystem> systems, RelativeMediumLayers layers,
                                             UnaryOperator<double[]> subtract) {
    UnaryOperator<double[]> plusErrors = subtract.equals(SUBTRACT) ? PLUS_ERRORS : UnaryOperator.identity();
    double[] logRhoAbsErrors = plusErrors.apply(systems.stream().mapToDouble(TetrapolarSystem::getApparentRelativeError).toArray());
    double[][] a = getAMatrix(systems, layers, subtract.equals(SUBTRACT) ? SUBTRACT_MATRIX : UnaryOperator.identity());

    return IntStream.range(0, 1 << (logRhoAbsErrors.length - 1))
        .mapToObj(n -> {
          var b = Arrays.copyOf(logRhoAbsErrors, logRhoAbsErrors.length);
          for (var i = 0; i < logRhoAbsErrors.length; i++) {
            if ((n & (1 << i)) == 0) {
              b[i] *= -1.0;
            }
          }
          return getLayer2RelativeMedium(layers, a, b);
        })
        .reduce((v1, v2) -> {
          double kEMax = Math.max(v1.k12AbsError(), v2.k12AbsError());
          double hToLEMax = Math.max(v1.hToLAbsError(), v2.hToLAbsError());
          return new Layer2RelativeMedium(ValuePair.Name.K12.of(layers.k12(), kEMax), ValuePair.Name.H_L.of(layers.hToL(), hToLEMax));
        })
        .orElseThrow();
  }
}
