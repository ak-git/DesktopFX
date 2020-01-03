package com.ak.rsm;

import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.DoubleFunction;
import java.util.function.IntToDoubleFunction;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.inverse.Inequality;
import com.ak.math.Simplex;
import org.apache.commons.math3.analysis.TrivariateFunction;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleBounds;

import static java.lang.StrictMath.exp;
import static java.lang.StrictMath.log;
import static java.lang.StrictMath.pow;

/**
 * Calculates <b>full</b> resistance R<sub>m-n</sub> (in Ohm) between electrodes for <b>2-layer</b> model.
 */
final class Resistance2Layer extends AbstractResistanceLayer<Potential2Layer> implements TrivariateFunction {
  @Nonnull
  private final Resistance1Layer resistance1Layer;

  Resistance2Layer(@Nonnull TetrapolarSystem electrodeSystem) {
    super(electrodeSystem, Potential2Layer::new);
    resistance1Layer = new Resistance1Layer(electrodeSystem);
  }

  public double value(@Nonnull double[] rho1rho2h) {
    if (rho1rho2h.length != 3) {
      throw new IllegalArgumentException(Arrays.toString(rho1rho2h));
    }
    return value(rho1rho2h[0], rho1rho2h[1], rho1rho2h[2]);
  }

  @Override
  public double value(@Nonnegative double rho1, @Nonnegative double rho2, @Nonnegative double h) {
    double result = resistance1Layer.value(rho1);
    if (Double.compare(rho1, rho2) != 0.0) {
      double k = Layers.getK12(rho1, rho2);
      result += (2.0 * rho1 / Math.PI) * Layers.sum(n -> pow(k, n) * apply(r -> r.value(n, h)));
    }
    return result;
  }

  @Nonnull
  public static Medium inverseStatic(@Nonnull TetrapolarSystem[] systems, @Nonnull double[] rOhms) {
    Medium inverse = Resistance1Layer.inverseStatic(systems, rOhms);
    if (systems.length > 2) {
      Logger.getLogger(Resistance2Layer.class.getName()).log(Level.INFO, inverse::toString);
      double rho = inverse.getRho();
      double maxL = Arrays.stream(systems).mapToDouble(s -> s.lToH(1.0)).max().orElseThrow();
      double[] measured = IntStream.range(0, systems.length).mapToDouble(i -> new Resistance1Layer(systems[i]).getApparent(rOhms[i])).toArray();

      PointValuePair pointValuePair = Simplex.optimizeCMAES(rho1rho2h -> {
            double[] predicted = Arrays.stream(systems).mapToDouble(s -> new Resistance1Layer(s).getApparent(new Resistance2Layer(s).value(rho1rho2h))).toArray();
            return Inequality.absolute().applyAsDouble(measured, predicted);
          }, new SimpleBounds(new double[] {0.0, 0.0, 0.0}, new double[] {Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, maxL}),
          new double[] {rho, rho, 0}, new double[] {rho / 10.0, rho / 10.0, maxL / 10.0});

      double[] p = pointValuePair.getPoint();
      double rho1 = p[0];
      double rho2 = p[1];
      double h1 = p[2];
      return new Medium.Builder(systems, rOhms, s -> new Resistance2Layer(s).value(p)).addLayer(rho1, h1).build(rho2);
    }
    else {
      return inverse;
    }
  }

  @Nonnull
  public static Medium inverseDynamic(@Nonnull TetrapolarSystem[] systems, @Nonnull double[] rOhmsBefore, @Nonnull double[] rOhmsAfter, double dh) {
    IntToDoubleFunction rDiff = index -> (rOhmsAfter[index] - rOhmsBefore[index]) / dh;

    DoubleFunction<IntToDoubleFunction> apparentDiffByH = h -> index -> {
      double apparent = new Resistance1Layer(systems[index]).getApparent(rDiff.applyAsDouble(index));
      return log(Math.abs(apparent) * h);
    };

    Medium inverse = inverseStatic(systems, rOhmsBefore);
    if (Arrays.stream(rangeSystems(systems.length, index -> apparentDiffByH.apply(1.0).applyAsDouble(index))).anyMatch(Double::isInfinite)) {
      return inverse;
    }
    else {
      Logger.getLogger(Resistance2Layer.class.getName()).log(Level.INFO, inverse::toString);
    }

    IntToDoubleFunction logApparentFunction = index -> log(new Resistance1Layer(systems[index]).getApparent(rOhmsBefore[index]));
    double[] logApparent = rangeSystems(systems.length, logApparentFunction);

    BiFunction<Double, Double, IntToDoubleFunction> logApparentPredictedFunction = (k, h) ->
        index -> new Log1pApparent2Rho(systems[index]).value(k, h);

    double maxL = Arrays.stream(systems).mapToDouble(s -> s.lToH(1.0)).max().orElseThrow();
    PointValuePair find = Simplex.optimizeCMAES(p -> {
          double h = p[0];
          double k = p[1];

          double[] logDiff = rangeSystems(systems.length, apparentDiffByH.apply(h));
          double[] measured = rangeSystems(systems.length, i -> logApparent[i] - logDiff[i]);

          double[] logApparentPredicted = rangeSystems(systems.length, index -> logApparentPredictedFunction.apply(k, h).applyAsDouble(index));
          double[] logDiffPredicted = rangeSystems(systems.length, index -> {
            TrivariateFunction resistance = new Resistance2Layer(systems[index]);
            double a = resistance.value(1.0, 1.0 / Layers.getRho1ToRho2(k), h + dh) -
                resistance.value(1.0, 1.0 / Layers.getRho1ToRho2(k), h);
            double apparent = new Resistance1Layer(systems[index]).getApparent(a / dh);
            if (Double.compare(Math.signum(apparent), Math.signum(rDiff.applyAsDouble(index))) == 0) {
              return log(Math.abs(apparent) * h);
            }
            else {
              return Double.POSITIVE_INFINITY;
            }
          });
          double[] predicted = rangeSystems(systems.length, i -> logApparentPredicted[i] - logDiffPredicted[i]);
          return Inequality.absolute().applyAsDouble(measured, predicted);
        },
        new SimpleBounds(new double[] {0.0, -1.0}, new double[] {maxL, 1.0}), new double[] {maxL / 2.0, 0.0}, new double[] {maxL / 10.0, 0.1}
    );

    double h = find.getPoint()[0];
    double k = find.getPoint()[1];

    double sumLogApparent = sumLog(systems, logApparentFunction);
    double sumLogApparentPredicted = sumLog(systems, index -> logApparentPredictedFunction.apply(k, h).applyAsDouble(index));
    double rho1 = exp((sumLogApparent - sumLogApparentPredicted) / systems.length);
    double rho2 = rho1 / Layers.getRho1ToRho2(k);
    return new Medium.Builder(systems, rOhmsBefore, rOhmsAfter, dh,
        (s, dH) -> new Resistance2Layer(s).value(rho1, rho2, h + dH))
        .addLayer(rho1, h).build(rho2);
  }

  static double sumLog(@Nonnull TetrapolarSystem[] systems, @Nonnull IntToDoubleFunction logApparentPredictedFunction) {
    return IntStream.range(0, systems.length).mapToDouble(logApparentPredictedFunction).reduce(Double::sum).orElseThrow();
  }

  static double[] rangeSystems(@Nonnegative int length, @Nonnull IntToDoubleFunction mapper) {
    return IntStream.range(0, length).mapToDouble(mapper).toArray();
  }
}
