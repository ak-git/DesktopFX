package com.ak.appliance.rsce.comm.bytes;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class CRC16IBMChecksumTest {
  static Stream<Arguments> checksum() {
    byte[][] input = {
        {0x01, 0x05, 0x0C, 0x00, 0x00},
        {0x01, 0x05, 0x0C, 0x20, 0x4E},
        {0x01, 0x05, 0x0C, (byte) 0xE0, (byte) 0xB1},

        {0x01, 0x05, 0x0C, (byte) 0xA0, 0x0F},
        {0x01, 0x03, 0x00}
    };

    int[] expected = {
        0x0FD9, 0xFB40, 0xBB50, 0x0BE1, 0xF020
    };

    Arguments[] values = new Arguments[expected.length];
    for (int i = 0; i < values.length; i++) {
      values[i] = arguments(input[i], expected[i]);
    }
    return Stream.of(values);
  }

  @ParameterizedTest
  @MethodSource("checksum")
  void testUpdate(byte[] input, int expectedSum) {
    CRC16IBMChecksum checksum = new CRC16IBMChecksum();
    for (int i = 0, bytesLength = input.length; i < bytesLength; i++) {
      for (int b : input) {
        checksum.update(b);
      }
      assertThat(checksum.getValue()).withFailMessage("Test [%d], checksum %s".formatted(i, checksum)).isEqualTo(expectedSum);
      checksum.reset();

      checksum.update(input, 0, input.length);
      assertThat(checksum.getValue()).withFailMessage("Test [%d], checksum %s".formatted(i, checksum)).isEqualTo(expectedSum);
      checksum.reset();
    }
  }
}