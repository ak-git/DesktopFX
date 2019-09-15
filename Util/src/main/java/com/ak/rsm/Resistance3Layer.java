package com.ak.rsm;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.IntToDoubleFunction;
import java.util.function.ToDoubleBiFunction;
import java.util.function.ToDoubleFunction;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.inverse.Inequality;
import com.ak.math.Simplex;
import com.ak.util.LineFileBuilder;
import com.ak.util.Strings;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleBounds;
import tec.uom.se.AbstractUnit;

import static java.lang.StrictMath.log;

/**
 * Calculates <b>full</b> resistance R<sub>m-n</sub> (in Ohm) between electrodes for <b>3-layer</b> model.
 */
final class Resistance3Layer extends AbstractResistanceLayer<Potential3Layer> {
  Resistance3Layer(@Nonnull TetrapolarSystem electrodeSystem, double hStep) {
    super(electrodeSystem, value -> new Potential3Layer(value, Math.abs(hStep)));
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
    return applyAsDouble(u -> u.value(rho1, rho2, rho3, p1, p2mp1));
  }

  @Nonnull
  public static Medium inverse2(@Nonnull TetrapolarSystem[] systems, @Nonnull double[] rOhmsBefore, @Nonnull double[] rOhmsAfter, double dh) {
    double dhAbs = Math.abs(dh);
    ToDoubleBiFunction<Integer, IntToDoubleFunction> diff = (i, toDouble) -> toDouble.applyAsDouble(i) - toDouble.applyAsDouble((i + 1) % systems.length);
    double[] subLogApparent = IntStream.range(0, systems.length)
        .mapToDouble(i -> diff.applyAsDouble(i, index -> log(new Resistance1Layer(systems[index]).getApparent(rOhmsBefore[index])))).toArray();

    double[] subLogDiff = IntStream.range(0, systems.length)
        .mapToDouble(i -> diff.applyAsDouble(i, index -> {
          double a = rOhmsAfter[index] - rOhmsBefore[index];
          a /= dh;
          return log(Math.abs(a)) * Math.signum(a);
        })).toArray();

    if (Arrays.stream(subLogDiff).anyMatch(Double::isNaN)) {
      double rho = Resistance1Layer.inverse(systems, rOhmsBefore).getRho();
      return new Medium.Builder(systems, rOhmsBefore, s -> new Resistance3Layer(s, dhAbs).value(rho, rho, rho, 0, 0)).addLayer(rho, 0).addLayer(rho, 0).build(rho);
    }

    int p1 = 5;
    int p2mp1 = 5;

    ToDoubleFunction<double[]> findK = point -> {
      double k12 = point[0];
      double k23 = point[1];
      double[] subLogApparentPredicted = IntStream.range(0, systems.length)
          .mapToDouble(i ->
              diff.applyAsDouble(i, index ->
                  new Log1pApparent3Rho(systems[index].sToL(), systems[index].Lh(dhAbs)).value(k12, k23, p1, p2mp1)
              )
          ).toArray();

      double[] subLogDiffPredicted = IntStream.range(0, systems.length)
          .mapToDouble(i ->
              diff.applyAsDouble(i, index ->
                  {
                    Resistance3Layer resistance3Layer = new Resistance3Layer(systems[index], dhAbs);
                    double rho1 = 1.0;
                    double rho2 = rho1 / Layers.getRho1ToRho2(k12);
                    double rho3 = rho2 / Layers.getRho1ToRho2(k23);
                    double a = resistance3Layer.value(rho1, rho2, rho3, p1 + (int) Math.signum(dh), p2mp1) -
                        resistance3Layer.value(rho1, rho2, rho3, p1, p2mp1);
                    a /= dh;
                    return log(Math.abs(a)) * Math.signum(a);
                  }
              )
          ).toArray();
      Inequality inequality = Inequality.absolute();
//      inequality.applyAsDouble(subLogApparent, subLogApparentPredicted);
      inequality.applyAsDouble(subLogDiff, subLogDiffPredicted);
      return inequality.getAsDouble();
    };

    try {
      LineFileBuilder.of("%.1f %.1f %.6f").
          xRange(-0.9, -0.1, 0.1).
          yRange(0.1, 0.9, 0.1).
          generate("z.txt",
              (k12, k23) -> findK.applyAsDouble(new double[] {k12, k23}));
    }
    catch (IOException e) {
      Logger.getAnonymousLogger().log(Level.INFO, e.getMessage(), e);
    }
    return new Medium.Builder(systems, rOhmsBefore, s -> 0.0).build(0.0);
  }

  @Nonnull
  public static Medium inverse(@Nonnull TetrapolarSystem[] systems, @Nonnull double[] rOhmsBefore, @Nonnull double[] rOhmsAfter, double dh) {
    Logger.getAnonymousLogger().log(Level.INFO, Resistance2Layer.inverse(systems, rOhmsBefore).toString());
    Medium inverse2 = Resistance2Layer.inverse(Arrays.copyOf(systems, 2), Arrays.copyOf(rOhmsBefore, 2), Arrays.copyOf(rOhmsAfter, 2), dh);
    Logger.getAnonymousLogger().log(Level.INFO, inverse2.toString());

    double dhAbs = Math.abs(dh);
    ToDoubleBiFunction<Integer, IntToDoubleFunction> diff = (i, toDouble) -> toDouble.applyAsDouble(i) - toDouble.applyAsDouble((i + 1) % systems.length);
    double[] subLogApparent = IntStream.range(0, systems.length)
        .mapToDouble(i -> diff.applyAsDouble(i, index -> log(new Resistance1Layer(systems[index]).getApparent(rOhmsBefore[index])))).toArray();

    double[] subLogDiff = IntStream.range(0, systems.length)
        .mapToDouble(i -> diff.applyAsDouble(i, index -> {
          double a = rOhmsAfter[index] - rOhmsBefore[index];
          a /= dh;
          return log(Math.abs(a)) * Math.signum(a);
        })).toArray();

    if (Arrays.stream(subLogDiff).anyMatch(Double::isNaN)) {
      double rho = Resistance1Layer.inverse(systems, rOhmsBefore).getRho();
      return new Medium.Builder(systems, rOhmsBefore, s -> new Resistance3Layer(s, dhAbs).value(rho, rho, rho, 0, 0)).addLayer(rho, 0).addLayer(rho, 0).build(rho);
    }

    SimpleBounds kBounds = new SimpleBounds(
        new double[] {-1.0 + Double.MIN_VALUE, -1.0 + Double.MIN_VALUE},
        new double[] {1.0 - Double.MIN_VALUE, 1.0 - Double.MIN_VALUE}
    );
    double[] kInitial = {-0.5, 0.5};
    Map<String, PointValuePair> kResults = new HashMap<>();

    BiFunction<Integer, Integer, PointValuePair> findK = (p1, p2mp1) -> {
      String key = String.format("%d %d", p1, p2mp1);
      if (kResults.containsKey(key)) {
        return kResults.get(key);
      }
      else {
        PointValuePair optimize = Simplex.optimize(point -> {
          double k12 = point[0];
          double k23 = point[1];
          double[] subLogApparentPredicted = IntStream.range(0, systems.length)
              .mapToDouble(i ->
                  diff.applyAsDouble(i, index ->
                      new Log1pApparent3Rho(systems[index].sToL(), systems[index].Lh(dhAbs)).value(k12, k23, p1, p2mp1)
                  )
              ).toArray();

          double[] subLogDiffPredicted = IntStream.range(0, systems.length)
              .mapToDouble(i ->
                  diff.applyAsDouble(i, index ->
                      {
                        Resistance3Layer resistance3Layer = new Resistance3Layer(systems[index], dhAbs);
                        double rho1 = 1.0;
                        double rho2 = rho1 / Layers.getRho1ToRho2(k12);
                        double rho3 = rho2 / Layers.getRho1ToRho2(k23);
                        double a = resistance3Layer.value(rho1, rho2, rho3, p1 + (int) Math.signum(dh), p2mp1) -
                            resistance3Layer.value(rho1, rho2, rho3, p1, p2mp1);
                        a /= dh;
                        return log(Math.abs(a)) * Math.signum(a);
                      }
                  )
              ).toArray();
          Inequality inequality = Inequality.absolute();
          inequality.applyAsDouble(subLogApparent, subLogApparentPredicted);
          inequality.applyAsDouble(subLogDiff, subLogDiffPredicted);
          return inequality.getAsDouble();
        }, kBounds, kInitial, new double[] {0.1, 0.1});
        for (int i = 0; i < kInitial.length; i++) {
          kInitial[i] = optimize.getPoint()[i];
        }
        kResults.put(key, optimize);
        System.out.printf("[%d %d] %s %.6f %n", p1, p2mp1, Strings.toString("%.2f", optimize.getPoint(), AbstractUnit.ONE), optimize.getValue());
        return optimize;
      }
    };

    PointValuePair optimize = Simplex.optimizeCMAES(p -> findK.apply((int) Math.round(p[0]), (int) Math.round(p[1])).getValue(),
        new SimpleBounds(new double[] {1.0, 1.0}, new double[] {Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY}),
        new double[] {1, 1}, new double[] {10, 10});

    System.out.printf("%s%n", Strings.toString("%.1f", optimize.getPoint(), AbstractUnit.ONE));

//    ToDoubleBiFunction<double[], int[]> findRho3 = (k, p) ->
//        Simplex.optimize(rho3 -> {
//          double rho2 = Layers.getRho1ToRho2(k[1]) * rho3[0];
//          double rho1 = Layers.getRho1ToRho2(k[0]) * rho2;
//          double[] apparent = IntStream.range(0, systems.length).mapToDouble(i -> new Resistance1Layer(systems[i]).getApparent(rOhmsBefore[i])).toArray();
//          double[] predicted = Arrays.stream(systems).mapToDouble(system -> {
//            double r = new Resistance3Layer(system, dhAbs).value(rho1, rho2, rho3[0], p[0], p[1]);
//            return new Resistance1Layer(system).getApparent(r);
//          }).toArray();
//          return Inequality.absolute().applyAsDouble(apparent, predicted);
//        }, new SimpleBounds(new double[] {0.0}, new double[] {Double.POSITIVE_INFINITY}), new double[] {inverse2.getRho2()}, new double[] {0.1}).getPoint()[0];
//
//    Function<int[], double[]> findRhos = p -> {
//      int p1 = p[0];
//      int p2 = p[1];
//      double[] k = findK.apply(p1, p2);
//      double rho3 = findRho3.applyAsDouble(k, new int[] {p1, p2});
//      double rho2 = Layers.getRho1ToRho2(k[1]) * rho3;
//      double rho1 = Layers.getRho1ToRho2(k[0]) * rho2;
//      System.out.printf("%d %d %s%n", p1, p2, Strings.toString("%.3f", new double[] {rho1, rho2, rho3}, Units.OHM));
//      return new double[] {rho1, rho2, rho3};
//    };
//
//    PointValuePair pPoint = Simplex.optimize(p -> {
//      int p1 = (int) p[0];
//      int p2 = (int) p[1];
//
//      double[] rhos = findRhos.apply(new int[] {p1, p2});
//      double rho1 = rhos[0];
//      double rho2 = rhos[1];
//      double rho3 = rhos[2];
//
//      double[] apparentDiff = IntStream.range(0, systems.length).mapToDouble(i -> {
//        Resistance1Layer resistance = new Resistance1Layer(systems[i]);
//        double r1 = resistance.getApparent(rOhmsAfter[i]);
//        double r0 = resistance.getApparent(rOhmsBefore[i]);
//        return (r1 - r0);
//      }).toArray();
//      double[] predictedDiff = Arrays.stream(systems).mapToDouble(system -> {
//        Resistance3Layer resistance3Layer = new Resistance3Layer(system, dhAbs);
//        return new Resistance1Layer(system).getApparent(
//            resistance3Layer.value(rho1, rho2, rho3, p1 + (int) Math.signum(dh), p2) -
//                resistance3Layer.value(rho1, rho2, rho3, p1, p2)
//        );
//      }).toArray();
//      return Inequality.absolute().applyAsDouble(apparentDiff, predictedDiff);
//    }, new SimpleBounds(new double[] {1.0, 1.0}, new double[] {Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY}),
//        new double[] {1.0, 1.0}, new double[] {1.0, 1.0});
//
//    int p1 = (int) pPoint.getPoint()[0];
//    int p2mp1 = (int) pPoint.getPoint()[1];
//    double[] rhos = findRhos.apply(new int[] {p1, p2mp1});
//    return new Medium.Builder(systems, rOhmsBefore, rOhmsAfter, dh,
//        (s, dH) -> new Resistance3Layer(s, dhAbs).value(rhos[0], rhos[1], rhos[2], p1 + (int) (dH / dhAbs), p2mp1))
//        .addLayer(rhos[0], p1 * dhAbs)
//        .addLayer(rhos[1], (p2mp1 + p1) * dhAbs).build(rhos[2]);
    return new Medium.Builder(systems, rOhmsBefore, s -> 0.0).build(0.0);
  }

  private static double mean(@Nonnull SimpleBounds bounds, @Nonnegative int index) {
    return (bounds.getUpper()[index] + bounds.getLower()[index]) / 2.0;
  }
}
