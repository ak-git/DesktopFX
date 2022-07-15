package com.ak.comm.bytes.nmis;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.EnumSet;

import javax.annotation.ParametersAreNonnullByDefault;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NmisProtocolByteTest {
  @Test
  void testIs() {
    assertFalse(NmisProtocolByte.START.is((byte) 0x00));
    assertTrue(NmisProtocolByte.START.is((byte) 0x7E));

    assertFalse(NmisProtocolByte.LEN.is((byte) (NmisProtocolByte.MAX_CAPACITY + 1)));
    assertTrue(NmisProtocolByte.LEN.is((byte) (NmisProtocolByte.MAX_CAPACITY - 4)));

    for (NmisProtocolByte b : EnumSet.complementOf(EnumSet.of(NmisProtocolByte.START, NmisProtocolByte.LEN))) {
      assertThrows(UnsupportedOperationException.class, () -> b.is((byte) 0x00));
    }
  }

  @ParameterizedTest
  @MethodSource("com.ak.comm.bytes.nmis.NmisTestProvider#allOhmsMyoOffResponse")
  @ParametersAreNonnullByDefault
  void testResponseOhmsCRC(NmisRequest request, byte[] input) {
    assertNotNull(request);
    assertTrue(NmisProtocolByte.checkCRC(ByteBuffer.wrap(Arrays.copyOfRange(input, 1, input.length))), Arrays.toString(input));
    assertFalse(NmisProtocolByte.checkCRC(ByteBuffer.wrap(input)), Arrays.toString(input));
  }

  @ParameterizedTest
  @MethodSource("com.ak.comm.bytes.nmis.NmisTestProvider#myoResponse")
  @ParametersAreNonnullByDefault
  void testResponseMyoCRC(NmisRequest request, byte[] input) {
    assertNotNull(request);
    assertTrue(NmisProtocolByte.checkCRC(ByteBuffer.wrap(input)), Arrays.toString(input));
  }

  @ParameterizedTest
  @MethodSource("com.ak.comm.bytes.nmis.NmisTestProvider#sequenceResponse")
  @ParametersAreNonnullByDefault
  void testResponseSequenceCRC(NmisRequest request, byte[] input) {
    assertNotNull(request);
    assertTrue(NmisProtocolByte.checkCRC(ByteBuffer.wrap(input)), Arrays.toString(input));
  }

  @ParameterizedTest
  @MethodSource("com.ak.comm.bytes.nmis.NmisTestProvider#aliveAndChannelsResponse")
  @ParametersAreNonnullByDefault
  void testResponseAliveAndChannelsCRC(NmisAddress address, byte[] input) {
    if (NmisAddress.CHANNELS.contains(address)) {
      assertTrue(NmisProtocolByte.checkCRC(ByteBuffer.wrap(input)), Arrays.toString(input));
    }
  }
}