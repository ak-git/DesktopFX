package com.ak.appliance.nmis.comm.bytes;

import com.ak.comm.bytes.LogUtils;
import com.ak.comm.log.LogTestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.logging.Logger;

import static com.ak.appliance.nmis.comm.bytes.NmisAddress.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

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
  @MethodSource("com.ak.appliance.nmis.comm.bytes.NmisTestProvider#nullResponse")
  void testNotFound(ByteBuffer buffer) {
    assertTrue(
        LogTestUtils.isSubstituteLogLevel(LOGGER, LogUtils.LOG_LEVEL_ERRORS,
            () -> assertNull(find(buffer)),
            logRecord -> assertThat(logRecord.getMessage()).endsWith("Address -12 not found")
        )
    );
  }

  @ParameterizedTest
  @MethodSource("com.ak.appliance.nmis.comm.bytes.NmisTestProvider#aliveAndChannelsResponse")
  void testFind(NmisAddress address, byte[] input) {
    assertThat(find(ByteBuffer.wrap(input))).isNotNull().isEqualTo(address);
  }

  @ParameterizedTest
  @MethodSource("com.ak.appliance.nmis.comm.bytes.NmisExtractorTestProvider#extractNone")
  void testExtractorNone(ByteBuffer buffer) {
    NmisResponseFrame response = new NmisResponseFrame.Builder(buffer).build().orElseThrow();
    assertThat(response.extractTime().count()).isZero();

    ByteBuffer destination = ByteBuffer.allocate(buffer.array().length);
    response.extractData(destination);
    assertThat(destination.position()).isZero();
  }

  @ParameterizedTest
  @MethodSource("com.ak.appliance.nmis.comm.bytes.NmisExtractorTestProvider#extractTime")
  void testExtractorValues(byte[] from, Iterable<Integer> expected) {
    new NmisResponseFrame.Builder(ByteBuffer.wrap(from)).build()
        .ifPresent(response -> assertThat(response.extractTime()).containsSequence(expected));
  }

  @ParameterizedTest
  @MethodSource("com.ak.appliance.nmis.comm.bytes.NmisExtractorTestProvider#extractData")
  void testExtractorToBuffer(byte[] from, byte[] expected) {
    ByteBuffer destination = ByteBuffer.allocate(expected.length);
    new NmisResponseFrame.Builder(ByteBuffer.wrap(from)).build()
        .ifPresent(response -> {
          response.extractData(destination);
          assertThat(destination.array()).isEqualTo(expected);
        });
  }
}