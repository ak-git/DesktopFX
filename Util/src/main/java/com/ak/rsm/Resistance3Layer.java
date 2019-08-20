package com.ak.rsm;

import java.util.Arrays;
import java.util.function.DoubleUnaryOperator;
import java.util.function.IntToDoubleFunction;
import java.util.function.ToDoubleBiFunction;
import java.util.stream.IntStream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.inverse.Inequality;
import com.ak.math.Simplex;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleBounds;

import static java.lang.StrictMath.log;

/**
 * Calculates <b>full</b> resistance R<sub>m-n</sub> (in Ohm) between electrodes for <b>3-layer</b> model.
 */
final class Resistance3Layer extends AbstractResistanceLayer<Potential3Layer> {
  Resistance3Layer(@Nonnull TetrapolarSystem electrodeSystem, @Nonnegative double hStep) {
    super(electrodeSystem, value -> new Potential3Layer(value, hStep));
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
  public static Medium inverse(@Nonnull TetrapolarSystem[] systems, @Nonnull double[] rOhmsBefore, @Nonnull double[] rOhmsAfter, double dh) {
//    Logger.getAnonymousLogger().log(Level.INFO, Resistance2Layer.inverse(systems, rOhmsBefore).toString());
//    Medium inverse2 = Resistance2Layer.inverse(Arrays.copyOf(systems, 2), Arrays.copyOf(rOhmsBefore, 2), Arrays.copyOf(rOhmsAfter, 2), dh);
//    Logger.getAnonymousLogger().log(Level.INFO, inverse2.toString());

    ToDoubleBiFunction<Integer, IntToDoubleFunction> diff = (i, toDouble) -> toDouble.applyAsDouble(i) - toDouble.applyAsDouble((i + 1) % systems.length);
    double[] subLogApparent = IntStream.range(0, systems.length)
        .mapToDouble(i -> diff.applyAsDouble(i, index -> log(new Resistance1Layer(systems[index]).getApparent(rOhmsBefore[index])))).toArray();

    double[] subLogDiff = IntStream.range(0, systems.length)
        .mapToDouble(i -> diff.applyAsDouble(i, index -> log(Math.abs((rOhmsAfter[index] - rOhmsBefore[index]) / dh)))).toArray();

    if (Arrays.stream(subLogDiff).anyMatch(Double::isNaN)) {
      double rho = Resistance1Layer.inverse(systems, rOhmsBefore).getRho();
      return new Medium.Builder(systems, rOhmsBefore, s -> new Resistance3Layer(s, dh).value(rho, rho, rho, 0, 0)).addLayer(rho, 0).addLayer(rho, 0).build(rho);
    }

    int p1 = 5;
    int p2 = 5;

    DoubleUnaryOperator find = k12 -> {
      PointValuePair k23Point = Simplex.optimize(k23point -> {
        double k23 = k23point[0];
        double[] subLogApparentPredicted = IntStream.range(0, systems.length)
            .mapToDouble(i ->
                diff.applyAsDouble(i, index ->
                    new Log1pApparent3Rho(systems[index].sToL(), systems[index].Lh(Math.abs(dh))).value(k12, k23, p1, p2)
                )
            ).toArray();

        return Inequality.absolute().applyAsDouble(subLogApparent, subLogApparentPredicted);
      }, new SimpleBounds(new double[] {-1.0}, new double[] {1.0}), new double[] {0.0}, new double[] {0.1});

      PointValuePair k23PointDiff = Simplex.optimize(k23point -> {
        double k23 = k23point[0];
        double[] subLogDiffPredicted = IntStream.range(0, systems.length)
            .mapToDouble(i ->
                diff.applyAsDouble(i, index ->
                    {
                      Resistance3Layer resistance3Layer = new Resistance3Layer(systems[index], dh);
                      double rho1 = 1.0;
                      double rho2 = rho1 / Layers.getRho1ToRho2(k12);
                      double rho3 = rho2 / Layers.getRho1ToRho2(k23);
                      return log(Math.abs(
                          (resistance3Layer.value(rho1, rho2, rho3, p1 + (int) Math.signum(dh), p2) -
                              resistance3Layer.value(rho1, rho2, rho3, p1, p2)) / dh
                          )
                      );
                    }
                )
            ).toArray();
        return Inequality.absolute().applyAsDouble(subLogDiff, subLogDiffPredicted);
      }, new SimpleBounds(new double[] {-1.0}, new double[] {1.0}), k23Point.getPoint(), new double[] {0.01});

      System.out.printf("%.2f %.2f%n", k12, k23Point.getPoint()[0]);
      return Inequality.absolute().applyAsDouble(k23Point.getPoint(), k23PointDiff.getPoint());
    };

    PointValuePair optimize = Simplex.optimize(point -> {
      double k12 = point[0];
      return find.applyAsDouble(k12);
    }, new SimpleBounds(new double[] {-1.0}, new double[] {1.0}), new double[] {-0.1}, new double[] {0.1});

    System.out.printf("%.2f%n", optimize.getPoint()[0], optimize.getValue());

    return new Medium.Builder(systems, rOhmsBefore, s -> new Resistance3Layer(s, dh).value(0.0, 0.0, 0.0, 0, 0)).addLayer(0.0, 0.0).addLayer(0.0, 0.0).build(0.0);
  }
}
