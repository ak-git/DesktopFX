package com.ak.rsm;

import java.util.Arrays;
import java.util.Comparator;
import java.util.function.BiFunction;
import java.util.function.IntFunction;
import java.util.function.IntToDoubleFunction;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToLongFunction;
import java.util.function.UnaryOperator;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

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
  @ParametersAreNonnullByDefault
  public static Medium inverseDynamic(TetrapolarSystem[] systems, double[] rOhmsBefore, double[] rOhmsAfter, double dH) {
    IntToDoubleFunction rDiff = index -> (rOhmsAfter[index] - rOhmsBefore[index]);

    Supplier<IntToDoubleFunction> apparentDiffByH = () ->
        index -> log(Math.abs(new Resistance1Layer(systems[index]).getApparent(rDiff.applyAsDouble(index))));

    if (Arrays.stream(rangeSystems(systems.length, index -> apparentDiffByH.get().applyAsDouble(index))).anyMatch(Double::isInfinite)) {
      return Resistance1Layer.inverseStatic(systems, rOhmsBefore);
    }

    IntToDoubleFunction logApparentFunction = index -> log(new Resistance1Layer(systems[index]).getApparent(rOhmsBefore[index]));
    double[] logApparent = rangeSystems(systems.length, logApparentFunction);

    double[] logDiff = rangeSystems(systems.length, apparentDiffByH.get());
    double[] measured = rangeSystems(systems.length, i -> logApparent[i] - logDiff[i]);

    int p2 = 200;
    return IntStream.iterate(0, i -> i + 1).mapToDouble(divider -> StrictMath.exp(divider / 4.0))
        .mapToInt(divider -> (int) Math.ceil(divider)).takeWhile(divider -> divider < p2 - 1).distinct()
        .mapToObj(divider -> {
          double h = Math.abs(dH / divider);
          BiFunction<double[], int[], IntToDoubleFunction> logApparentPredictedFunction = (k, p) ->
              index -> new Log1pApparent3Rho(systems[index]).value(k[0], k[1], h, p[0], p[1]);

          IntFunction<PointValuePair> pIterate = p1 -> {
            int p2mp1 = p2 - p1;

            UnaryOperator<double[]> diffPredictedFunction =
                k -> rangeSystems(systems.length, index -> {
                  double rho1 = 1.0;
                  double rho2 = rho1 / Layers.getRho1ToRho2(k[0]);
                  double rho3 = rho2 / Layers.getRho1ToRho2(k[1]);
                  return new Resistance1Layer(systems[index]).getApparent((
                      new Resistance3Layer(systems[index], h).value(rho1, rho2, rho3, p1 + (int) Math.round(Math.signum(dH) * divider), p2mp1) -
                          new Resistance3Layer(systems[index], h).value(rho1, rho2, rho3, p1, p2mp1))
                  );
                });

            ToDoubleFunction<double[]> kIterate =
                k -> {
                  double[] logApparentPredicted = rangeSystems(systems.length,
                      index -> logApparentPredictedFunction.apply(k, new int[] {p1, p2mp1}).applyAsDouble(index)
                  );
                  double[] diffPredicted = diffPredictedFunction.apply(k);
                  double[] predicted = rangeSystems(systems.length, i -> logApparentPredicted[i] - log(Math.abs(diffPredicted[i])));
                  return Inequality.absolute().applyAsDouble(measured, predicted);
                };

            ToLongFunction<PointValuePair> countSigns =
                point -> {
                  double[] diffPredicted = diffPredictedFunction.apply(point.getPoint());
                  return IntStream.range(0, systems.length)
                      .mapToObj(index -> Double.compare(Math.signum(rDiff.applyAsDouble(index)), Math.signum(diffPredicted[index])) == 0)
                      .filter(Boolean::booleanValue).count();
                };

            PointValuePair optimizeK = IntStream.range(0, 4)
                .mapToObj(i -> {
                  boolean b1 = (i & 1) == 0;
                  boolean b2 = (i & 2) == 0;
                  return new SimpleBounds(new double[] {b2 ? 0.0 : -1.0, b1 ? 0.0 : -1.0}, new double[] {b2 ? 1.0 : 0.0, b1 ? 1.0 : 0.0});
                }).map(bounds -> Simplex.optimize(kIterate::applyAsDouble, bounds))
                .min(Comparator.comparingLong(countSigns).reversed().thenComparingDouble(Pair::getValue)).orElseThrow();
            PointValuePair p = new PointValuePair(
                new double[] {optimizeK.getPoint()[0], optimizeK.getPoint()[1], p1, countSigns.applyAsLong(optimizeK)}, optimizeK.getValue()
            );
            Logger.getLogger(Resistance3Layer.class.getName()).config(
                () -> {
                  double[] v = p.getPoint();
                  return String.format("/%d [%.2f / %.2f] p1 = %.0f; signs = %.0f; e = %.6f", divider, v[0], v[1], v[2], v[3], p.getValue());
                }
            );
            return p;
          };

          PointValuePair min = IntStream.range(divider + 1, p2).mapToObj(pIterate)
              .min(Comparator.<PointValuePair>comparingDouble(o -> o.getPoint()[3]).reversed().thenComparingDouble(Pair::getValue))
              .orElseThrow();

          double[] k = Arrays.copyOf(min.getPoint(), 2);
          int p1 = (int) Math.round(min.getPoint()[2]);
          int p2mp1 = p2 - p1;
          double sumLogApparent = Resistance2Layer.sumLog(systems, logApparentFunction);
          double sumLogApparentPredicted = Resistance2Layer.sumLog(systems,
              index -> logApparentPredictedFunction.apply(k, new int[] {p1, p2mp1}).applyAsDouble(index)
          );
          double rho1 = StrictMath.exp((sumLogApparent - sumLogApparentPredicted) / systems.length);
          double rho2 = rho1 / Layers.getRho1ToRho2(k[0]);
          double rho3 = rho2 / Layers.getRho1ToRho2(k[1]);

          Medium medium = new Medium.Builder(systems, rOhmsBefore, rOhmsAfter, dH,
              (s, deltaH) -> new Resistance3Layer(s, h).value(rho1, rho2, rho3, p1 + (int) Math.round(Math.signum(deltaH) * divider), p2mp1))
              .inequality(min.getValue())
              .addLayer(rho1, p1 * h)
              .addLayer(rho2, p2mp1 * h)
              .build(rho3);
          Logger.getLogger(Resistance3Layer.class.getName()).info(
              () -> String.format("/%d%n%s", divider, medium)
          );
          return medium;
        })
        .min(Comparator.comparingDouble(Medium::getInequality)).orElseThrow();
  }
}
