package com.ak.rsm;

import java.util.Arrays;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.inverse.Inequality;
import com.ak.math.Simplex;
import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.analysis.TrivariateFunction;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleBounds;

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
  public static Medium inverse(@Nonnull TetrapolarSystem[] systems, @Nonnull double[] rOhms) {
    Medium inverse = Resistance1Layer.inverse(systems, rOhms);
    Logger.getLogger(Resistance2Layer.class.getName()).log(Level.INFO, inverse.toString());

    double rho = inverse.getRho();
    double maxL = Arrays.stream(systems).mapToDouble(s -> s.Lh(1.0)).max().orElseThrow();
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

  @Nonnull
  public static Medium inverse(@Nonnull TetrapolarSystem[] systems, @Nonnull double[] rOhmsBefore, @Nonnull double[] rOhmsAfter, double dh) {
    if (!Arrays.stream(systems).allMatch(system -> Double.compare(system.Lh(1), systems[0].Lh(1)) == 0)) {
      throw new IllegalArgumentException(Arrays.toString(systems));
    }
    Medium inverse = Resistance1Layer.inverse(systems, rOhmsBefore);
    double rho = inverse.getRho();
    Logger.getLogger(Resistance2Layer.class.getName()).log(Level.INFO, inverse.toString());

    DoubleBinaryOperator subtract = (left, right) -> left - right;
    double subLogApparent = IntStream.range(0, systems.length)
        .mapToDouble(i -> log(new Resistance1Layer(systems[i]).getApparent(rOhmsBefore[i]))).reduce(subtract).orElseThrow();
    double subLogDiff = IntStream.range(0, systems.length)
        .mapToDouble(i -> {
          double a = (rOhmsAfter[i] - rOhmsBefore[i]) / Math.abs(dh);
          return log(Math.abs(a)) * Math.signum(a);
        }).reduce(subtract).orElseThrow();

    if (Double.isNaN(subLogDiff)) {
      return new Medium.Builder(systems, rOhmsBefore, s -> new Resistance2Layer(s).value(rho, rho, 0)).addLayer(rho, 0).build(rho);
    }

    DoubleUnaryOperator findK = h -> {
      MultivariateFunction multivariateFunction = p -> {
        double k = p[0];
        double subLogApparentPredicted = Arrays.stream(systems)
            .mapToDouble(system -> new Log1pApparent2Rho(system).value(k, h)).reduce(subtract).orElseThrow();
        return Inequality.absolute().applyAsDouble(subLogApparent, subLogApparentPredicted);
      };
      return Simplex.optimize(multivariateFunction, new SimpleBounds(new double[] {-1.0}, new double[] {1.0}), new double[] {0.0}, new double[] {0.1}).getPoint()[0];
    };

    double maxL = Arrays.stream(systems).mapToDouble(s -> s.Lh(1.0)).max().orElseThrow();
    PointValuePair findLh = Simplex.optimize(point -> {
          double h = point[0];
          double k = findK.applyAsDouble(h);
          double subLogDiffPredicted = Arrays.stream(systems)
              .mapToDouble(system -> new LogDerivativeApparent2Rho(system).value(k, h)).reduce(subtract).orElseThrow();
          return Inequality.absolute().applyAsDouble(subLogDiff, subLogDiffPredicted);
        },
        new SimpleBounds(new double[] {0.0}, new double[] {maxL}),
        new double[] {maxL / 2.0}, new double[] {maxL / 10.0}
    );

    double h = findLh.getPoint()[0];
    double k = findK.applyAsDouble(h);

    double sumLogApparent = IntStream.range(0, systems.length)
        .mapToDouble(i -> log(new Resistance1Layer(systems[i]).getApparent(rOhmsBefore[i]))).reduce(Double::sum).orElseThrow();
    double sumLogApparentPredicted = Arrays.stream(systems)
        .mapToDouble(system -> new Log1pApparent2Rho(system).value(k, h)).reduce(Double::sum).orElseThrow();
    double rho1 = StrictMath.exp((sumLogApparent - sumLogApparentPredicted) / systems.length);
    double rho2 = rho1 / Layers.getRho1ToRho2(k);
    return new Medium.Builder(systems, rOhmsBefore, rOhmsAfter, dh, (s, dH) -> new Resistance2Layer(s).value(rho1, rho2, h + dH)).addLayer(rho1, h).build(rho2);
  }
}
