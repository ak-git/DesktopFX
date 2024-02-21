package com.ak.numbers.rcm;

import com.ak.numbers.Interpolators;
import com.ak.numbers.common.SimpleCoefficients;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import javax.annotation.Nonnegative;
import java.util.function.IntUnaryOperator;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class RcmCoefficientsTest {
  @Test
  void testCoefficients() {
    assertThat(RcmCoefficients.values()).hasSize(2);
    assertThat(RcmSimpleCoefficients.values()).hasSize(3);

    IntUnaryOperator rheo260ADC = Interpolators.interpolator(RcmCoefficients.RHEO_ADC_TO_260_MILLI.of(1)).get();
    assertThat(rheo260ADC.applyAsInt(100)).isEqualTo(1054);
    assertThat(rheo260ADC.applyAsInt(1300)).isEqualTo(911);

    assertThat(RcmCoefficients.CC_ADC_TO_OHM.of(1).get())
        .isNotEqualTo(RcmCoefficients.CC_ADC_TO_OHM.of(2).get());
    assertThat(RcmCoefficients.RHEO_ADC_TO_260_MILLI.of(1).get())
        .isNotEqualTo(RcmCoefficients.RHEO_ADC_TO_260_MILLI.of(2).get());
  }

  @ParameterizedTest
  @EnumSource(names = "CC_ADC_TO_OHM")
  void testCC(RcmCoefficients c) {
    assertThat(IntStream.of(1, 2)).isNotEmpty().allSatisfy(cNum -> assertThat(c.of(cNum).get()).hasSize(20));
  }

  @ParameterizedTest
  @EnumSource(names = "RHEO_ADC_TO_260_MILLI")
  void testRheo(RcmCoefficients c) {
    assertThat(IntStream.of(1, 2)).isNotEmpty().allSatisfy(cNum -> assertThat(c.of(cNum).get()).hasSize(16));
  }

  static Stream<Arguments> rcmSimpleCoefficients() {
    return Stream.of(
        arguments(RcmSimpleCoefficients.BR_F005, 10),
        arguments(RcmSimpleCoefficients.BR_F025, 25),
        arguments(RcmSimpleCoefficients.BR_F200, 22)
    );
  }

  @ParameterizedTest
  @MethodSource("rcmSimpleCoefficients")
  void testCoefficients(SimpleCoefficients coefficients, @Nonnegative int count) {
    assertThat(coefficients.get()).hasSize(count);
  }
}