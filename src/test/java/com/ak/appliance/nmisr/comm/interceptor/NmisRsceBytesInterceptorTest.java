package com.ak.appliance.nmisr.comm.interceptor;

import com.ak.appliance.nmis.comm.bytes.NmisProtocolByte;
import com.ak.appliance.nmis.comm.bytes.NmisRequest;
import com.ak.appliance.nmis.comm.interceptor.NmisBytesInterceptor;
import com.ak.appliance.rsce.comm.bytes.RsceCommandFrame;
import com.ak.comm.bytes.LogUtils;
import com.ak.comm.interceptor.BytesInterceptor;
import com.ak.comm.log.LogTestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.annotation.Nullable;
import java.nio.ByteBuffer;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class NmisRsceBytesInterceptorTest {
  private static final Logger LOGGER = Logger.getLogger(NmisRsceBytesInterceptor.class.getName());
  private final BytesInterceptor<NmisRequest, RsceCommandFrame> interceptor = new NmisRsceBytesInterceptor();
  private final ByteBuffer byteBuffer = ByteBuffer.allocate(NmisProtocolByte.MAX_CAPACITY);

  static Stream<Arguments> data() {
    return Stream.of(
        arguments(new byte[] {
                // NO Data, empty Rsce frame
                0x7e, 0x45, 0x02, 0x3f, 0x00, 0x04},
            RsceCommandFrame.simple(RsceCommandFrame.Control.ALL, RsceCommandFrame.RequestType.EMPTY)
        ),
        arguments(new byte[] {
                0x7e, 0x45, 0x08, 0x3f, 0x00, 0x03, 0x04, 0x18, 0x32, (byte) 0xca, 0x74, (byte) 0x99},
            RsceCommandFrame.position(RsceCommandFrame.Control.ROTATE, (byte) 50)
        ),
        arguments(new byte[] {
                0x7e, 0x45, 0x09, 0x44, 0x00, 0x01, 0x05, 0x0b, (byte) 0xe0, (byte) 0xb1, (byte) 0xe1, 0x7a, 0x0d},
            RsceCommandFrame.precise(RsceCommandFrame.Control.CATCH, RsceCommandFrame.RequestType.STATUS_I_ANGLE)
        ),
        arguments(new byte[] {
                // NO Data, invalid Rsce frame
                0x7e, (byte) 0x92, 0x08, 0x01, 0x00, 0x00, 0x00, (byte) 0x84, (byte) 0x84, (byte) 0x84, (byte) 0x84, 0x29},
            RsceCommandFrame.simple(RsceCommandFrame.Control.ALL, RsceCommandFrame.RequestType.EMPTY)
        )
    );
  }

  @ParameterizedTest
  @MethodSource("data")
  void testInterceptor(byte[] bytes, @Nullable RsceCommandFrame response) {
    byteBuffer.clear();
    byteBuffer.put(bytes);
    byteBuffer.flip();
    Assertions.assertAll(interceptor.toString(),
        () -> assertThat(interceptor.name()).isEqualTo("NMIS-RSC Energia"),
        () -> assertThat(interceptor.getBaudRate()).isEqualTo(new NmisBytesInterceptor().getBaudRate()),
        () -> assertThat(interceptor.getPingRequest()).hasValue(NmisRequest.Sequence.CATCH_100.build())
    );

    assertFalse(LogTestUtils.isSubstituteLogLevel(LOGGER, LogUtils.LOG_LEVEL_LEXEMES,
        () -> {
          Stream<RsceCommandFrame> frames = interceptor.apply(byteBuffer);
          if (response == null) {
            assertThat(frames.count()).isZero();
          }
          else {
            assertThat(frames).containsExactly(response);
          }
          assertThat(interceptor.putOut(NmisRequest.Sequence.ROTATE_INV.build()).remaining()).isPositive();
        },
        logRecord -> fail(logRecord.getMessage())
    ));
  }
}