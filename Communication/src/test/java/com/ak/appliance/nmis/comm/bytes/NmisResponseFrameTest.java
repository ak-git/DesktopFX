package com.ak.appliance.nmis.comm.bytes;

import com.ak.comm.bytes.BufferFrame;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.ak.comm.bytes.LogUtils.LOG_LEVEL_ERRORS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class NmisResponseFrameTest {
  private static final Logger LOGGER = Logger.getLogger(BufferFrame.class.getName());
  private final AtomicInteger exceptionCounter = new AtomicInteger();

  @BeforeEach
  void setUp() {
    LOGGER.setFilter(r -> {
      assertThat(r.getThrown()).isNull();
      exceptionCounter.incrementAndGet();
      return false;
    });
    LOGGER.setLevel(LOG_LEVEL_ERRORS);
  }

  @AfterEach
  void tearDown() {
    LOGGER.setFilter(null);
    LOGGER.setLevel(Level.INFO);
  }

  @ParameterizedTest
  @MethodSource("com.ak.appliance.nmis.comm.bytes.NmisTestProvider#invalidTestByteResponse")
  void testNewInstance(ByteBuffer byteBuffer) {
    assertAll(Arrays.toString(byteBuffer.array()),
        () -> assertThat(NmisAddress.find(byteBuffer)).isNotEmpty(),
        () -> assertThat(NmisProtocolByte.checkCRC(byteBuffer)).isTrue(),
        () -> assertThat(new NmisResponseFrame.Builder(byteBuffer).build()).isEmpty());
    assertThat(exceptionCounter.get()).isOne();
  }

  @ParameterizedTest
  @MethodSource("com.ak.appliance.nmis.comm.bytes.NmisTestProvider#sequenceResponse")
  void testEquals(NmisRequest request, byte[] input) {
    NmisResponseFrame nmisResponseFrame = new NmisResponseFrame.Builder(ByteBuffer.wrap(input)).build().orElseThrow();
    assertThat(nmisResponseFrame).withFailMessage(nmisResponseFrame::toString)
        .isNotEqualTo(request).hasSameHashCodeAs(request.toResponse());
  }
}