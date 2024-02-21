package com.ak.comm.interceptor.rsce;

import com.ak.comm.bytes.LogUtils;
import com.ak.comm.bytes.rsce.RsceCommandFrame;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.comm.log.LogTestUtils;
import com.ak.util.Strings;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.ByteBuffer;
import java.util.logging.Logger;

import static com.ak.comm.bytes.rsce.RsceCommandFrame.Control.CATCH;
import static com.ak.comm.bytes.rsce.RsceCommandFrame.RequestType.STATUS_I_SPEED_ANGLE;
import static org.assertj.core.api.Assertions.assertThat;

class RsceBytesInterceptorTest {
  private static final Logger LOGGER = Logger.getLogger(RsceBytesInterceptor.class.getName());

  @ParameterizedTest
  @MethodSource("com.ak.comm.bytes.rsce.RsceTestDataProvider#simpleRequests")
  void testSimpleRequest(byte[] bytes, RsceCommandFrame.Control control, RsceCommandFrame.RequestType type) {
    checkResponse(bytes, RsceCommandFrame.simple(control, type));
  }

  @ParameterizedTest
  @MethodSource("com.ak.comm.bytes.rsce.RsceTestDataProvider#offRequests")
  void testOffRequest(byte[] expected, RsceCommandFrame.Control control) {
    checkResponse(expected, RsceCommandFrame.off(control));
  }

  @ParameterizedTest
  @MethodSource("com.ak.comm.bytes.rsce.RsceTestDataProvider#preciseRequests")
  void testPreciseRequest(byte[] expected, short speed) {
    checkResponse(expected, RsceCommandFrame.precise(CATCH, STATUS_I_SPEED_ANGLE, speed));
  }

  private static void checkResponse(byte[] bytes, RsceCommandFrame request) {
    BytesInterceptor<RsceCommandFrame, RsceCommandFrame> interceptor = new RsceBytesInterceptor();

    LogTestUtils.isSubstituteLogLevel(LOGGER, LogUtils.LOG_LEVEL_LEXEMES,
        () -> assertThat(interceptor.apply(ByteBuffer.wrap(bytes))).containsSequence(request),
        logRecord ->
            assertThat(logRecord.getMessage().replaceAll(".*" + RsceCommandFrame.class.getSimpleName(), Strings.EMPTY))
                .isEqualTo(request.toString().replaceAll(".*" + RsceCommandFrame.class.getSimpleName(), Strings.EMPTY))
    );

    LogTestUtils.isSubstituteLogLevel(LOGGER, LogUtils.LOG_LEVEL_LEXEMES,
        () -> assertThat(interceptor.putOut(request).remaining()).isPositive(),
        logRecord -> assertThat(logRecord.getMessage().replaceAll(".*" + RsceCommandFrame.class.getSimpleName(), Strings.EMPTY))
            .isEqualTo(request.toString().replaceAll(".*" + RsceCommandFrame.class.getSimpleName(), Strings.EMPTY) +
                " - " + bytes.length + " bytes OUT to hardware")
    );
  }
}