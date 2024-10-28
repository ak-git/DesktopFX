package com.ak.appliance.briko.comm.converter;

import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.converter.Converter;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.ByteOrder;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class BrikoConverterTest {
  static Stream<Arguments> variables() {
    return Stream.of(
        arguments(new byte[] {
                0, 0x2A,
                (byte) 0xc1,
                0x2d, 0x4f, 0x02, 0x00,
                (byte) 0xc2,
                0x5a, 0x27, 0x03, 0x00,
                (byte) 0xc3,
                0, 0, 0, 0,
                (byte) 0xc4,
                0, 0, 0, 0,
                (byte) 0xc5,
                0x20, (byte) 0xbf, 0x02, 0x00,
                (byte) 0xc6,
                0x20, (byte) 0xbf, 0x02, 0x00,
                (byte) 0xc7,
                0x20, (byte) 0xbf, 0x02, 0x00,
                (byte) 0xc8,
                0x20, (byte) 0xbf, 0x02, 0x00,
            },
            new int[] {24, 33, 0, 0, 29, 29, 29, 29}
        )
    );
  }

  @ParameterizedTest
  @MethodSource("variables")
  void testApply(byte[] inputBytes, int[] outputInts) {
    Converter<BufferFrame, BrikoVariable> converter = new BrikoConverter();
    AtomicBoolean processed = new AtomicBoolean();
    BufferFrame bufferFrame = new BufferFrame(inputBytes, ByteOrder.LITTLE_ENDIAN);

    converter.apply(bufferFrame).forEach(ints -> {
      assertThat(ints).containsExactly(outputInts);
      processed.set(true);
    });

    assertTrue(processed.get(), "Data are not converted!");
    assertThat(converter.getFrequency()).isEqualTo(1000.0);
  }
}