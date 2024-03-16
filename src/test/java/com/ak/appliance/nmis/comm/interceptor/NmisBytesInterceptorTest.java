package com.ak.appliance.nmis.comm.interceptor;

import com.ak.appliance.nmis.comm.bytes.NmisAddress;
import com.ak.appliance.nmis.comm.bytes.NmisRequest;
import com.ak.appliance.nmis.comm.bytes.NmisResponseFrame;
import com.ak.comm.bytes.LogUtils;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.comm.log.LogTestUtils;
import com.ak.util.Strings;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import static com.ak.appliance.nmis.comm.bytes.NmisAddress.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class NmisBytesInterceptorTest {
  private static final Logger LOGGER = Logger.getLogger(NmisBytesInterceptor.class.getName());

  @ParameterizedTest
  @MethodSource("com.ak.appliance.nmis.comm.bytes.NmisTestProvider#allOhmsMyoOff")
  void testRequestOhms(NmisRequest request, byte[] expected) {
    testRequest(request, expected);
  }

  @ParameterizedTest
  @MethodSource("com.ak.appliance.nmis.comm.bytes.NmisTestProvider#myo")
  void testRequestMyo(NmisRequest request, byte[] expected) {
    testRequest(request, expected);
  }

  @ParameterizedTest
  @MethodSource("com.ak.appliance.nmis.comm.bytes.NmisTestProvider#sequence")
  void testRequestSequence(NmisRequest request, byte[] expected) {
    testRequest(request, expected);
  }

  @ParameterizedTest
  @MethodSource("com.ak.appliance.nmis.comm.bytes.NmisTestProvider#allOhmsMyoOffResponse")
  void testResponseOhms(NmisRequest request, byte[] input) {
    assertThat(request.toResponse())
        .isEqualTo(new NmisResponseFrame.Builder(ByteBuffer.wrap(Arrays.copyOfRange(input, 1, input.length))).build())
        .isNotEqualTo(new NmisResponseFrame.Builder(ByteBuffer.wrap(input)).build());
    testResponse(request, input, true);
  }

  @ParameterizedTest
  @MethodSource("com.ak.appliance.nmis.comm.bytes.NmisTestProvider#myoResponse")
  void testResponseMyo(NmisRequest request, byte[] input) {
    assertThat(request.toResponse()).isEqualTo(new NmisResponseFrame.Builder(ByteBuffer.wrap(input)).build());
    testResponse(request, input, true);
  }

  @ParameterizedTest
  @MethodSource("com.ak.appliance.nmis.comm.bytes.NmisTestProvider#sequenceResponse")
  void testResponseSequence(NmisRequest request, byte[] input) {
    assertThat(request.toResponse()).isEqualTo(new NmisResponseFrame.Builder(ByteBuffer.wrap(input)).build());
    testResponse(request, input, true);
  }

  @ParameterizedTest
  @MethodSource("com.ak.appliance.nmis.comm.bytes.NmisTestProvider#aliveAndChannelsResponse")
  void testResponseAliveAndChannels(NmisAddress address, byte[] input) {
    if (EnumSet.of(CATCH_ELBOW, ROTATE_ELBOW, CATCH_HAND, ROTATE_HAND).contains(address)) {
      assertNotNull(Optional.ofNullable(new NmisResponseFrame.Builder(ByteBuffer.wrap(input)).build()).orElseThrow(NullPointerException::new));
    }
  }

  @ParameterizedTest
  @MethodSource("com.ak.appliance.nmis.comm.bytes.NmisTestProvider#invalidTestByteResponse")
  void testInvalidResponse(ByteBuffer input) {
    testResponse(NmisRequest.Sequence.CATCH_30.build(), input.array(), false);
  }

  @ParameterizedTest
  @MethodSource("com.ak.appliance.nmis.comm.bytes.NmisTestProvider#invalidCRCResponse")
  void testInvalidResponseCRC(ByteBuffer input) {
    testResponse(NmisRequest.Sequence.CATCH_30.build(), input.array(), false);
  }

  private static void testRequest(NmisRequest request, byte[] expected) {
    ByteBuffer byteBuffer = ByteBuffer.allocate(expected.length);
    request.writeTo(byteBuffer);
    assertThat(byteBuffer.array()).containsExactly(expected);
  }

  private static void testResponse(NmisRequest request, byte[] input, boolean logFlag) {
    BytesInterceptor<NmisRequest, NmisResponseFrame> interceptor = new NmisBytesInterceptor();

    assertEquals(LogTestUtils.isSubstituteLogLevel(LOGGER, LogUtils.LOG_LEVEL_LEXEMES, () -> {
      Collection<NmisResponseFrame> frames = interceptor.apply(ByteBuffer.wrap(input)).toList();
      if (!frames.isEmpty()) {
        assertThat(frames).containsSequence(Collections.singleton(request.toResponse()));
      }
    }, logRecord -> assertEquals(logRecord.getMessage().replaceAll(".*" + NmisResponseFrame.class.getSimpleName(), Strings.EMPTY),
        request.toResponse().toString().replaceAll(".*" + NmisResponseFrame.class.getSimpleName(), Strings.EMPTY))), logFlag);

    AtomicReference<String> logMessage = new AtomicReference<>(Strings.EMPTY);
    assertTrue(LogTestUtils.isSubstituteLogLevel(LOGGER, LogUtils.LOG_LEVEL_ERRORS,
        () -> {
          int bytesOut = interceptor.putOut(request).remaining();
          assertTrue(bytesOut > 0);
          assertEquals(logMessage.get(),
              request.toString().replaceAll(".*" + NmisRequest.class.getSimpleName(), Strings.EMPTY) +
                  " - " + bytesOut + " bytes OUT to hardware");
        },
        logRecord -> logMessage.set(logRecord.getMessage().replaceAll(".*" + NmisRequest.class.getSimpleName(), Strings.EMPTY))));
  }
}