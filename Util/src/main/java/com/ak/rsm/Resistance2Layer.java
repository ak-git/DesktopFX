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
import com.ak.util.Metrics;
import com.ak.util.Strings;
import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.analysis.TrivariateFunction;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleBounds;

import static java.lang.StrictMath.log;

/**
 * Calculates <b>full</b> resistance R<sub>m-n</sub> (in Ohm) between electrodes for <b>2-layer</b> model.
 */
final class Resistance2Layer extends AbstractResistanceLayer<Potential2Layer> implements TrivariateFunction {
  Resistance2Layer(@Nonnull TetrapolarSystem electrodeSystem) {
    super(electrodeSystem, Potential2Layer::new);
  }

  @Override
  public double value(@Nonnegative double rho1, @Nonnegative double rho2, @Nonnegative double h) {
    return applyAsDouble(u -> u.value(rho1, rho2, h));
  }

  public static class Medium extends AbstractMedium {
    private final double rho1;
    private final double rho2;
    private final double h;

    private Medium(@Nonnegative double rho1, @Nonnegative double rho2, @Nonnegative double h) {
      this.rho1 = rho1;
      this.rho2 = rho2;
      this.h = h;
    }

    public double getRho2() {
      return rho2;
    }

    @Override
    public String toString() {
      return String.format("%s; %s; h = %.2f mm", Strings.rho1(rho1), Strings.rho2(rho2), Metrics.toMilli(h));
    }

    @Override
    public String toString(@Nonnull TetrapolarSystem[] systems, @Nonnull double[] rOhms) {
      return toString(systems, rOhms, s -> new Resistance2Layer(s).value(rho1, rho2, h));
    }

    @Nonnull
    public static Medium inverse(@Nonnull TetrapolarSystem[] systems, @Nonnull double[] rOhms) {
      Resistance1Layer.Medium inverse = Resistance1Layer.Medium.inverse(systems, rOhms);
      Logger.getAnonymousLogger().log(Level.INFO, Resistance1Layer.Medium.inverse(systems, rOhms).toString(systems, rOhms));

      double rho = inverse.getRho();
      double maxL = Arrays.stream(systems).mapToDouble(s -> s.Lh(1.0)).max().orElseThrow();
      double[] measured = IntStream.range(0, systems.length).mapToDouble(i -> new Resistance1Layer(systems[i]).getApparent(rOhms[i])).toArray();

      PointValuePair pointValuePair = Simplex.optimizeCMAES(point -> {
            double rho1 = point[0];
            double rho2 = point[1];
            double h = point[2];

            double[] predicted = Arrays.stream(systems).mapToDouble(s -> new Resistance1Layer(s).getApparent(new Resistance2Layer(s).value(rho1, rho2, h))).toArray();
            return Inequality.absolute().applyAsDouble(measured, predicted);
          }, new SimpleBounds(new double[] {0.0, 0.0, 0.0}, new double[] {Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, maxL}),
          new double[] {rho, rho, 0}, new double[] {rho / 10.0, rho / 10.0, maxL / 10.0});

      double[] p = pointValuePair.getPoint();
      return new Medium(p[0], p[1], p[2]);
    }

    @Nonnull
    public static Medium inverse(@Nonnull TetrapolarSystem[] systems, @Nonnull double[] rOhmsBefore, @Nonnull double[] rOhmsAfter, double dh) {
      double rho = Resistance1Layer.Medium.inverse(systems, rOhmsBefore).getRho();
      Logger.getAnonymousLogger().log(Level.INFO, Resistance1Layer.Medium.inverse(systems, rOhmsBefore).toString(systems, rOhmsBefore));

      DoubleBinaryOperator subtract = (left, right) -> left - right;
      double subLogApparent = IntStream.range(0, systems.length)
          .mapToDouble(i -> log(new Resistance1Layer(systems[i]).getApparent(rOhmsBefore[i]))).reduce(subtract).orElseThrow();
      double subLogDiff = IntStream.range(0, systems.length)
          .mapToDouble(i -> log(Math.abs((rOhmsAfter[i] - rOhmsBefore[i]) / dh))).reduce(subtract).orElseThrow();

      if (Double.isNaN(subLogDiff)) {
        return new Medium(rho, rho, 0.0);
      }

      DoubleUnaryOperator findK = Lh -> {
        MultivariateFunction multivariateFunction = p -> {
          double k = p[0];
          double subLogApparentPredicted = Arrays.stream(systems)
              .mapToDouble(system -> new Log1pApparent2Rho(system.sToL()).value(k, Lh)).reduce(subtract).orElseThrow();
          return Inequality.absolute().applyAsDouble(subLogApparent, subLogApparentPredicted);
        };
        return Simplex.optimize(multivariateFunction, new SimpleBounds(new double[] {-1.0}, new double[] {1.0}), new double[] {0.0}, new double[] {0.1}).getPoint()[0];
      };

      PointValuePair findLh = Simplex.optimize(point -> {
            double Lh = point[0];
            double k = findK.applyAsDouble(Lh);
            double subLogDiffPredicted = Arrays.stream(systems)
                .mapToDouble(system -> new LogDerivativeApparent2Rho(system.sToL()).value(k, Lh)).reduce(subtract).orElseThrow();
            return Inequality.absolute().applyAsDouble(subLogDiff, subLogDiffPredicted);
          },
          new SimpleBounds(new double[] {0.0}, new double[] {Double.POSITIVE_INFINITY}),
          new double[] {1.0}, new double[] {0.1}
      );

      double Lh = findLh.getPoint()[0];
      double k = findK.applyAsDouble(Lh);

      double sumLogApparent = IntStream.range(0, systems.length)
          .mapToDouble(i -> log(new Resistance1Layer(systems[i]).getApparent(rOhmsBefore[i]))).reduce(Double::sum).orElseThrow();
      double sumLogApparentPredicted = Arrays.stream(systems)
          .mapToDouble(system -> new Log1pApparent2Rho(system.sToL()).value(k, Lh)).reduce(Double::sum).orElseThrow();
      double rho1 = StrictMath.exp((sumLogApparent - sumLogApparentPredicted) / systems.length);
      double rho2 = rho1 / Layers.getRho1ToRho2(k);
      double h = systems[0].h(Lh);
      return new Medium(rho1, rho2, h);
    }
  }
}
