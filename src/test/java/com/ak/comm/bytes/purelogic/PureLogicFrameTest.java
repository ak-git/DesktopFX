package com.ak.comm.bytes.purelogic;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.annotation.Nonnull;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class PureLogicFrameTest {
  static Stream<Arguments> requests() {
    return Stream.of(
        arguments(PureLogicFrame.StepCommand.MICRON_015.action(false), "STEP -00016\r\n"),
        arguments(PureLogicFrame.StepCommand.MICRON_015.action(true), "STEP +00016\r\n"),
        arguments(PureLogicFrame.StepCommand.MICRON_150.action(false), "STEP -00160\r\n"),
        arguments(PureLogicFrame.StepCommand.MICRON_300.action(true), "STEP +00320\r\n"),
        arguments(PureLogicFrame.StepCommand.MICRON_750.action(true), "STEP +00800\r\n")
    );
  }

  @ParameterizedTest
  @MethodSource("requests")
  void testRequest(@Nonnull PureLogicFrame request, @Nonnull String expected) {
    ByteBuffer buffer = ByteBuffer.allocate(expected.length());
    request.writeTo(buffer);
    assertThat(new String(buffer.array(), StandardCharsets.UTF_8)).isEqualTo(expected);
    assertThat(request.toString()).contains(expected.strip());
  }
}