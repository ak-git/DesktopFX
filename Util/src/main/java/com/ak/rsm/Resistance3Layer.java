package com.ak.rsm;

import java.util.Arrays;
import java.util.function.IntToDoubleFunction;
import java.util.function.ToDoubleBiFunction;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.inverse.Inequality;
import com.ak.math.Simplex;
import com.ak.util.Strings;
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

  public static class Medium extends AbstractMedium {
    @Nonnull
    private final double[] rho;
    private final double hStep;
    @Nonnegative
    private final int p1;
    @Nonnegative
    private final int p2mp1;

    private Medium(@Nonnull double[] rho, double hStep, @Nonnegative int p1, @Nonnegative int p2mp1) {
      this.rho = Arrays.copyOf(rho, rho.length);
      this.hStep = hStep;
      this.p1 = p1;
      this.p2mp1 = p2mp1;
    }

    @Override
    public String toString() {
      return String.format("%s; %s; %s",
          IntStream.range(0, rho.length).mapToObj(i -> Strings.rho(rho[i], i + 1)).collect(Collectors.joining("; ")),
          Strings.h(p1 * Math.abs(hStep), 1),
          Strings.h((p1 + p2mp1) * Math.abs(hStep), 2)
      );
    }

    @Override
    public String toString(@Nonnull TetrapolarSystem[] systems, @Nonnull double[] rOhms) {
      return toString(systems, rOhms, s -> new Resistance3Layer(s, hStep).value(rho[0], rho[1], rho[2], p1, p2mp1));
    }

    @Nonnull
    public static Resistance3Layer.Medium inverse(@Nonnull TetrapolarSystem[] systems, @Nonnull double[] rOhmsBefore, @Nonnull double[] rOhmsAfter, double dh) {
      Resistance2Layer.Medium inverse2Static = Resistance2Layer.Medium.inverse(systems, rOhmsBefore);
      Logger.getAnonymousLogger().log(Level.INFO, inverse2Static.toString(systems, rOhmsBefore));

      Resistance2Layer.Medium inverse2 = Resistance2Layer.Medium.inverse(Arrays.copyOf(systems, 2), Arrays.copyOf(rOhmsBefore, 2), Arrays.copyOf(rOhmsAfter, 2), dh);
      Logger.getAnonymousLogger().log(Level.INFO, inverse2.toString(Arrays.copyOf(systems, 2), Arrays.copyOf(rOhmsBefore, 2)));

      ToDoubleBiFunction<Integer, IntToDoubleFunction> diff = (i, toDouble) -> toDouble.applyAsDouble(i) - toDouble.applyAsDouble((i + 1) % systems.length);
      double[] subLogApparent = IntStream.range(0, systems.length)
          .mapToDouble(i -> diff.applyAsDouble(i, index -> log(new Resistance1Layer(systems[index]).getApparent(rOhmsBefore[index])))).toArray();

      double[] subLogDiff = IntStream.range(0, systems.length)
          .mapToDouble(i -> diff.applyAsDouble(i, index -> log(Math.abs((rOhmsAfter[index] - rOhmsBefore[index]) / dh)))).toArray();

      if (Arrays.stream(subLogDiff).anyMatch(Double::isNaN)) {
        double rho = Resistance1Layer.Medium.inverse(systems, rOhmsBefore).getRho();
        return new Resistance3Layer.Medium(new double[] {rho, rho, rho}, 0.0, 0, 0);
      }

      int p1 = 5;
      int p2 = 5;

      double k12 = -1.0;
      if (k12 > -1.0) {
        PointValuePair pointValuePair = Simplex.optimize(k23point -> {
          double k23 = k23point[0];
          double[] subLogApparentPredicted = IntStream.range(0, systems.length)
              .mapToDouble(i ->
                  diff.applyAsDouble(i, index ->
                      new Log1pApparent3Rho(systems[index].sToL(), systems[index].Lh(Math.abs(dh))).value(k12, k23, p1, p2)
                  )
              ).toArray();

          return Inequality.absolute().applyAsDouble(subLogApparent, subLogApparentPredicted);
        }, new SimpleBounds(new double[] {-1.0}, new double[] {1.0}), new double[] {0.0}, new double[] {0.1});
      }
      else {
        return new Resistance3Layer.Medium(new double[] {inverse2.getRho1(), inverse2.getRho2(), inverse2.getRho2()},
            Math.abs(dh), (int) (inverse2.getH() / Math.abs(dh)), 0);
      }

      return new Resistance3Layer.Medium(new double[] {0, 0, 0}, dh, 1, 1);
    }
  }
}
