package com.ak.rsm;

import java.util.Arrays;
import java.util.function.DoubleBinaryOperator;
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

  public static class Medium {
    private final double rho1;
    private final double rho2;
    private final double h;

    private Medium(@Nonnegative double rho1, @Nonnegative double rho2, @Nonnegative double h) {
      this.rho1 = rho1;
      this.rho2 = rho2;
      this.h = h;
    }

    @Override
    public String toString() {
      return String.format("%s = %.3f %s; %s = %.3f %s; h = %.2f mm",
          Strings.RHO_1, rho1, Strings.OHM_METRE,
          Strings.RHO_2, rho2, Strings.OHM_METRE,
          Metrics.toMilli(h));
    }

    public static Medium inverse(@Nonnull TetrapolarSystem[] systems, @Nonnull double[] rOhmsBefore, @Nonnull double[] rOhmsAfter, double dh) {
      DoubleBinaryOperator subtract = (left, right) -> left - right;
      double subLogApparent = IntStream.range(0, systems.length)
          .mapToDouble(i -> log(new Resistance1Layer(systems[i]).getApparent(rOhmsBefore[i]))).reduce(subtract).orElseThrow();
      double subLogDiff = IntStream.range(0, systems.length)
          .mapToDouble(i -> log(Math.abs((rOhmsAfter[i] - rOhmsBefore[i]) / dh))).reduce(subtract).orElseThrow();

      MultivariateFunction multivariateFunction = p -> {
        double k = p[0];
        double Lh = p[1];
        if (k > 1.0 || k < -1.0 || Lh <= 0) {
          return Double.POSITIVE_INFINITY;
        }
        else {
          double subLogApparentPredicted = Arrays.stream(systems)
              .mapToDouble(system -> new Log1pApparent2Rho(system.sToL()).value(k, Lh)).reduce(subtract).orElseThrow();
          double subLogDiffPredicted = Arrays.stream(systems)
              .mapToDouble(system -> new LogDerivativeApparent2Rho(system.sToL()).value(k, Lh)).reduce(subtract).orElseThrow();
          Inequality inequality = Inequality.absolute();
          inequality.applyAsDouble(subLogApparent, subLogApparentPredicted);
          inequality.applyAsDouble(subLogDiff, subLogDiffPredicted);
          return inequality.getAsDouble();
        }
      };

      PointValuePair p = Simplex.optimizeNelderMead(multivariateFunction, new double[] {0.0, 1.0}, new double[] {0.1, 0.1});
      double k = p.getPoint()[0];
      double Lh = p.getPoint()[1];

      double sumLogApparent = IntStream.range(0, systems.length)
          .mapToDouble(i -> log(new Resistance1Layer(systems[i]).getApparent(rOhmsBefore[i]))).reduce(Double::sum).orElseThrow();
      double sumLogApparentPredicted = Arrays.stream(systems)
          .mapToDouble(system -> new Log1pApparent2Rho(system.sToL()).value(k, Lh)).reduce(Double::sum).orElseThrow();
      double rho1 = StrictMath.exp((sumLogApparent - sumLogApparentPredicted) / 2.0);
      double rho2 = rho1 / Layers.getRho1ToRho2(k);
      double h = systems[0].h(Lh);
      return new Medium(rho1, rho2, h);
    }
  }
}
