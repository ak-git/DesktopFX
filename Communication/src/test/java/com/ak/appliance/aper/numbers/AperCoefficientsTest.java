package com.ak.appliance.aper.numbers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class AperCoefficientsTest {
  @Test
  void testCoefficients() {
    assertThat(AperCoefficients.values()).hasSize(1);
    assertThat(AperSurfaceCoefficientsChannel1.values()).hasSize(5);
    assertThat(AperSurfaceCoefficientsChannel2.values()).hasSize(5);
  }

  static Stream<Arguments> aperCoefficients() {
    return Stream.of(
        arguments(AperCoefficients.ADC_TO_OHM, 18),
        arguments(AperSurfaceCoefficientsChannel1.CCU_VADC_0, 4),
        arguments(AperSurfaceCoefficientsChannel1.CCU_VADC_15148, 16),
        arguments(AperSurfaceCoefficientsChannel1.CCU_VADC_30129, 14),
        arguments(AperSurfaceCoefficientsChannel1.CCU_VADC_90333, 12),
        arguments(AperSurfaceCoefficientsChannel1.CCU_VADC_330990, 10),
        arguments(AperSurfaceCoefficientsChannel2.CCU_VADC_0, 4),
        arguments(AperSurfaceCoefficientsChannel2.CCU_VADC_15148, 16),
        arguments(AperSurfaceCoefficientsChannel2.CCU_VADC_30129, 14),
        arguments(AperSurfaceCoefficientsChannel2.CCU_VADC_90333, 12),
        arguments(AperSurfaceCoefficientsChannel2.CCU_VADC_330990, 10)
    );
  }

  @ParameterizedTest
  @MethodSource("aperCoefficients")
  void testCoefficients(Supplier<double[]> coefficients, int count) {
    assertThat(coefficients.get()).hasSize(count);
  }
}