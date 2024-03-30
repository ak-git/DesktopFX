package com.ak.rsm.inverse;

import com.ak.csv.CSVLineFileCollector;
import com.ak.math.Simplex;
import com.ak.math.ValuePair;
import com.ak.rsm.apparent.Apparent3Rho;
import com.ak.rsm.measurement.DerivativeMeasurement;
import com.ak.rsm.measurement.TetrapolarDerivativeMeasurement;
import com.ak.rsm.medium.Layer2Medium;
import com.ak.rsm.resistance.DerivativeResistivity;
import com.ak.rsm.system.InexactTetrapolarSystem;
import com.ak.rsm.system.Layers;
import com.ak.rsm.system.TetrapolarSystem;
import com.ak.util.Extension;
import com.ak.util.Metrics;
import com.ak.util.Strings;
import org.apache.commons.math3.optim.PointValuePair;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tec.uom.se.unit.MetricPrefix;

import javax.annotation.Nonnegative;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;
import static tec.uom.se.unit.Units.METRE;

class Inverse3Test {
  private static final Logger LOGGER = Logger.getLogger(Inverse3Test.class.getName());

  static Stream<Arguments> model() {
    double rho1 = 2.0;
    double rho2 = 10.0;
    double rho3 = 5.0;
    double hmmStep = 0.205;
    double smmBase = 8.0;
    int p1 = (int) (2.05 / hmmStep);
    int p2mp1 = (int) (4.3 / hmmStep);

    LOGGER.info(() -> "p1 = %d; p2mp1 = %d".formatted(p1, p2mp1));

    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(-hmmStep * 3.0)
                    .system4(smmBase).rho1(rho1).rho2(rho2).rho3(rho3).hStep(hmmStep).p(p1, p2mp1),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(hmmStep)
                    .system4(smmBase).rho1(rho1).rho2(rho2).rho3(rho3).hStep(hmmStep).p(p1, p2mp1),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(hmmStep * 2.0)
                    .system4(smmBase).rho1(rho1).rho2(rho2).rho3(rho3).hStep(hmmStep).p(p1, p2mp1)
            )
        )
    );
  }

  @ParameterizedTest
  @MethodSource({
      "com.ak.rsm.inverse.InverseTestE7694akProvider#e17_52_54_s4",
      "model"
  })
  @Disabled("ignored com.ak.rsm.inverse.Inverse3Test.testGroup")
  void testGroup(Collection<Collection<DerivativeMeasurement>> ms) throws IOException {
    Function<Collection<DerivativeMeasurement>, Double> findDh =
        dm -> dm.stream().mapToDouble(DerivativeResistivity::dh).summaryStatistics().getAverage();
    Collection<DerivativeMeasurement> derivativeMeasurements = ms.stream()
        .collect(Collectors.toMap(Function.identity(), findDh))
        .entrySet().stream().filter(dh -> dh.getValue() > 0).min(Map.Entry.comparingByValue()).orElseThrow().getKey();
    Assertions.assertThat(derivativeMeasurements).isNotEmpty();
    testSingle(derivativeMeasurements);
  }

  private static void testSingle(Collection<? extends DerivativeMeasurement> dm) throws IOException {
    Function<Collection<InexactTetrapolarSystem>, Regularization> regularizationFunction = Regularization.Interval.ZERO_MAX.of(10.0);
    LOGGER.info(regularizationFunction::toString);
    Layer2Medium layer2Medium = DynamicAbsolute.LAYER_2.apply(dm, regularizationFunction);
    LOGGER.info(layer2Medium::toString);
    ValuePair h = layer2Medium.h();
    LOGGER.info(h::toString);

    Map<ValuePair.Name, String> outMap = new EnumMap<>(
        Stream.of(ValuePair.Name.H1, ValuePair.Name.H2, ValuePair.Name.DH2,
                ValuePair.Name.RHO_1, ValuePair.Name.RHO_2, ValuePair.Name.RHO_3,
                ValuePair.Name.K12, ValuePair.Name.K23).
            collect(Collectors.toMap(Function.identity(), v -> Strings.EMPTY))
    );

    try (CSVLineFileCollector csvCollector = new CSVLineFileCollector(
        Path.of(Extension.CSV.attachTo(Inverse3Test.class.getSimpleName())),
        outMap.keySet().stream().map(Enum::name).toArray(String[]::new))
    ) {
      int SCALE = 100;
      double hStep = dm.stream().flatMapToDouble(m -> DoubleStream.of(Math.abs(m.dh()))).min().orElseThrow() / SCALE;
      LOGGER.info(() -> "3-layer step %s".formatted(ValuePair.Name.H.of(hStep, 0.0)));
      Assertions.assertThat(
          IntStream.range(2, 3).mapToObj(i -> new int[] {i * SCALE})
              .flatMap(i ->
                  IntStream.iterate(1, j -> j + 1).mapToObj(j -> new int[] {i[0], j * SCALE})
                      .takeWhile(ints -> {
                            int p1 = ints[0];
                            int p2mp1 = ints[1];
                            ValuePair h1 = ValuePair.Name.H1.of(hStep * p1, 0.0);
                            ValuePair h2 = ValuePair.Name.H2.of(hStep * (p1 + p2mp1), 0.0);
                            ValuePair dh2 = ValuePair.Name.DH2.of(hStep * p2mp1, 0.0);

                            Stream.of(h1, h2, dh2).forEach(valuePair -> outMap.put(
                                    valuePair.name(),
                                    String.format(Locale.ROOT,
                                        "%.3f".formatted(Metrics.Length.METRE.to(valuePair.value(), MetricPrefix.MILLI(METRE)))
                                    )
                                )
                            );

                            LOGGER.info(() -> Stream.of(h1, h2, dh2).map(ValuePair::toString).collect(Collectors.joining("; ")));
                            return h2.value() < h.value() + hStep;
                          }
                      )
              )
              .map(ints -> {
                double[] p = Arrays.stream(ints).mapToDouble(value -> value).toArray();
                PointValuePair optimized = Simplex.optimizeAll(
                    k -> DynamicInverse.of(dm, hStep).applyAsDouble(DoubleStream.concat(Arrays.stream(k), Arrays.stream(p)).toArray()),
                    new Simplex.Bounds(0.0, 1.0), new Simplex.Bounds(-1.0, 0.0)
                );
                return new PointValuePair(
                    DoubleStream.concat(Arrays.stream(optimized.getPoint()), Arrays.stream(p)).toArray(), optimized.getValue()
                );
              })
              .map(optimized -> {
                double[] kpp = optimized.getPoint();
                var rho1 = getRho1(dm, kpp, hStep);
                var rho2 = ValuePair.Name.RHO_2.of(rho1.value() / Layers.getRho1ToRho2(kpp[0]), 0.0);
                var rho3 = ValuePair.Name.RHO_3.of(rho2.value() / Layers.getRho1ToRho2(kpp[1]), 0.0);
                ValuePair k12 = ValuePair.Name.K12.of(kpp[0], 0.0);
                ValuePair k23 = ValuePair.Name.K23.of(kpp[1], 0.0);

                LOGGER.info(() -> "%.6f %s; %s; %s; %s; %s".formatted(optimized.getValue(), k12, k23, rho1, rho2, rho3));

                Stream.of(rho1, rho2, rho3, k12, k23).forEach(valuePair -> outMap.put(
                        valuePair.name(),
                        String.format(Locale.ROOT,
                            "%f".formatted(valuePair.value())
                        )
                    )
                );

                return outMap.values().toArray();
              })
              .collect(csvCollector)).isTrue();
    }
  }

  private static ValuePair getRho1(Collection<? extends DerivativeMeasurement> measurements, double[] kw, @Nonnegative double hStep) {
    return measurements.stream()
        .map(measurement -> {
          TetrapolarSystem s = measurement.system();
          double normApparent = Apparent3Rho.newApparentDivRho1(s.relativeSystem())
              .value(
                  kw[0], kw[1],
                  hStep / s.lCC(),
                  (int) Math.round(kw[2]), (int) Math.round(kw[3])
              );
          return ValuePair.Name.RHO_1.of(measurement.resistivity() / normApparent, 0.0);
        })
        .reduce(ValuePair::mergeWith).orElseThrow();
  }
}
