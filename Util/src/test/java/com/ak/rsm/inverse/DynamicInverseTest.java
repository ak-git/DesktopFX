package com.ak.rsm.inverse;

import java.util.function.ToDoubleFunction;
import java.util.stream.Stream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.rsm.measurement.TetrapolarDerivativeMeasurement;
import com.ak.rsm.system.Layers;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.byLessThan;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class DynamicInverseTest {
  static Stream<Arguments> kw() {
    return Stream.of(
        arguments(new double[] {Layers.getK12(9.0, 1.0), 2.0 / 18.0,}, 0.0),
        arguments(new double[] {Layers.getK12(1.585601, 1.753970), 6.831488 / 18.0}, 6.859),
        arguments(new double[] {Layers.getK12(0.246123, 1.902301), 0.345480 / 18.0}, 6.782),
        arguments(new double[] {Layers.getK12(1.323014, 2.406517), 4.647438 / 18.0}, 4.833)
    );
  }

  @ParameterizedTest
  @MethodSource("kw")
  void test(@Nonnull double[] kw, @Nonnegative double inequality) {
    ToDoubleFunction<double[]> function = DynamicInverse.of(
        TetrapolarDerivativeMeasurement.milli(0.1).dh(0.105).system2(6.0).rho1(9.0).rho2(1.0).h(2.0)
    );
    assertThat(function.applyAsDouble(kw)).isCloseTo(inequality, byLessThan(1.0e-3));
  }
}