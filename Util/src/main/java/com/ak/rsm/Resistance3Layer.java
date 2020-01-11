package com.ak.rsm;

import java.util.Arrays;
import java.util.Comparator;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.IntToDoubleFunction;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.inverse.Inequality;
import com.ak.math.Simplex;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleBounds;
import org.apache.commons.math3.util.Pair;

import static com.ak.rsm.Resistance2Layer.rangeSystems;
import static java.lang.StrictMath.log;

/**
 * Calculates <b>full</b> resistance R<sub>m-n</sub> (in Ohm) between electrodes for <b>3-layer</b> model.
 */
final class Resistance3Layer extends AbstractResistanceLayer<Potential3Layer> {
  @Nonnull
  private final Resistance1Layer resistance1Layer;
  @Nonnull
  private final Resistance2Layer resistance2Layer;
  @Nonnegative
  private final double hStep;

  Resistance3Layer(@Nonnull TetrapolarSystem electrodeSystem, double hStep) {
    super(electrodeSystem, value -> new Potential3Layer(value, hStep));
    resistance1Layer = new Resistance1Layer(electrodeSystem);
    resistance2Layer = new Resistance2Layer(electrodeSystem);
    this.hStep = Math.abs(hStep);
  }

  /**
   * Calculates <b>full</b> resistance R<sub>m-n</sub> (in Ohm)
   *
   * @param rho1  specific resistance of <b>1st-layer</b> in Ohm-m
   * @param rho2  specific resistance of <b>2nd-layer</b> in Ohm-m
   * @param rho3  specific resistance of <b>3nd-layer</b> in Ohm-m
   * @param p1    height of <b>1-layer</b>
   * @param p2mp1 height of <b>2-layer</b>
   * @return resistance R<sub>m-n</sub> (in Ohm)
   */
  public double value(@Nonnegative double rho1, @Nonnegative double rho2, @Nonnegative double rho3, @Nonnegative int p1, @Nonnegative int p2mp1) {
    if (Double.compare(rho1, rho2) != 0 && Double.compare(rho2, rho3) != 0 && p1 > 0 && p2mp1 > 0) {
      double k12 = Layers.getK12(rho1, rho2);
      double k23 = Layers.getK12(rho2, rho3);
      double[] q = Layers.qn(k12, k23, p1, p2mp1);
      double r3 = resistance1Layer.value(rho1) + (2.0 * rho1 / Math.PI) * Layers.sum(n -> q[n] * apply(r -> r.value(n)));
      if (k12 < 0 && k23 > 0) {
        return Math.max(resistance2Layer.value(rho1, rho2, hStep * p1), r3);
      }
      else {
        return r3;
      }
    }
    else if (p1 < 1 && p2mp1 < 1) {
      return resistance1Layer.value(rho3);
    }
    else if (Double.compare(rho1, rho2) != 0 && p1 > 0) {
      return resistance2Layer.value(rho1, rho2, hStep * p1);
    }
    else if (Double.compare(rho2, rho3) != 0) {
      return resistance2Layer.value(rho2, rho3, hStep * (p2mp1 + p1));
    }
    else {
      return resistance1Layer.value(rho1);
    }
  }

  @Nonnull
  public static Medium inverseDynamic(@Nonnull TetrapolarSystem[] systems, @Nonnull double[] rOhmsBefore, @Nonnull double[] rOhmsAfter, double dh) {
    Medium inverse = Resistance2Layer.inverseDynamic(systems, rOhmsBefore, rOhmsAfter, dh);
    Logger.getAnonymousLogger().info(inverse::toString);

    IntToDoubleFunction rDiff = index -> rOhmsAfter[index] - rOhmsBefore[index];

    IntFunction<IntToDoubleFunction> apparentDiffByH = p ->
        index -> log(Math.abs(new Resistance1Layer(systems[index]).getApparent(rDiff.applyAsDouble(index))) * p);

    if (Arrays.stream(rangeSystems(systems.length, index -> apparentDiffByH.apply(1).applyAsDouble(index))).anyMatch(Double::isInfinite)) {
      double rho = Resistance1Layer.inverseStatic(systems, rOhmsBefore).getRho();
      return new Medium.Builder(systems, rOhmsBefore, s -> new Resistance3Layer(s, dh).value(rho, rho, rho, 0, 0))
          .addLayer(rho, 0).addLayer(rho, 0).build(rho);
    }

    IntToDoubleFunction logApparentFunction = index -> log(new Resistance1Layer(systems[index]).getApparent(rOhmsBefore[index]));
    double[] logApparent = rangeSystems(systems.length, logApparentFunction);

    BiFunction<double[], int[], IntToDoubleFunction> logApparentPredictedFunction = (k, p) ->
        index -> new Log1pApparent3Rho(systems[index]).value(k[0], k[1], dh, p[0], p[1]);

    Function<int[], PointValuePair> findK = p -> {
      int p1 = p[0];
      int p2mp1 = p[1];
      return Simplex.optimize("", x -> {
            double k12 = x[0];
            double k23 = x[1];

            double[] logDiff = rangeSystems(systems.length, apparentDiffByH.apply(p1 + p2mp1));
            double[] measured = rangeSystems(systems.length, i -> logApparent[i] - logDiff[i]);

            double[] logApparentPredicted = rangeSystems(systems.length,
                index -> logApparentPredictedFunction.apply(new double[] {k12, k23}, new int[] {p1, p2mp1}).applyAsDouble(index)
            );
            double[] logDiffPredicted = rangeSystems(systems.length, index -> {
              Resistance3Layer resistance = new Resistance3Layer(systems[index], dh);
              double rho1 = 1.0;
              double rho2 = rho1 / Layers.getRho1ToRho2(k12);
              double rho3 = rho2 / Layers.getRho1ToRho2(k23);
              double a = resistance.value(rho1, rho2, rho3, p1 - 1, p2mp1) -
                  resistance.value(rho1, rho2, rho3, p1, p2mp1);
              a *= (p1 + p2mp1);
              double apparent = new Resistance1Layer(systems[index]).getApparent(a);
              if (Double.compare(Math.signum(apparent), Math.signum(rDiff.applyAsDouble(index))) == 0) {
                return log(Math.abs(apparent));
              }
              else {
                return Double.POSITIVE_INFINITY;
              }
            });
            double[] predicted = rangeSystems(systems.length, i -> logApparentPredicted[i] - logDiffPredicted[i]);
            return Inequality.absolute().applyAsDouble(measured, predicted);
          },
          new SimpleBounds(new double[] {-1.0, -1.0}, new double[] {1.0, 1.0}),
          new double[] {0.0, 0.0}, new double[] {1, 1}
      );
    };

    PointValuePair f = IntStream.range(2, 100)
        .mapToObj(p1 -> {
          PointValuePair minForP1 = IntStream.range(1, p1)
              .mapToObj(p2mp1 -> new PointValuePair(new double[] {p1, p2mp1}, findK.apply(new int[] {p1, p2mp1}).getValue()))
              .min(Comparator.comparingDouble(Pair::getValue)).orElseThrow();
          Logger.getAnonymousLogger().info(() -> String.format("%s %.6f", Arrays.toString(minForP1.getPoint()), minForP1.getValue()));
          return minForP1;
        })
        .min(Comparator.comparingDouble(Pair::getValue)).orElseThrow();

    Logger.getAnonymousLogger().info(() -> String.format("%s %.6f", Arrays.toString(f.getPoint()), f.getValue()));

    int[] p = Arrays.stream(f.getPoint()).mapToInt(value -> (int) Math.round(value)).toArray();
    double[] k = findK.apply(p).getPoint();

    double sumLogApparent = Resistance2Layer.sumLog(systems, logApparentFunction);
    double sumLogApparentPredicted = Resistance2Layer.sumLog(systems,
        index -> logApparentPredictedFunction.apply(k, p).applyAsDouble(index)
    );
    double rho1 = StrictMath.exp((sumLogApparent - sumLogApparentPredicted) / systems.length);
    double rho2 = rho1 / Layers.getRho1ToRho2(k[0]);
    double rho3 = rho2 / Layers.getRho1ToRho2(k[1]);

    return new Medium.Builder(systems, rOhmsBefore, rOhmsAfter, dh,
        (s, deltaH) -> new Resistance3Layer(s, dh).value(rho1, rho2, rho3, p[0] + (int) Math.signum(deltaH), p[1]))
        .addLayer(rho1, p[0] * Math.abs(dh))
        .addLayer(rho2, p[1] * Math.abs(dh)).build(rho3);
  }
}
