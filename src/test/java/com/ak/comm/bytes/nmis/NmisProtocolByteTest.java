package com.ak.comm.bytes.nmis;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.EnumSet;

import static com.ak.comm.bytes.nmis.NmisAddress.*;
import static org.junit.jupiter.api.Assertions.*;

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
  void testResponseOhmsCRC(NmisRequest request, byte[] input) {
    assertNotNull(request);
    assertTrue(NmisProtocolByte.checkCRC(ByteBuffer.wrap(Arrays.copyOfRange(input, 1, input.length))), Arrays.toString(input));
    assertFalse(NmisProtocolByte.checkCRC(ByteBuffer.wrap(input)), Arrays.toString(input));
  }

  @ParameterizedTest
  @MethodSource({
      "com.ak.comm.bytes.nmis.NmisTestProvider#allOhmsMyoOff",
      "com.ak.comm.bytes.nmis.NmisTestProvider#myo",
      "com.ak.comm.bytes.nmis.NmisTestProvider#sequence",
      "com.ak.comm.bytes.nmis.NmisTestProvider#myoResponse"
  })
  void testResponseMyoCRC(NmisRequest request, byte[] input) {
    assertNotNull(request);
    assertTrue(NmisProtocolByte.checkCRC(ByteBuffer.wrap(input)), Arrays.toString(input));
  }

  @ParameterizedTest
  @MethodSource("com.ak.comm.bytes.nmis.NmisTestProvider#aliveAndChannelsResponse")
  void testResponseAliveAndChannelsCRC(NmisAddress address, byte[] input) {
    if (EnumSet.of(CATCH_ELBOW, ROTATE_ELBOW, CATCH_HAND, ROTATE_HAND).contains(address)) {
      assertTrue(NmisProtocolByte.checkCRC(ByteBuffer.wrap(input)), Arrays.toString(input));
    }
  }
}