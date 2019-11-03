package com.ak.rsm;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntToDoubleFunction;
import java.util.function.ToDoubleBiFunction;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.inverse.Inequality;
import com.ak.math.Simplex;
import com.ak.util.Strings;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleBounds;
import org.apache.commons.math3.util.Pair;

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
  public static Medium inverse(@Nonnull TetrapolarSystem[] systems, @Nonnull double[] rOhmsBefore, @Nonnull double[] rOhmsAfter, double dh) {
    ToDoubleBiFunction<Integer, IntToDoubleFunction> diff = Resistance2Layer.newDiff(systems.length);
    IntToDoubleFunction logApparentFunction = Resistance2Layer.logApparentFunction(systems, rOhmsBefore);

    double[] subLogApparent = IntStream.range(0, systems.length)
        .mapToDouble(i -> diff.applyAsDouble(i, logApparentFunction)).limit(systems.length - 1).toArray();
    double[] subLogDiff = IntStream.range(0, systems.length)
        .mapToDouble(i ->
            diff.applyAsDouble(i, index -> {
              double a = new Resistance1Layer(systems[index]).getApparent(rOhmsAfter[index] - rOhmsBefore[index]);
              return log(Math.abs(a)) * Math.signum(a);
            }))
        .limit(systems.length - 1).toArray();

    if (Arrays.stream(subLogDiff).anyMatch(Double::isNaN)) {
      double rho = Resistance1Layer.inverse(systems, rOhmsBefore).getRho();
      return new Medium.Builder(systems, rOhmsBefore, s -> new Resistance3Layer(s, dh).value(rho, rho, rho, 0, 0))
          .addLayer(rho, 0).addLayer(rho, 0).build(rho);
    }

    try {
      Logger.getAnonymousLogger().log(Level.INFO, Resistance2Layer.inverse(systems, rOhmsBefore).toString());
    }
    catch (MaxCountExceededException ex) {
      Logger.getAnonymousLogger().log(Level.WARNING, "Static 2-layer problem has no solution");
    }
    Logger.getAnonymousLogger().log(Level.INFO, Resistance2Layer.inverse(systems, rOhmsBefore, rOhmsAfter, dh).toString());

    BiFunction<double[], int[], IntToDoubleFunction> logApparentPredictedFunction = (k, p) -> index -> {
      double rho1 = 1.0;
      double rho2 = rho1 / Layers.getRho1ToRho2(k[0]);
      double rho3 = rho2 / Layers.getRho1ToRho2(k[1]);
      double resistance = new Resistance3Layer(systems[index], dh).value(rho1, rho2, rho3, p[0], p[1]);
      return log(new Resistance1Layer(systems[index]).getApparent(resistance));
    };

    Map<String, PointValuePair> cacheK = new ConcurrentHashMap<>();
    Function<int[], Double> findP = p -> {
      String key = Arrays.toString(p);
      if (cacheK.containsKey(key)) {
        return cacheK.get(key).getValue();
      }
      else {
        return cacheK.computeIfAbsent(key, s -> {
          double[] k = IntStream.range(0, 1).mapToObj(value -> Simplex.optimize(Strings.EMPTY, x -> {
                double[] subLogApparentPredicted = IntStream.range(0, systems.length)
                    .mapToDouble(i -> diff.applyAsDouble(i, logApparentPredictedFunction.apply(x, p)))
                    .limit(systems.length - 1).toArray();
                return Inequality.absolute().applyAsDouble(subLogApparent, subLogApparentPredicted);
              },
              new SimpleBounds(new double[] {-0.99, -0.99}, new double[] {0.99, 0.99}),
              new double[] {(Math.random() * 2 - 1.0) * 0.99, (Math.random() * 2 - 1.0) * 0.99}, new double[] {0.1, 0.1}))
              .peek(r -> Logger.getAnonymousLogger().config(Strings.toString("%.3f", r.getPoint()) + " : " + r.getValue()))
              .min(Comparator.comparingDouble(Pair::getValue)).orElseThrow().getPoint();

          double[] subLogDiffPredicted = IntStream.range(0, systems.length)
              .mapToDouble(i ->
                  diff.applyAsDouble(i, index -> {
                        Resistance3Layer resistance3Layer = new Resistance3Layer(systems[index], dh);
                        double rho1 = 1.0;
                        double rho2 = rho1 / Layers.getRho1ToRho2(k[0]);
                        double rho3 = rho2 / Layers.getRho1ToRho2(k[1]);

                        Resistance1Layer resistance1Layer = new Resistance1Layer(systems[index]);
                        int p1 = p[0];
                        int p2mp1 = p[1];
                        double a = resistance1Layer.getApparent(
                            resistance3Layer.value(rho1, rho2, rho3, p1 + (int) Math.signum(dh), p2mp1) -
                                resistance3Layer.value(rho1, rho2, rho3, p1, p2mp1)
                        );
                        return log(Math.abs(a)) * Math.signum(a);
                      }
                  ))
              .limit(systems.length - 1).toArray();

          return new PointValuePair(k, Inequality.absolute().applyAsDouble(subLogDiff, subLogDiffPredicted));
        }).getValue();
      }
    };

    Function<double[], int[]> doublesToInt = doubles -> Arrays.stream(doubles).mapToInt(v -> (int) Math.round(v)).toArray();

    AtomicReference<PointValuePair> firstPFound = new AtomicReference<>();
    int[] p = doublesToInt.apply(
        IntStream.range(0, 16).mapToObj(value ->
            Simplex.optimizeCMAES(x -> findP.apply(doublesToInt.apply(x)),
                new SimpleBounds(new double[] {1.0, 1.0}, new double[] {100.0, 100.0}),
                new double[] {Math.random() * 99 + 1.0, Math.random() * 99 + 1.0}, new double[] {1, 1}))
            .peek(r -> {
              firstPFound.set(r);
              Logger.getAnonymousLogger().info(Arrays.toString(doublesToInt.apply(r.getPoint())) + " : " + r.getValue());
            })
            .takeWhile(pointValuePair -> pointValuePair.getValue() > Simplex.STOP_FITNESS * 5.0)
            .min(Comparator.comparingDouble(Pair::getValue)).orElse(firstPFound.get()).getPoint()
    );
    double[] k = cacheK.get(Arrays.toString(p)).getPoint();

    double sumLogApparent = Resistance2Layer.sumLogApparent(systems, rOhmsBefore);
    double sumLogApparentPredicted = Resistance2Layer.sumLogApparentPredicted(systems,
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
