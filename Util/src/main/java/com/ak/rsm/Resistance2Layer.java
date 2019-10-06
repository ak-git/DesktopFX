package com.ak.rsm;

import java.util.Arrays;
import java.util.function.DoubleUnaryOperator;
import java.util.function.IntToDoubleFunction;
import java.util.function.ToDoubleBiFunction;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.inverse.Inequality;
import com.ak.math.Simplex;
import org.apache.commons.math3.analysis.TrivariateFunction;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleBounds;
import tec.uom.se.unit.Units;

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
    Medium inverse = Resistance1Layer.inverse(systems, rOhmsBefore);
    double rho = inverse.getRho();
    Logger.getLogger(Resistance2Layer.class.getName()).log(Level.INFO, inverse.toString());

    ToDoubleBiFunction<Integer, IntToDoubleFunction> diff = (i, toDouble) -> toDouble.applyAsDouble(i) - toDouble.applyAsDouble((i + 1) % systems.length);
    double[] subLogApparent = IntStream.range(0, systems.length)
        .mapToDouble(i -> diff.applyAsDouble(i, index -> log(new Resistance1Layer(systems[index]).getApparent(rOhmsBefore[index]))))
        .limit(systems.length - 1).toArray();

    double[] subLogDiff = IntStream.range(0, systems.length)
        .mapToDouble(i -> diff.applyAsDouble(i, index -> log(Math.abs(rOhmsAfter[index] - rOhmsBefore[index]))))
        .limit(systems.length - 1).toArray();

    if (Arrays.stream(subLogDiff).anyMatch(Double::isNaN)) {
      return new Medium.Builder(systems, rOhmsBefore, s -> new Resistance2Layer(s).value(rho, rho, 0)).addLayer(rho, 0).build(rho);
    }

    DoubleUnaryOperator findK = h -> Simplex.optimize("k = %.3f", k -> {
      double[] subLogApparentPredicted = IntStream.range(0, systems.length)
          .mapToDouble(i -> diff.applyAsDouble(i, index -> new Log1pApparent2Rho(systems[index]).value(k, h)))
          .limit(systems.length - 1).toArray();
      return Inequality.absolute().applyAsDouble(subLogApparent, subLogApparentPredicted);
    }, new double[] {-1.0, 1.0}, 0.0, 0.1).getPoint()[0];

    double maxL = Arrays.stream(systems).mapToDouble(s -> s.Lh(1.0)).max().orElseThrow();
    PointValuePair findLh = Simplex.optimize("h = %.4f " + Units.METRE, h -> {
          double k = findK.applyAsDouble(h);
          double[] subLogDiffPredicted = IntStream.range(0, systems.length)
              .mapToDouble(i -> diff.applyAsDouble(i, index -> {
                TrivariateFunction resistance = new Resistance2Layer(systems[index]);
                double a = resistance.value(1.0, 1.0 / Layers.getRho1ToRho2(k), h + dh) - resistance.value(1.0, 1.0 / Layers.getRho1ToRho2(k), h);
                return log(Math.abs(a));
              }))
              .limit(systems.length - 1).toArray();
          return Inequality.absolute().applyAsDouble(subLogDiff, subLogDiffPredicted);
        },
        new double[] {0.0, maxL}, maxL / 2.0, maxL / 10.0
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
