package com.ak.appliance.purelogic.comm.bytes;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class PureLogicFrameTest {
  static Stream<Arguments> requests() {
    return Stream.of(
        arguments(PureLogicFrame.ALIVE, "?\r\n"),
        arguments(PureLogicFrame.Direction.NONE.micron7p5multiplyBy(0), "?\r\n"),
        arguments(PureLogicFrame.Direction.DOWN.micron7p5multiplyBy(1), "STEP -00008\r\n"),
        arguments(PureLogicFrame.Direction.UP.micron7p5multiplyBy(2), "STEP +00016\r\n"),
        arguments(PureLogicFrame.Direction.DOWN.micron7p5multiplyBy(20), "STEP -00160\r\n"),
        arguments(PureLogicFrame.Direction.UP.micron7p5multiplyBy(40), "STEP +00320\r\n"),
        arguments(PureLogicFrame.Direction.UP.micron7p5multiplyBy(100), "STEP +00800\r\n")
    );
  }

  @ParameterizedTest
  @MethodSource("requests")
  void testRequest(PureLogicFrame request, String expected) {
    ByteBuffer buffer = ByteBuffer.allocate(expected.length());
    request.writeTo(buffer);
    assertThat(new String(buffer.array(), StandardCharsets.UTF_8)).isEqualTo(expected);
    assertThat(request.toString()).contains(expected.strip());
  }
}