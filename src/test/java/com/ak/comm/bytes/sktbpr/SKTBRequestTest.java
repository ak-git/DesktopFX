package com.ak.comm.bytes.sktbpr;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.annotation.Nonnull;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class SKTBRequestTest {
  static Stream<Arguments> requests() {
    SKTBRequest build1 = SKTBRequest.RequestBuilder.of(null).rotate(0).flex(0).grip(0).build();
    SKTBRequest build2 = SKTBRequest.RequestBuilder.of(build1).rotate(-100).flex(0).grip(0).build();
    SKTBRequest build3 = SKTBRequest.RequestBuilder.of(build2).rotate(0).flex(31).grip(0).build();
    SKTBRequest build4 = SKTBRequest.RequestBuilder.of(build3).rotate(0).flex(0).grip(-20).build();
    SKTBRequest build5 = SKTBRequest.RequestBuilder.of(build4).rotate(-100).flex(31).grip(-20).build();
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
  void testRequest(@Nonnull SKTBRequest request, @Nonnull ByteBuffer expected) {
    ByteBuffer buffer = ByteBuffer.allocate(expected.capacity());
    request.writeTo(buffer);
    assertThat(buffer.array()).isEqualTo(expected.array());
  }
}