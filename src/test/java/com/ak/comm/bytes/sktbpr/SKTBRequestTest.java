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
    SKTBRequest empty = new SKTBRequest.RequestBuilder(null).build();
    return Stream.of(
        arguments(empty,
            ByteBuffer.allocate(11).order(ByteOrder.LITTLE_ENDIAN)
                .put((byte) 0x5a).put((byte) 0).put((byte) 8).putShort((short) 0).putShort((short) 0).putShort((short) 0)
                .putShort((short) 500)
        ),
        arguments(new SKTBRequest.RequestBuilder(empty).build(),
            ByteBuffer.allocate(11).order(ByteOrder.LITTLE_ENDIAN)
                .put((byte) 0x5a).put((byte) 1).put((byte) 8).putShort((short) 0).putShort((short) 0).putShort((short) 0)
                .putShort((short) 500)
        ),
        arguments(new SKTBRequest.RequestBuilder(empty).rotate(-100).build(),
            ByteBuffer.allocate(11).order(ByteOrder.LITTLE_ENDIAN)
                .put((byte) 0x5a).put((byte) 1).put((byte) 8).putShort((short) -50000).putShort((short) 0).putShort((short) 0)
                .putShort((short) 500)
        ),
        arguments(new SKTBRequest.RequestBuilder(empty).flex(31).build(),
            ByteBuffer.allocate(11).order(ByteOrder.LITTLE_ENDIAN)
                .put((byte) 0x5a).put((byte) 1).put((byte) 8).putShort((short) 0).putShort((short) 30000).putShort((short) 0)
                .putShort((short) 500)
        ),
        arguments(new SKTBRequest.RequestBuilder(empty).grip(-20).build(),
            ByteBuffer.allocate(11).order(ByteOrder.LITTLE_ENDIAN)
                .put((byte) 0x5a).put((byte) 1).put((byte) 8).putShort((short) 0).putShort((short) 0).putShort((short) -20000)
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