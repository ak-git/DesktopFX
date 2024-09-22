package com.ak.appliance.rcm.comm.converter;

import com.ak.appliance.aper.comm.converter.SplineCoefficientsUtils;
import com.ak.appliance.rcm.numbers.RcmBaseSurfaceCoefficientsChannel1;
import com.ak.appliance.rcm.numbers.RcmBaseSurfaceCoefficientsChannel2;
import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.converter.Converter;
import com.ak.comm.converter.LinkedConverter;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.ByteOrder;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class RcmConverterTest {
  static Stream<Arguments> variables() {
    return Stream.of(
        arguments(
            new byte[] {-10, -36, -125, -72, -5, -60, -125, -124, -111, -94, -7, -98, -127, -128, -5, -78, -127, -10, -127, -128},
            new int[] {-67590, 5493, 0, 66, -38791, 31534, 0}
        )
    );
  }

  @ParameterizedTest
  @MethodSource("variables")
  void testApply(byte[] inputBytes, int[] outputInts) {
    Converter<BufferFrame, RcmOutVariable> converter = LinkedConverter.of(new RcmConverter(), RcmOutVariable.class);
    BufferFrame bufferFrame = new BufferFrame(inputBytes, ByteOrder.LITTLE_ENDIAN);
    for (int i = 0; i < 759; i++) {
      long count = converter.apply(bufferFrame).count();
      assertThat(count).withFailMessage("Set cycles to %d", i).isZero();
    }
    assertThat(converter.apply(bufferFrame)).withFailMessage("Increase cycles!").hasSize(5).startsWith(outputInts);
    assertThat(converter.getFrequency()).isEqualTo(200.0);
  }

  @Test
  @Disabled("ignored com.ak.appliance.converter.comm.rcm.RcmConverterTest.testBaseSplineSurface1")
  void testBaseSplineSurface1() {
    SplineCoefficientsUtils.testSplineSurface(RcmBaseSurfaceCoefficientsChannel1.class);
    SplineCoefficientsUtils.testSplineSurface(RcmBaseSurfaceCoefficientsChannel2.class);
  }
}