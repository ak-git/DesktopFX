package com.ak.appliance.nmis.comm.bytes;

import org.junit.jupiter.params.provider.Arguments;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;

class NmisExtractorTestProvider {
  private static final byte[] EMPTY_BYTES = {};

  private NmisExtractorTestProvider() {
  }

  static Stream<Arguments> extractNone() {
    return Stream.of(
        arguments(
            // channel 41 CATCH_ELBOW
            ByteBuffer.wrap(new byte[] {0x7E, 0x41, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0xC7})
        )
    );
  }

  static Stream<Arguments> extractTime() {
    return Stream.of(
        arguments(
            // no Time, alive
            new byte[] {0x7E, -12, 0x7E, 0x40, 0x08, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0xC7},
            List.of()
        ),
        arguments(
            // Time, but NO Data, empty frame
            new byte[] {0x7e, 0x45, 0x02, 0x3f, 0x00, 0x04},
            List.of(0x3f)
        ),
        arguments(
            // Time and Data
            new byte[] {0x7e, 0x45, 0x09, 0x44, 0x00, 0x01, 0x05, 0x0b, (byte) 0xe0, (byte) 0xb1, (byte) 0xe1, 0x7a, 0x0d},
            List.of(0x44)
        )
    );
  }

  static Stream<Arguments> extractData() {
    return Stream.of(
        arguments(
            // no Data, alive
            new byte[] {0x7E, -12, 0x7E, 0x40, 0x08, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0xC7},
            EMPTY_BYTES
        ),
        arguments(
            // no Data, empty frame
            new byte[] {0x7e, 0x45, 0x02, 0x3f, 0x00, 0x04},
            EMPTY_BYTES
        ),
        arguments(
            // Time and Data
            new byte[] {0x7e, 0x45, 0x09, 0x44, 0x00, 0x01, 0x05, 0x0b, (byte) 0xe0, (byte) 0xb1, (byte) 0xe1, 0x7a, 0x0d},
            new byte[] {0x01, 0x05, 0x0b, (byte) 0xe0, (byte) 0xb1, (byte) 0xe1, 0x7a}
        )
    );
  }
}
