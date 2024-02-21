package com.ak.comm.bytes.sktbpr;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class SKTBRequestTest {
  static Stream<Arguments> requests() {
    SKTBRequest build1 = SKTBRequest.NONE;
    SKTBRequest build2 = build1.from().rotate(-100_000).flex(0).grip(0).build();
    SKTBRequest build3 = build2.from().rotate(0).flex(31_000).grip(0).build();
    SKTBRequest build4 = build3.from().rotate(0).flex(0).grip(-20_000).build();
    SKTBRequest build5 = build4.from().rotate(-100_000).flex(31_000).grip(-20_000).build();
    return Stream.of(
        arguments(build1,
            ByteBuffer.allocate(11).order(ByteOrder.LITTLE_ENDIAN)
                .put((byte) 0x5a).put((byte) 0).put((byte) 8).putShort((short) 0).putShort((short) 0).putShort((short) 0)
                .putShort((short) 500)
        ),
        arguments(build2,
            ByteBuffer.allocate(11).order(ByteOrder.LITTLE_ENDIAN)
                .put((byte) 0x5a).put((byte) 1).put((byte) 8).putShort((short) -10000).putShort((short) 0).putShort((short) 0)
                .putShort((short) 500)
        ),
        arguments(build3,
            ByteBuffer.allocate(11).order(ByteOrder.LITTLE_ENDIAN)
                .put((byte) 0x5a).put((byte) 2).put((byte) 8).putShort((short) 0).putShort((short) 3000).putShort((short) 0)
                .putShort((short) 500)
        ),
        arguments(build4,
            ByteBuffer.allocate(11).order(ByteOrder.LITTLE_ENDIAN)
                .put((byte) 0x5a).put((byte) 3).put((byte) 8).putShort((short) 0).putShort((short) 0).putShort((short) -10000)
                .putShort((short) 500)
        ),
        arguments(build5,
            ByteBuffer.allocate(11).order(ByteOrder.LITTLE_ENDIAN)
                .put((byte) 0x5a).put((byte) 4).put((byte) 8).putShort((short) -10000).putShort((short) 3000).putShort((short) -10000)
                .putShort((short) 500)
        )
    );
  }

  @ParameterizedTest
  @MethodSource("requests")
  void testRequest(SKTBRequest request, ByteBuffer expected) {
    ByteBuffer buffer = ByteBuffer.allocate(expected.capacity());
    request.writeTo(buffer);
    assertThat(buffer.array()).isEqualTo(expected.array());
  }
}