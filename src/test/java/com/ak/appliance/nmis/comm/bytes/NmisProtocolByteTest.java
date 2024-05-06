package com.ak.appliance.nmis.comm.bytes;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.EnumSet;

import static com.ak.appliance.nmis.comm.bytes.NmisAddress.*;
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
  @MethodSource("com.ak.appliance.nmis.comm.bytes.NmisTestProvider#allOhmsMyoOffResponse")
  void testResponseOhmsCRC(NmisRequest request, byte[] input) {
    assertNotNull(request);
    assertTrue(NmisProtocolByte.checkCRC(ByteBuffer.wrap(Arrays.copyOfRange(input, 1, input.length))), Arrays.toString(input));
    assertFalse(NmisProtocolByte.checkCRC(ByteBuffer.wrap(input)), Arrays.toString(input));
  }

  @ParameterizedTest
  @MethodSource({
      "com.ak.appliance.nmis.comm.bytes.NmisTestProvider#allOhmsMyoOff",
      "com.ak.appliance.nmis.comm.bytes.NmisTestProvider#myo",
      "com.ak.appliance.nmis.comm.bytes.NmisTestProvider#sequence",
      "com.ak.appliance.nmis.comm.bytes.NmisTestProvider#myoResponse"
  })
  void testResponseMyoCRC(NmisRequest request, byte[] input) {
    assertNotNull(request);
    assertTrue(NmisProtocolByte.checkCRC(ByteBuffer.wrap(input)), Arrays.toString(input));
  }

  @ParameterizedTest
  @MethodSource("com.ak.appliance.nmis.comm.bytes.NmisTestProvider#aliveAndChannelsResponse")
  void testResponseAliveAndChannelsCRC(NmisAddress address, byte[] input) {
    if (EnumSet.of(CATCH_ELBOW, ROTATE_ELBOW, CATCH_HAND, ROTATE_HAND).contains(address)) {
      assertTrue(NmisProtocolByte.checkCRC(ByteBuffer.wrap(input)), Arrays.toString(input));
    }
  }
}