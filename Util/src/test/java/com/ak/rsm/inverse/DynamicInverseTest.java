package com.ak.rsm.inverse;

import com.ak.rsm.measurement.TetrapolarDerivativeMeasurement;
import com.ak.rsm.resistance.DeltaH;
import com.ak.rsm.system.Layers;
import com.ak.util.Metrics;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.function.ToDoubleFunction;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.byLessThan;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static tech.units.indriya.unit.Units.METRE;

class DynamicInverseTest {
  static Stream<Arguments> layer2() {
    return Stream.of(
        arguments(new double[] {Layers.getK12(9.0, 1.0), 2.0 / 18.0}, 0.0),
        arguments(new double[] {Layers.getK12(1.585601, 1.753970), 6.831488 / 18.0}, Double.NaN),
        arguments(new double[] {Layers.getK12(0.246123, 1.902301), 0.345480 / 18.0}, Double.NaN),
        arguments(new double[] {Layers.getK12(1.323014, 2.406517), 4.647438 / 18.0}, Double.NaN)
    );
  }

  @ParameterizedTest
  @MethodSource("layer2")
  void testLayer2(double[] kw, double inequality) {
    ToDoubleFunction<double[]> function = DynamicInverse.of(
        TetrapolarDerivativeMeasurement.milli(0.1).dh(DeltaH.H1.apply(0.105)).system2(6.0)
            .rho1(9.0).rho2(1.0).h(2.0)
    );
    assertThat(function.applyAsDouble(kw)).isCloseTo(inequality, byLessThan(1.0e-3));
  }

  static Stream<Arguments> layer3() {
    return Stream.of(
        arguments(new double[] {Layers.getK12(9.0, 1.0), Layers.getK12(1.0, 4.0), 50, 50}, 0.0)
    );
  }

  @ParameterizedTest
  @MethodSource("layer3")
  void testLayer3(double[] k, double inequality) {
    Assertions.assertAll(
        () -> {
          ToDoubleFunction<double[]> function = DynamicInverse.ofH1Changed(
              TetrapolarDerivativeMeasurement.milli(0.0).dh(DeltaH.H1.apply(0.1)).system2(6.0)
                  .rho1(9.0).rho2(1.0).rho3(4.0).hStep(0.01).p(50, 50),
              Metrics.Length.MILLI.to(0.01, METRE)
          );
          assertThat(function.applyAsDouble(k)).isCloseTo(inequality, byLessThan(1.0e-3));
        },
        () -> {
          ToDoubleFunction<double[]> function = DynamicInverse.ofH2Changed(
              TetrapolarDerivativeMeasurement.milli(0.0).dh(DeltaH.H2.apply(0.1)).system2(6.0)
                  .rho1(9.0).rho2(1.0).rho3(4.0).hStep(0.01).p(50, 50),
              Metrics.Length.MILLI.to(0.01, METRE)
          );
          assertThat(function.applyAsDouble(k)).isCloseTo(inequality, byLessThan(1.0e-3));
        },
        () -> {
          ToDoubleFunction<double[]> function = DynamicInverse.ofH1H2Changed(
              TetrapolarDerivativeMeasurement.milli(0.0).dh(DeltaH.H1_H2.apply(0.1)).system2(6.0)
                  .rho1(9.0).rho2(1.0).rho3(4.0).hStep(0.01).p(50, 50),
              Metrics.Length.MILLI.to(0.01, METRE)
          );
          assertThat(function.applyAsDouble(k)).isCloseTo(inequality, byLessThan(1.0e-3));
        }
    );
  }
}