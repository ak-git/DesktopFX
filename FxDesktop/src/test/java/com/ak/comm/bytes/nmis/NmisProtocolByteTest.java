package com.ak.comm.bytes.nmis;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.EnumSet;

import org.testng.Assert;
import org.testng.annotations.Test;

public final class NmisProtocolByteTest {
  @Test
  public void testIs() {
    Assert.assertFalse(NmisProtocolByte.START.is((byte) 0x00));
    Assert.assertTrue(NmisProtocolByte.START.is((byte) 0x7E));

    Assert.assertFalse(NmisProtocolByte.LEN.is((byte) (NmisProtocolByte.MAX_CAPACITY + 1)));
    Assert.assertTrue(NmisProtocolByte.LEN.is((byte) (NmisProtocolByte.MAX_CAPACITY - 4)));

    for (NmisProtocolByte b : EnumSet.complementOf(EnumSet.of(NmisProtocolByte.START, NmisProtocolByte.LEN))) {
      Assert.assertThrows(UnsupportedOperationException.class, () -> b.is((byte) 0x00));
    }
  }

  @Test(dataProviderClass = NmisTestProvider.class, dataProvider = "allOhmsMyoOffResponse")
  public void testResponseOhmsCRC(NmisRequest request, byte[] input) {
    Assert.assertTrue(NmisProtocolByte.checkCRC(ByteBuffer.wrap(Arrays.copyOfRange(input, 1, input.length))), Arrays.toString(input));
    Assert.assertFalse(NmisProtocolByte.checkCRC(ByteBuffer.wrap(input)), Arrays.toString(input));
  }

  @Test(dataProviderClass = NmisTestProvider.class, dataProvider = "360OhmsMyoHzResponse")
  public void testResponseMyoCRC(NmisRequest request, byte[] input) {
    Assert.assertTrue(NmisProtocolByte.checkCRC(ByteBuffer.wrap(input)), Arrays.toString(input));
  }

  @Test(dataProviderClass = NmisTestProvider.class, dataProvider = "sequenceResponse")
  public void testResponseSequenceCRC(NmisRequest request, byte[] input) {
    Assert.assertTrue(NmisProtocolByte.checkCRC(ByteBuffer.wrap(input)), Arrays.toString(input));
  }

  @Test(dataProviderClass = NmisTestProvider.class, dataProvider = "aliveAndChannelsResponse")
  public void testResponseAliveAndChannelsCRC(NmisAddress address, byte[] input) {
    if (NmisAddress.CHANNELS.contains(address)) {
      Assert.assertTrue(NmisProtocolByte.checkCRC(ByteBuffer.wrap(input)), Arrays.toString(input));
    }
  }
}