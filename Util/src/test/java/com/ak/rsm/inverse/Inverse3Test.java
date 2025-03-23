package com.ak.rsm.inverse;

import com.ak.csv.CSVLineFileBuilder;
import com.ak.math.Simplex;
import com.ak.math.ValuePair;
import com.ak.rsm.apparent.Apparent3Rho;
import com.ak.rsm.measurement.DerivativeMeasurement;
import com.ak.rsm.measurement.TetrapolarDerivativeMeasurement;
import com.ak.rsm.medium.Layer2Medium;
import com.ak.rsm.resistance.DeltaH;
import com.ak.rsm.resistance.DerivativeResistivity;
import com.ak.rsm.system.InexactTetrapolarSystem;
import com.ak.rsm.system.Layers;
import com.ak.rsm.system.TetrapolarSystem;
import com.ak.util.Numbers;
import com.ak.util.Strings;
import org.apache.commons.math3.optim.PointValuePair;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnegative;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class Inverse3Test {
  private static final Logger LOGGER = LoggerFactory.getLogger(Inverse3Test.class);

  static Stream<Arguments> model() {
    double rho1 = 2.0;
    double rho2 = 10.0;
    double rho3 = 5.0;
    double hmmStep = 0.1;
    double smmBase = 10.0;
    int p1 = (int) (2.0 / hmmStep);
    int p2mp1 = (int) (4.0 / hmmStep);

    LOGGER.info("p1 = {}; p2mp1 = {}", p1, p2mp1);

    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.milli(0.1).dh(DeltaH.H1.apply(-hmmStep * 3.0))
                    .system4(smmBase).rho1(rho1).rho2(rho2).rho3(rho3).hStep(hmmStep).p(p1, p2mp1),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(DeltaH.H1.apply(hmmStep))
                    .system4(smmBase).rho1(rho1).rho2(rho2).rho3(rho3).hStep(hmmStep).p(p1, p2mp1),
                TetrapolarDerivativeMeasurement.milli(0.1).dh(DeltaH.H1.apply(hmmStep * 2.0))
                    .system4(smmBase).rho1(rho1).rho2(rho2).rho3(rho3).hStep(hmmStep).p(p1, p2mp1)
            )
        )
    );
  }

  @ParameterizedTest
  @MethodSource({
      "com.ak.rsm.inverse.InverseTestE7694akProvider#e17_52_54_s4",
      "com.ak.rsm.inverse.InverseTestE7694akProvider#e17_55_32_s4",
      "model"
  })
  @Disabled("ignored com.ak.rsm.inverse.Inverse3Test.testGroup")
  void testGroup(Collection<Collection<DerivativeMeasurement>> ms) {
    testSingle(filter(ms));
  }

  private static Collection<DerivativeMeasurement> filter(Collection<Collection<DerivativeMeasurement>> ms) {
    Function<Collection<DerivativeMeasurement>, Double> findDh =
        dm -> dm.stream().mapToDouble(DerivativeResistivity::dh).summaryStatistics().getAverage();
    Collection<DerivativeMeasurement> derivativeMeasurements = ms.stream()
        .collect(Collectors.toMap(Function.identity(), findDh))
        .entrySet().stream().filter(dh -> Math.abs(dh.getValue()) > 0).min(Map.Entry.comparingByValue()).orElseThrow().getKey();
    assertThat(derivativeMeasurements).hasSizeGreaterThanOrEqualTo(2);
    return derivativeMeasurements.stream().limit(2).toList();
  }

  private static void testSingle(Collection<? extends DerivativeMeasurement> dm) {
    Regularization.Interval regularization = Regularization.Interval.ZERO_MAX_LOG1P;
    Function<Collection<InexactTetrapolarSystem>, Regularization> regularizationFunction = regularization.of(1.0);
    Layer2Medium layer2Medium = DynamicAbsolute.ofLayer2(dm, regularizationFunction);
    ValuePair h = layer2Medium.h();
    LOGGER.info("{}\n{}", regularizationFunction, layer2Medium);

    assertThatNoException().isThrownBy(() -> {
      int scale = 2;
      double hStep = dm.stream().flatMapToDouble(m -> DoubleStream.of(Math.abs(m.dh()))).min().orElseThrow() / scale;
      LOGGER.info("3-layer step {}", ValuePair.Name.H.of(hStep, 0.0));

          ValuePair.Name[] keys = {
              ValuePair.Name.RHO_1, ValuePair.Name.RHO_2, ValuePair.Name.RHO_3,
              ValuePair.Name.K12, ValuePair.Name.K23, ValuePair.Name.ERR
          };

          CSVLineFileBuilder<ValuePair[]> csvBuilder = CSVLineFileBuilder
              .of((vdh2, vh1) -> {
                ValuePair h1 = ValuePair.Name.H1.of(vh1, 0.0);
                ValuePair h2 = ValuePair.Name.H2.of(vh1 + vdh2, 0.0);
                ValuePair dh2 = ValuePair.Name.DH2.of(vdh2, 0.0);

                double[] p = Stream.of(h1, dh2).mapToDouble(x -> x.value() / hStep).toArray();

                PointValuePair optimizedK = Simplex.optimizeAll(
                    k -> DynamicInverse.ofH1Changed(dm, hStep).applyAsDouble(DoubleStream.concat(Arrays.stream(k), Arrays.stream(p)).toArray()),
                    new Simplex.Bounds(0.0, 1.0), new Simplex.Bounds(-1.0, 0.0)
                );

                PointValuePair optimized = new PointValuePair(
                    DoubleStream.concat(Arrays.stream(optimizedK.getPoint()), Arrays.stream(p)).toArray(), optimizedK.getValue()
                );

                double[] kpp = optimized.getPoint();
                var rho1 = getRho1(dm, kpp, hStep);
                var rho2 = ValuePair.Name.RHO_2.of(rho1.value() / Layers.getRho1ToRho2(kpp[0]), 0.0);
                var rho3 = ValuePair.Name.RHO_3.of(rho2.value() / Layers.getRho1ToRho2(kpp[1]), 0.0);
                ValuePair k12 = ValuePair.Name.K12.of(kpp[0], 0.0);
                ValuePair k23 = ValuePair.Name.K23.of(kpp[1], 0.0);
                ValuePair err = ValuePair.Name.ERR.of(optimized.getValue(), 0.0);

                LOGGER.info(
                    Stream.of(h1, dh2, h2, err, k12, k23, rho1, rho2, rho3)
                        .map(ValuePair::toString).collect(Collectors.joining(Strings.SEMICOLON))
                );
                return new ValuePair[] {rho1, rho2, rho3, k12, k23, err};
              })
              .xStream(() -> DoubleStream.iterate(hStep, x -> x < h.value() * 2, x -> x * 2))
              .yStream(() -> DoubleStream.iterate(hStep, x -> x < h.value() * 2, x -> x * 2));

          for (int i = 0; i < keys.length; i++) {
            int finalI = i;
            csvBuilder.saveTo(keys[i].name(), result -> {
              assertThat(result[finalI].name()).isEqualTo(keys[finalI]);
              return result[finalI].value();
            });
          }
          csvBuilder.generate();
        }
    );
  }

  private static ValuePair getRho1(Collection<? extends DerivativeMeasurement> measurements, double[] kw, @Nonnegative double hStep) {
    return measurements.stream()
        .map(measurement -> {
          TetrapolarSystem s = measurement.system();
          double normApparent = Apparent3Rho.newApparentDivRho1(s.relativeSystem())
              .value(
                  kw[0], kw[1],
                  hStep / s.lCC(),
                  Numbers.toInt(kw[2]), Numbers.toInt(kw[3])
              );
          return ValuePair.Name.RHO_1.of(measurement.resistivity() / normApparent, 0.0);
        })
        .reduce(ValuePair::mergeWith).orElseThrow();
  }
}
