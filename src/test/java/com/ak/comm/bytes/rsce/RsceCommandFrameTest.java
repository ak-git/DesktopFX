package com.ak.comm.bytes.rsce;

import java.nio.ByteBuffer;
import java.util.Arrays;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.comm.bytes.BytesChecker;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;

class RsceCommandFrameTest {
  @ParameterizedTest
  @MethodSource("com.ak.comm.bytes.rsce.RsceTestDataProvider#simpleRequests")
  @ParametersAreNonnullByDefault
  void testSimpleRequest(byte[] expected, RsceCommandFrame.Control control, RsceCommandFrame.RequestType type) {
    checkRequest(expected, RsceCommandFrame.simple(control, type));
  }

  @ParameterizedTest
  @MethodSource("com.ak.comm.bytes.rsce.RsceTestDataProvider#offRequests")
  @ParametersAreNonnullByDefault
  void testOffRequest(byte[] expected, RsceCommandFrame.Control control) {
    checkRequest(expected, RsceCommandFrame.off(control));
  }

  @ParameterizedTest
  @MethodSource("com.ak.comm.bytes.rsce.RsceTestDataProvider#preciseRequests")
  void testPreciseRequest(@Nonnull byte[] expected, short speed) {
    checkRequest(expected, RsceCommandFrame.precise(RsceCommandFrame.Control.CATCH, RsceCommandFrame.RequestType.STATUS_I_SPEED_ANGLE, speed));
  }

  @ParameterizedTest
  @MethodSource("com.ak.comm.bytes.rsce.RsceTestDataProvider#positionCatchRequests")
  void testPositionCatchRequest(@Nonnull byte[] expected, byte position) {
    checkRequest(expected, RsceCommandFrame.position(RsceCommandFrame.Control.CATCH, position));
    RsceCommandFrame frame = new RsceCommandFrame.ResponseBuilder(ByteBuffer.wrap(expected)).build();
    assertThat(frame).isNotNull();
    assertThat(frame.extract(RsceCommandFrame.FrameField.OPEN_PERCENT, -1))
        .withFailMessage("%s, Position = %d".formatted(frame, frame.extract(RsceCommandFrame.FrameField.OPEN_PERCENT, -1)))
        .isEqualTo(position);
  }

  @ParameterizedTest
  @MethodSource("com.ak.comm.bytes.rsce.RsceTestDataProvider#positionRotateRequests")
  void testPositionRotateRequest(@Nonnull byte[] expected, byte position) {
    checkRequest(expected, RsceCommandFrame.position(RsceCommandFrame.Control.ROTATE, position));
    RsceCommandFrame frame = new RsceCommandFrame.ResponseBuilder(ByteBuffer.wrap(expected)).build();
    assertThat(frame).isNotNull();
    assertThat(frame.extract(RsceCommandFrame.FrameField.ROTATE_PERCENT, -1))
        .withFailMessage("%s, Position = %d".formatted(frame, frame.extract(RsceCommandFrame.FrameField.ROTATE_PERCENT, -1)))
        .isEqualTo(position);
  }

  @ParameterizedTest
  @MethodSource("com.ak.comm.bytes.rsce.RsceTestDataProvider#infoRequests")
  @ParametersAreNonnullByDefault
  void testInfoRequest(byte[] bytes, int[] rDozenMilliOhms, int[] infoOnes) {
    RsceCommandFrame frame = new RsceCommandFrame.ResponseBuilder(ByteBuffer.wrap(bytes)).build();
    assertThat(frame).isNotNull();

    assertThat(frame.extract(RsceCommandFrame.FrameField.R1_DOZEN_MILLI_OHM, 0))
        .withFailMessage("%s, Ohms = %d".formatted(frame, frame.extract(RsceCommandFrame.FrameField.R1_DOZEN_MILLI_OHM, 0)))
        .isEqualTo(rDozenMilliOhms[0]);
    assertThat(frame.extract(RsceCommandFrame.FrameField.R2_DOZEN_MILLI_OHM, 0))
        .withFailMessage("%s, Ohms = %d".formatted(frame, frame.extract(RsceCommandFrame.FrameField.R2_DOZEN_MILLI_OHM, 0)))
        .isEqualTo(rDozenMilliOhms[1]);
    assertThat(frame.extract(RsceCommandFrame.FrameField.ACCELEROMETER, 0))
        .withFailMessage("%s, Info = %d".formatted(frame, frame.extract(RsceCommandFrame.FrameField.ACCELEROMETER, 0)))
        .isEqualTo(infoOnes[0]);
  }

  @ParameterizedTest
  @MethodSource("com.ak.comm.bytes.rsce.RsceTestDataProvider#finger")
  void testInfoRequest(@Nonnull byte[] bytes, int fingerSpeed) {
    RsceCommandFrame frame = new RsceCommandFrame.ResponseBuilder(ByteBuffer.wrap(bytes)).build();
    assertThat(frame).isNotNull();
    assertThat(frame.extract(RsceCommandFrame.FrameField.FINGER, 0))
        .withFailMessage("%s, finger = %d".formatted(frame, frame.extract(RsceCommandFrame.FrameField.FINGER, 0)))
        .isEqualTo(fingerSpeed);
  }

  @Test
  void testInvalidInfoRequest() {
    RsceCommandFrame frame = new RsceCommandFrame.RequestBuilder(
        RsceCommandFrame.Control.ALL, RsceCommandFrame.ActionType.NONE, RsceCommandFrame.RequestType.EMPTY
    ).build();
    assertThat(frame).isNotNull();
  }

  @ParameterizedTest
  @MethodSource("com.ak.comm.bytes.rsce.RsceTestDataProvider#invalidRequests")
  void testInvalidRequests(@Nonnull ByteBuffer buffer) {
    assertThat(new RsceCommandFrame.ResponseBuilder(buffer).build()).withFailMessage(() -> Arrays.toString(buffer.array())).isNull();
  }

  @ParameterizedTest
  @MethodSource("com.ak.comm.bytes.rsce.RsceTestDataProvider#invalidRequests")
  void testInvalidMethod(@Nonnull ByteBuffer buffer) {
    BytesChecker responseBuilder = new RsceCommandFrame.ResponseBuilder();
    Assertions.assertThatExceptionOfType(UnsupportedOperationException.class)
        .isThrownBy(() -> responseBuilder.bufferLimit(buffer)).withNoCause().withMessage(Arrays.toString(buffer.array()));
  }

  @ParametersAreNonnullByDefault
  private static void checkRequest(byte[] expected, RsceCommandFrame request) {
    ByteBuffer byteBuffer = ByteBuffer.allocate(expected.length);
    request.writeTo(byteBuffer);
    assertThat(byteBuffer.array()).withFailMessage(request::toString).isEqualTo(expected);

    RsceCommandFrame that = new RsceCommandFrame.ResponseBuilder(byteBuffer).build();
    assertThat(that).isNotNull().isEqualTo(request).isEqualTo(that).hasSameHashCodeAs(request).isNotEqualTo(byteBuffer);
  }
}