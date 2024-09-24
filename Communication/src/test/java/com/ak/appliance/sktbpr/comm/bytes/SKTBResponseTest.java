package com.ak.appliance.sktbpr.comm.bytes;

import com.ak.comm.bytes.BufferFrame;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.ak.comm.bytes.LogUtils.LOG_LEVEL_ERRORS;
import static org.assertj.core.api.Assertions.assertThat;

class SKTBResponseTest {
  @Test
  void testRequestOk() {
    SKTBResponse.Builder builder = new SKTBResponse.Builder();
    builder.buffer().put((byte) 0xa5);
    assertThat(builder.is((byte) 0xa5)).isTrue();
    builder.buffer().put((byte) 0x00);

    builder.buffer().put((byte) 6);
    assertThat(builder.is((byte) 6)).isTrue();
    builder.buffer().put((byte) 59);
    builder.buffer().put((byte) 1);
    builder.buffer().put((byte) 0);
    builder.buffer().put((byte) 0);
    builder.buffer().put((byte) -84);
    builder.buffer().put((byte) 10);

    builder.buffer().rewind();
    SKTBResponse response = builder.build().orElseThrow();
    assertThat(response).extracting(SKTBResponse::rotateAngle).isEqualTo(3);
    assertThat(response).extracting(SKTBResponse::flexAngle).isEqualTo(27);
  }

  @Nested
  class SKTBResponseTestLogger {
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

    @Test
    void testRequestFall() {
      SKTBResponse.Builder builder = new SKTBResponse.Builder();
      builder.buffer().put((byte) 0x5a);
      assertThat(builder.is((byte) 0x5a)).isFalse();
      builder.buffer().put((byte) 0x00);

      builder.buffer().put((byte) 7);
      assertThat(builder.is((byte) 7)).isFalse();

      builder.buffer().rewind();
      assertThat(builder.build()).isEmpty();
      assertThat(exceptionCounter.get()).isOne();
    }
  }
}