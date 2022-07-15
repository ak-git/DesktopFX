package com.ak.comm.bytes.nmis;

import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.Optional;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.comm.bytes.LogUtils;
import com.ak.comm.log.LogTestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static com.ak.comm.bytes.nmis.NmisAddress.ALIVE;
import static com.ak.comm.bytes.nmis.NmisAddress.CATCH_ELBOW;
import static com.ak.comm.bytes.nmis.NmisAddress.CATCH_HAND;
import static com.ak.comm.bytes.nmis.NmisAddress.DATA;
import static com.ak.comm.bytes.nmis.NmisAddress.ROTATE_ELBOW;
import static com.ak.comm.bytes.nmis.NmisAddress.ROTATE_HAND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NmisAddressTest {
  private static final Logger LOGGER = Logger.getLogger(NmisAddress.class.getName());

  @Test
  void testGetAddrRequest() {
    EnumSet<NmisAddress> bad = EnumSet.of(ALIVE, CATCH_ELBOW, ROTATE_ELBOW, CATCH_HAND, ROTATE_HAND);
    for (NmisAddress address : bad) {
      assertThrows(UnsupportedOperationException.class, address::getAddrRequest);
    }
    for (NmisAddress address : EnumSet.complementOf(bad)) {
      if (address == DATA) {
        assertThat(address.getAddrRequest()).isEqualTo(address.getAddrResponse());
      }
      else {
        assertThat(address.getAddrRequest()).isNotEqualTo(address.getAddrResponse());
      }
    }
  }

  @ParameterizedTest
  @MethodSource("com.ak.comm.bytes.nmis.NmisTestProvider#nullResponse")
  void testNotFound(@Nonnull ByteBuffer buffer) {
    assertTrue(
        LogTestUtils.isSubstituteLogLevel(LOGGER, LogUtils.LOG_LEVEL_ERRORS,
            () -> assertNull(NmisAddress.find(buffer)),
            logRecord -> assertThat(logRecord.getMessage()).endsWith("Address -12 not found")
        )
    );
  }

  @ParameterizedTest
  @MethodSource("com.ak.comm.bytes.nmis.NmisTestProvider#aliveAndChannelsResponse")
  @ParametersAreNonnullByDefault
  void testFind(NmisAddress address, byte[] input) {
    assertThat(NmisAddress.find(ByteBuffer.wrap(input))).isNotNull().isEqualTo(address);
  }

  @ParameterizedTest
  @MethodSource("com.ak.comm.bytes.nmis.NmisExtractorTestProvider#extractNone")
  void testExtractorNone(@Nonnull ByteBuffer buffer) {
    NmisResponseFrame response = new NmisResponseFrame.Builder(buffer).build();
    assertNotNull(response);
    assertThat(response.extractTime().count()).isZero();

    ByteBuffer destination = ByteBuffer.allocate(buffer.array().length);
    response.extractData(destination);
    assertThat(destination.position()).isZero();
  }

  @ParameterizedTest
  @MethodSource("com.ak.comm.bytes.nmis.NmisExtractorTestProvider#extractTime")
  @ParametersAreNonnullByDefault
  void testExtractorValues(byte[] from, Iterable<Integer> expected) {
    Optional.ofNullable(new NmisResponseFrame.Builder(ByteBuffer.wrap(from)).build())
        .ifPresent(response -> assertThat(response.extractTime()).containsSequence(expected));
  }

  @ParameterizedTest
  @MethodSource("com.ak.comm.bytes.nmis.NmisExtractorTestProvider#extractData")
  @ParametersAreNonnullByDefault
  void testExtractorToBuffer(byte[] from, byte[] expected) {
    ByteBuffer destination = ByteBuffer.allocate(expected.length);
    Optional.ofNullable(new NmisResponseFrame.Builder(ByteBuffer.wrap(from)).build())
        .ifPresent(response -> {
          response.extractData(destination);
          assertThat(destination.array()).isEqualTo(expected);
        });
  }
}