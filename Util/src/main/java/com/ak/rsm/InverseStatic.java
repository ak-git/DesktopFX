package com.ak.rsm;

import java.util.Collection;
import java.util.List;
import java.util.function.DoubleBinaryOperator;
import java.util.function.UnaryOperator;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.inverse.Inequality;
import com.ak.math.Simplex;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleBounds;

import static com.ak.rsm.Measurements.getAMatrix;
import static com.ak.rsm.Measurements.getLayer2RelativeMedium;
import static com.ak.rsm.Measurements.getMaxHToL;
import static com.ak.rsm.Measurements.logApparentPredicted;
import static java.lang.StrictMath.log;

enum InverseStatic implements Inverseable<Measurement> {
  INSTANCE;

  private static final UnaryOperator<double[]> SUBTRACT = newSubtract((left, right) -> left - right);
  private static final UnaryOperator<double[]> PLUS_ERRORS = newSubtract((left, right) -> Math.abs(left) + Math.abs(right));

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
  public RelativeMediumLayers errors(List<TetrapolarSystem> systems, RelativeMediumLayers layers) {
    return errors(systems, layers, UnaryOperator.identity());
  }

  @Nonnull
  @ParametersAreNonnullByDefault
  private static RelativeMediumLayers errors(Collection<TetrapolarSystem> systems, RelativeMediumLayers layers,
                                             UnaryOperator<double[]> subtract) {
    var plusErrors = subtract.equals(SUBTRACT) ? PLUS_ERRORS : subtract;
    double[] logRhoAbsErrors = plusErrors.apply(systems.stream().mapToDouble(TetrapolarSystem::getApparentRelativeError).toArray());
    RealMatrix a = getAMatrix(systems, layers, subtract);
    return getLayer2RelativeMedium(layers, a, logRhoAbsErrors);
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
}
