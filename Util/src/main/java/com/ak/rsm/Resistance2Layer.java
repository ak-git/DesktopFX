package com.ak.rsm;

import java.util.Arrays;
import java.util.Comparator;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.IntToDoubleFunction;
import java.util.function.Supplier;
import java.util.function.ToDoubleBiFunction;
import java.util.function.ToLongFunction;
import java.util.function.UnaryOperator;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.inverse.Inequality;
import com.ak.math.Simplex;
import com.ak.util.Metrics;
import org.apache.commons.math3.analysis.TrivariateFunction;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleBounds;
import org.apache.commons.math3.util.Pair;

import static java.lang.StrictMath.exp;
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
  @ParametersAreNonnullByDefault
  public static Medium inverseStaticLinear(TetrapolarSystem[] systems, double[] rOhms) {
    Medium inverse = Resistance1Layer.inverseStatic(systems, rOhms);
    if (systems.length > 2) {
      double rho = inverse.getRho();
      double maxL = Arrays.stream(systems).mapToDouble(s -> s.lToH(1.0)).max().orElseThrow();
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
    else {
      return inverse;
    }
  }

  @Nonnull
  @ParametersAreNonnullByDefault
  public static Medium inverseStaticLog(TetrapolarSystem[] systems, double[] rOhms) {
    if (systems.length > 2) {
      double[] logApparent = logApparent(systems, rOhms);
      BiFunction<Double, Double, IntToDoubleFunction> logApparentPredictedFunction = logApparentPredictedFunction(systems);
      ToDoubleBiFunction<Integer, IntToDoubleFunction> diff = newDiff(systems.length);
      double maxL = Arrays.stream(systems).mapToDouble(s -> s.lToH(1.0)).max().orElseThrow();

      PointValuePair find = Simplex.optimizeCMAES(p -> {
            double h = p[0];
            double k = p[1];
            double[] subLogApparentPredicted = subtractSystems(systems.length, i -> diff.applyAsDouble(i, logApparentPredictedFunction.apply(k, h)));
            return Inequality.absolute().applyAsDouble(logApparent, subLogApparentPredicted);
          },
          new SimpleBounds(new double[] {0.0, -1.0}, new double[] {maxL, 1.0}),
          new double[] {maxL / 10.0, 0.0}, new double[] {maxL / 10.0, 0.1}
      );

      double h = find.getPoint()[0];
      double k = find.getPoint()[1];

      double sumLogApparent = sumLog(systems, logApparentFunction(systems, rOhms));
      double sumLogApparentPredicted = sumLog(systems, index -> logApparentPredictedFunction.apply(k, h).applyAsDouble(index));
      double rho1 = exp((sumLogApparent - sumLogApparentPredicted) / systems.length);
      double rho2 = rho1 / Layers.getRho1ToRho2(k);
      return new Medium.Builder(systems, rOhms, s -> new Resistance2Layer(s).value(rho1, rho2, h)).addLayer(rho1, h).build(rho2);
    }
    else {
      return Resistance1Layer.inverseStatic(systems, rOhms);
    }
  }

  @Nonnull
  @ParametersAreNonnullByDefault
  public static Medium inverseDynamic(TetrapolarSystem[] systems, double[] rOhmsBefore, double[] rOhmsAfter, double dh) {
    IntToDoubleFunction rDiff = index -> (rOhmsAfter[index] - rOhmsBefore[index]) / dh;

    Supplier<IntToDoubleFunction> apparentDiffByH = () ->
        index -> log(Math.abs(new Resistance1Layer(systems[index]).getApparent(rDiff.applyAsDouble(index))));

    if (Arrays.stream(rangeSystems(systems.length, index -> apparentDiffByH.get().applyAsDouble(index))).anyMatch(Double::isInfinite)) {
      return Resistance1Layer.inverseStatic(systems, rOhmsBefore);
    }

    IntToDoubleFunction logApparentFunction = index -> log(new Resistance1Layer(systems[index]).getApparent(rOhmsBefore[index]));
    double[] logApparent = rangeSystems(systems.length, logApparentFunction);

    Function<double[], IntToDoubleFunction> logApparentPredictedFunction = hk ->
        index -> new Log1pApparent2Rho(systems[index]).value(hk[1], hk[0]);

    UnaryOperator<double[]> diffPredictedFunction =
        hk -> rangeSystems(systems.length, index -> new DerivativeApparent2Rho(systems[index]).value(hk[1], hk[0]));

    BiPredicate<Integer, Double> sign =
        (index, diffPredicted) -> Double.compare(Math.signum(rDiff.applyAsDouble(index)), Math.signum(diffPredicted)) == 0;

    ToLongFunction<PointValuePair> countSigns =
        point -> {
          double[] diffPredicted = diffPredictedFunction.apply(point.getPoint());
          return IntStream.range(0, systems.length)
              .mapToObj(index -> sign.test(index, diffPredicted[index]))
              .filter(Boolean::booleanValue).count();
        };

    double maxL = Arrays.stream(systems).mapToDouble(s -> s.lToH(1.0)).max().orElseThrow();
    PointValuePair find = IntStream.range(0, 2)
        .mapToObj(i -> {
          boolean b1 = (i & 1) == 0;
          return new SimpleBounds(new double[] {0.0, b1 ? 0.0 : -1.0}, new double[] {maxL, b1 ? 1.0 : 0.0});
        })
        .map(bounds -> {
          PointValuePair pair = Simplex.optimize(p -> {
                double[] logDiff = rangeSystems(systems.length, apparentDiffByH.get());
                double[] measured = rangeSystems(systems.length, index -> logApparent[index] - logDiff[index]);

                double[] logApparentPredicted = rangeSystems(systems.length,
                    index -> logApparentPredictedFunction.apply(p).applyAsDouble(index)
                );
                double[] diffPredicted = diffPredictedFunction.apply(p);
                double[] predicted = rangeSystems(systems.length, index -> {
                  double result = logApparentPredicted[index] - log(Math.abs(diffPredicted[index]));
                  if (!sign.test(index, diffPredicted[index])) {
                    result *= -1.0;
                  }
                  return result;
                });
                return Inequality.absolute().applyAsDouble(measured, predicted);
              },
              bounds
          );
          Logger.getLogger(Resistance2Layer.class.getName()).config(
              () -> {
                double[] v = pair.getPoint();
                return String.format("k = %.2f; h = %.2f mm; signs = %d; e = %.6f",
                    v[1], Metrics.toMilli(v[0]), countSigns.applyAsLong(pair), pair.getValue());
              }
          );
          return pair;
        })
        .min(Comparator.comparingLong(countSigns).reversed().thenComparingDouble(Pair::getValue)).orElseThrow();

    double sumLogApparent = sumLog(systems, logApparentFunction);
    double sumLogApparentPredicted = sumLog(systems, index -> logApparentPredictedFunction.apply(find.getPoint()).applyAsDouble(index));

    double h = find.getPoint()[0];
    double k = find.getPoint()[1];

    double rho1 = exp((sumLogApparent - sumLogApparentPredicted) / systems.length);
    double rho2 = rho1 / Layers.getRho1ToRho2(k);
    return new Medium.Builder(systems, rOhmsBefore, rOhmsAfter, dh,
        (s, dH) -> new Resistance2Layer(s).value(rho1, rho2, h + dH))
        .addLayer(rho1, h).build(rho2);
  }

  static double sumLog(@Nonnull TetrapolarSystem[] systems, @Nonnull IntToDoubleFunction logApparentPredictedFunction) {
    return IntStream.range(0, systems.length).mapToDouble(logApparentPredictedFunction).reduce(Double::sum).orElseThrow();
  }

  static double[] rangeSystems(@Nonnegative int length, @Nonnull IntToDoubleFunction mapper) {
    return IntStream.range(0, length).mapToDouble(mapper).toArray();
  }

  private static ToDoubleBiFunction<Integer, IntToDoubleFunction> newDiff(@Nonnegative int length) {
    return (i, toDouble) -> toDouble.applyAsDouble(i) - toDouble.applyAsDouble((i + 1) % length);
  }

  private static IntToDoubleFunction logApparentFunction(@Nonnull TetrapolarSystem[] systems, @Nonnull double[] rOhms) {
    return index -> log(new Resistance1Layer(systems[index]).getApparent(rOhms[index]));
  }

  private static BiFunction<Double, Double, IntToDoubleFunction> logApparentPredictedFunction(@Nonnull TetrapolarSystem[] systems) {
    return (k, h) -> index -> new Log1pApparent2Rho(systems[index]).value(k, h);
  }

  private static double[] subtractSystems(@Nonnegative int length, @Nonnull IntToDoubleFunction mapper) {
    return IntStream.range(0, length).mapToDouble(mapper).limit(length - 1L).toArray();
  }

  private static double[] logApparent(@Nonnull TetrapolarSystem[] systems, @Nonnull double[] rOhmsBefore) {
    ToDoubleBiFunction<Integer, IntToDoubleFunction> diff = newDiff(systems.length);
    IntToDoubleFunction logApparentFunction = logApparentFunction(systems, rOhmsBefore);
    return subtractSystems(systems.length, i -> diff.applyAsDouble(i, logApparentFunction));
  }
}
