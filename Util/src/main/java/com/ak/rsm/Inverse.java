package com.ak.rsm;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.ToDoubleBiFunction;
import java.util.function.ToDoubleFunction;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.inverse.Inequality;
import com.ak.math.Simplex;
import com.ak.math.ValuePair;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleBounds;

import static com.ak.rsm.RelativeMediumLayers.SINGLE_LAYER;
import static java.lang.StrictMath.exp;

enum Inverse {
  ;

  private static final ToDoubleBiFunction<? super TetrapolarSystem, RelativeMediumLayers<Double>> LOG_APPARENT_PREDICTED =
      (s, kh) -> new Log1pApparent2Rho(s.toRelative()).value(kh.k12(), kh.h() / s.getL());

  private static final ToDoubleBiFunction<? super TetrapolarSystem, RelativeMediumLayers<Double>> LOG_DIFF_APPARENT_PREDICTED =
      (s, kh) -> StrictMath.log(Math.abs(new DerivativeApparent2Rho(s).value(kh.k12(), kh.h() / s.getL())));

  @Nonnull
  static MediumLayers<ValuePair> inverseStatic(@Nonnull Collection<Measurement> measurements) {
    if (measurements.size() > 2) {
      Function<Collection<Measurement>, Layer2Medium<Double>> layer2MediumFunction =
          ms -> {
            RelativeMediumLayers<Double> kh = inverseStaticRelative(ms, values -> {
              double[] sub = new double[values.length - 1];
              for (int i = 0; i < sub.length; i++) {
                sub[i] = values[i + 1] - values[i];
              }
              return sub;
            });

            double rho1 = getRho1(ms, kh);
            return new Layer2Medium.DoubleLayer2MediumBuilder(
                ms.stream().map(m -> new TetrapolarPrediction(m, kh, rho1)).collect(Collectors.toUnmodifiableList()))
                .layer1(rho1, kh.h()).k12(kh.k12()).build();
          };

      return getPairLayer2Medium(measurements, layer2MediumFunction, Measurement::newInstance);
    }
    else {
      Measurement average = measurements.stream().reduce(Measurement::merge).orElseThrow();
      return new Layer1Medium.Layer1MediumBuilder(
          measurements.stream()
              .map(m -> new TetrapolarPrediction(m, SINGLE_LAYER, average.getResistivity()))
              .collect(Collectors.toUnmodifiableList()))
          .layer1(average).build();
    }
  }

  @Nonnull
  static MediumLayers<ValuePair> inverseDynamic(@Nonnull Collection<DerivativeMeasurement> measurements) {
    if (measurements.size() > 1) {
      RelativeMediumLayers<Double> initial = new RelativeMediumLayers<>() {
        @Override
        public Double k12() {
          return measurements.stream().allMatch(d -> d.getDerivativeResistivity() > 0) ? -1.0 : 1.0;
        }

        @Override
        public Double h() {
          return getMaxHToL(measurements);
        }
      };

      Function<Collection<DerivativeMeasurement>, Layer2Medium<Double>> layer2MediumFunction =
          ms -> {
            RelativeMediumLayers<Double> kh = inverseDynamicRelative(ms, initial);
            double rho1 = getRho1(ms, kh);
            return new Layer2Medium.DoubleLayer2MediumBuilder(
                ms.stream().map(m -> TetrapolarDerivativePrediction.of(m, kh, rho1)).collect(Collectors.toUnmodifiableList()))
                .layer1(rho1, kh.h()).k12(kh.k12()).build();
          };

      return getPairLayer2Medium(measurements, layer2MediumFunction, DerivativeMeasurement::newInstance);
    }
    else {
      return inverseStatic(measurements.stream().collect(Collectors.<Measurement>toUnmodifiableList()));
    }
  }

  @Nonnull
  @ParametersAreNonnullByDefault
  static <T extends Measurement> Layer2Medium<ValuePair> getPairLayer2Medium(
      Collection<T> measurements,
      Function<Collection<T>, Layer2Medium<Double>> layer2MediumFunction,
      BiFunction<T, InexactTetrapolarSystem, T> newInstanceMeasurement) {

    Layer2Medium<Double> center = layer2MediumFunction.apply(measurements);
    return Measurement.getMeasurementsCombination(measurements, newInstanceMeasurement)
        .stream()
        .map(layer2MediumFunction)
        .map(m -> {
          Function<ToDoubleFunction<Layer2Medium<Double>>, ValuePair> getVar =
              f -> {
                double c = f.applyAsDouble(center);
                return new ValuePair(c, Inequality.absolute().applyAsDouble(f.applyAsDouble(m), c));
              };
          return new Layer2Medium.Layer2MediumBuilder(center.getPredictions())
              .layer1(getVar.apply(MediumLayers::rho1), getVar.apply(MediumLayers::h))
              .layer2(getVar.apply(MediumLayers::rho2))
              .k12(getVar.apply(MediumLayers::k12))
              .build();
        })
        .parallel()
        .reduce((m1, m2) -> {
          ToDoubleFunction<Function<Layer2Medium<ValuePair>, ValuePair>> getVar =
              f -> Math.max(f.apply(m1).getAbsError(), f.apply(m2).getAbsError());
          return new Layer2Medium.Layer2MediumBuilder(center.getPredictions())
              .layer1(
                  new ValuePair(center.rho1(), getVar.applyAsDouble(MediumLayers::rho1)),
                  new ValuePair(center.h(), getVar.applyAsDouble(Layer2Medium::h))
              )
              .layer2(new ValuePair(center.rho2(), getVar.applyAsDouble(MediumLayers::rho2)))
              .k12(new ValuePair(center.k12(), getVar.applyAsDouble(MediumLayers::k12)))
              .build();
        })
        .orElseThrow();
  }

  @Nonnull
  @ParametersAreNonnullByDefault
  static RelativeMediumLayers<Double> inverseDynamicRelative(Collection<? extends DerivativeMeasurement> derivativeMeasurements,
                                                             RelativeMediumLayers<Double> initialRelative) {
    double[] kMinMax = {-1.0, 1.0};
    if (initialRelative.k12() > 0.0) {
      kMinMax[0] = 0.0;
    }
    else if (initialRelative.k12() < 0.0) {
      kMinMax[1] = 0.0;
    }

    double[] subLog = derivativeMeasurements.stream().mapToDouble(d -> d.getLogResistivity() - d.getDerivativeLogResistivity()).toArray();
    Function<double[], RelativeMediumLayers<Double>> layersFunction = newLayerFunction(derivativeMeasurements);

    Function<TetrapolarSystem[], PointValuePair> find =
        systems -> Simplex.optimize(kw -> {
              double[] subLogPredicted =
                  Arrays.stream(systems)
                      .mapToDouble(s -> {
                        RelativeMediumLayers<Double> kh = layersFunction.apply(kw);
                        return LOG_APPARENT_PREDICTED.applyAsDouble(s, kh) - LOG_DIFF_APPARENT_PREDICTED.applyAsDouble(s, kh);
                      })
                      .toArray();
              return Inequality.absolute().applyAsDouble(subLog, subLogPredicted);
            },
            new SimpleBounds(new double[] {kMinMax[0], 0.0}, new double[] {kMinMax[1], Double.POSITIVE_INFINITY}),
            new double[] {initialRelative.k12(), initialRelative.h()}, new double[] {0.01 * Math.signum(initialRelative.k12()), 0.01}
        );

    PointValuePair kwOptimal = find.apply(
        derivativeMeasurements.stream().map(m -> m.getSystem().toExact()).toArray(TetrapolarSystem[]::new)
    );

    RelativeMediumLayers<Double> layers = layersFunction.apply(kwOptimal.getPoint());
    return new Layer2RelativeMedium<>(layers.k12(), layers.h());
  }

  @Nonnull
  @ParametersAreNonnullByDefault
  static RelativeMediumLayers<Double> inverseStaticRelative(Collection<? extends Measurement> measurements,
                                                            UnaryOperator<double[]> subtract) {
    double[] subLogApparent = subtract.apply(measurements.stream().mapToDouble(Measurement::getLogResistivity).toArray());
    Function<double[], RelativeMediumLayers<Double>> layersFunction = newLayerFunction(measurements);
    double maxHToL = getMaxHToL(measurements);
    PointValuePair kwOptimal = Simplex.optimizeCMAES(kw -> {
          double[] subLogApparentPredicted = subtract.apply(measurements.stream()
              .map(Measurement::getSystem)
              .mapToDouble(s -> LOG_APPARENT_PREDICTED.applyAsDouble(s.toExact(), layersFunction.apply(kw)))
              .toArray()
          );
          return Inequality.absolute().applyAsDouble(subLogApparent, subLogApparentPredicted);
        },
        new SimpleBounds(new double[] {-1.0, 0.0}, new double[] {1.0, maxHToL}),
        new double[] {0.0, maxHToL / 10.0}, new double[] {0.01, maxHToL / 100.0}
    );
    RelativeMediumLayers<Double> layers = layersFunction.apply(kwOptimal.getPoint());
    return new Layer2RelativeMedium<>(layers.k12(), layers.h());
  }

  @Nonnegative
  @ParametersAreNonnullByDefault
  private static double getRho1(Collection<? extends Measurement> measurements, RelativeMediumLayers<Double> kh) {
    double sumLogApparent = measurements.stream().mapToDouble(Measurement::getLogResistivity).sum();
    double sumLogApparentPredicted = measurements.stream()
        .map(Measurement::getSystem)
        .mapToDouble(s -> LOG_APPARENT_PREDICTED.applyAsDouble(s.toExact(), kh)).sum();
    return exp((sumLogApparent - sumLogApparentPredicted) / measurements.size());
  }

  @Nonnegative
  private static double getMaxHToL(@Nonnull Collection<? extends Measurement> measurements) {
    return measurements.parallelStream()
        .mapToDouble(measurement -> measurement.getSystem().getHMax(1.0)).min().orElseThrow() / getBaseL(measurements);
  }

  @Nonnull
  private static Function<double[], RelativeMediumLayers<Double>> newLayerFunction(@Nonnull Collection<? extends Measurement> measurements) {
    double baseL = getBaseL(measurements);
    return kw -> new Layer2RelativeMedium<>(kw[0], kw[1] * baseL);
  }

  @Nonnegative
  private static double getBaseL(@Nonnull Collection<? extends Measurement> measurements) {
    return measurements.parallelStream().mapToDouble(m -> m.getSystem().toExact().getL()).max().orElseThrow();
  }
}
