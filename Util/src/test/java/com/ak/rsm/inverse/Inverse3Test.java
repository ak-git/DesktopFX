package com.ak.rsm.inverse;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;
import java.util.function.ToDoubleBiFunction;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

import com.ak.inverse.Inequality;
import com.ak.math.Simplex;
import com.ak.rsm.apparent.Apparent3Rho;
import com.ak.rsm.resistance.DerivativeResistance;
import com.ak.rsm.resistance.Resistance;
import com.ak.rsm.resistance.TetrapolarDerivativeResistance;
import com.ak.rsm.system.TetrapolarSystem;
import com.ak.util.Metrics;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleBounds;
import org.testng.annotations.Test;

import static com.ak.rsm.measurement.Measurements.getBaseL;
import static java.lang.StrictMath.abs;
import static java.lang.StrictMath.log;

public class Inverse3Test {
  private static final double H_STEP = Metrics.fromMilli(0.01);

  @Test(invocationCount = 20, enabled = false)
  public void test() {
    Collection<DerivativeResistance> m000 = TetrapolarDerivativeResistance.milli().dh(Double.NaN).system2(6.0)
        .rho(
            4.36484833090749, 4.49332876699692,
            -0.168318891626108, -0.182683171577791
        );
    Collection<DerivativeResistance> m105 = TetrapolarDerivativeResistance.milli().dh(Double.NaN).system2(6.0)
        .rho(
            4.37027448440132, 4.4985260054503,
            -0.196894129863152, -0.243242343614173
        );
    Collection<DerivativeResistance> m210 = TetrapolarDerivativeResistance.milli().dh(Double.NaN).system2(6.0)
        .rho(
            4.37794989890251, 4.50886362514046,
            -0.224049353755586, -0.310504288925638
        );

    Function<Collection<DerivativeResistance>, double[]> subLog =
        m -> m.stream().mapToDouble(d -> log(d.resistivity()) - log(abs(d.derivativeResistivity()))).toArray();

    double[] subLog000 = subLog.apply(m000);
    double[] subLog105 = subLog.apply(m105);
    double[] subLog210 = subLog.apply(m210);

    Collection<TetrapolarSystem> systems = m000.stream().map(Resistance::system).toList();

    var logApparentPredicted = logApparentPredicted(systems);
    var logDiffApparentPredicted = logDiffApparentPredicted(systems);

    PointValuePair kwOptimal = Simplex.optimizeAll(
        kw -> {
          double step = kw[4];
          int layer = 3;
          double[] subLogPredicted000 = systems.stream()
              .mapToDouble(s -> logApparentPredicted.applyAsDouble(s, kw) - logDiffApparentPredicted.applyAsDouble(s, kw))
              .toArray();

          double[] kw105 = kw.clone();
          kw105[layer] = kw[layer] - step;
          double[] subLogPredicted105 = systems.stream()
              .mapToDouble(s -> logApparentPredicted.applyAsDouble(s, kw105) - logDiffApparentPredicted.applyAsDouble(s, kw105))
              .toArray();

          double[] kw210 = kw.clone();
          kw105[layer] = kw[layer] - step * 2;
          double[] subLogPredicted210 = systems.stream()
              .mapToDouble(s -> logApparentPredicted.applyAsDouble(s, kw210) - logDiffApparentPredicted.applyAsDouble(s, kw210))
              .toArray();

          return Inequality.absolute().applyAsDouble(subLog000, subLogPredicted000) +
              Inequality.absolute().applyAsDouble(subLog105, subLogPredicted105) +
              Inequality.absolute().applyAsDouble(subLog210, subLogPredicted210);
        },
        new SimpleBounds(new double[] {-1.0, -1.0, 1, 210 + 1, 1}, new double[] {1.0, 1.0, 500, 500, 110}),
        new double[] {0.01, 0.01, 5, 5, 2}
    );
    Logger.getAnonymousLogger().info(() -> "%.6f %s".formatted(kwOptimal.getValue(), Arrays.toString(kwOptimal.getPoint())));
  }

  @Nonnull
  public static ToDoubleBiFunction<TetrapolarSystem, double[]> logApparentPredicted(
      @Nonnull Collection<TetrapolarSystem> systems) {
    double baseL = getBaseL(systems);
    return (s, kw) -> Apparent3Rho.newLog1pApparent3Rho(s.relativeSystem())
        .value(
            kw[0], kw[1],
            H_STEP * baseL / s.lCC(),
            (int) Math.round(kw[2]), (int) Math.round(kw[3])
        );
  }

  @Nonnull
  public static ToDoubleBiFunction<TetrapolarSystem, double[]> logDiffApparentPredicted(
      @Nonnull Collection<TetrapolarSystem> measurements) {
    double baseL = getBaseL(measurements);
    return (s, kw) -> log(
        Math.abs(
            Apparent3Rho.newDerivativeApparentByPhi2Rho(s.relativeSystem())
                .value(
                    kw[0], kw[1],
                    H_STEP * baseL / s.lCC(),
                    (int) Math.round(kw[2]), (int) Math.round(kw[3])
                )
        )
    );
  }
}
