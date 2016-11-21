package com.ak.hardware.nmis.comm.bytes;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.EnumSet;

import org.testng.Assert;
import org.testng.annotations.Test;

import static com.ak.hardware.nmis.comm.bytes.NmisProtocolByte.LEN;
import static com.ak.hardware.nmis.comm.bytes.NmisProtocolByte.MAX_CAPACITY;
import static com.ak.hardware.nmis.comm.bytes.NmisProtocolByte.START;
import static com.ak.hardware.nmis.comm.bytes.NmisProtocolByte.checkCRC;

public final class NmisProtocolByteTest {
  @Test
  public void testIs() {
    Assert.assertFalse(START.is((byte) 0x00));
    Assert.assertTrue(START.is((byte) 0x7E));

    Assert.assertFalse(LEN.is((byte) (MAX_CAPACITY + 1)));
    Assert.assertTrue(LEN.is((byte) (MAX_CAPACITY - 4)));

    for (NmisProtocolByte b : EnumSet.complementOf(EnumSet.of(START, LEN))) {
      Assert.assertThrows(UnsupportedOperationException.class, () -> b.is((byte) 0x00));
    }
  }

  @Test(dataProviderClass = NmisTestProvider.class, dataProvider = "allOhmsMyoOffResponse")
  public void testResponseOhmsCRC(NmisRequest request, byte[] input) {
    Assert.assertTrue(checkCRC(ByteBuffer.wrap(Arrays.copyOfRange(input, 1, input.length))), Arrays.toString(input));
    Assert.assertFalse(checkCRC(ByteBuffer.wrap(input)), Arrays.toString(input));
  }

  @Test(dataProviderClass = NmisTestProvider.class, dataProvider = "360OhmsMyoHzResponse")
  public void testResponseMyoCRC(NmisRequest request, byte[] input) {
    Assert.assertTrue(checkCRC(ByteBuffer.wrap(input)), Arrays.toString(input));
  }

  @Test(dataProviderClass = NmisTestProvider.class, dataProvider = "sequenceResponse")
  public void testResponseSequenceCRC(NmisRequest request, byte[] input) {
    Assert.assertTrue(checkCRC(ByteBuffer.wrap(input)), Arrays.toString(input));
  }

  @Test(dataProviderClass = NmisTestProvider.class, dataProvider = "aliveAndChannelsResponse")
  public void testResponseAliveAndChannelsCRC(NmisAddress address, byte[] input) {
    if (NmisAddress.CHANNELS.contains(address)) {
      Assert.assertTrue(checkCRC(ByteBuffer.wrap(input)), Arrays.toString(input));
    }
  }
}