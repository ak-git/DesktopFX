package com.ak.comm.bytes.nmis;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.EnumSet;

import javax.annotation.Nonnull;

import org.testng.Assert;
import org.testng.annotations.Test;

public class NmisProtocolByteTest {
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
  public void testResponseOhmsCRC(@Nonnull NmisRequest request, @Nonnull byte[] input) {
    Assert.assertNotNull(request);
    Assert.assertTrue(NmisProtocolByte.checkCRC(ByteBuffer.wrap(Arrays.copyOfRange(input, 1, input.length))), Arrays.toString(input));
    Assert.assertFalse(NmisProtocolByte.checkCRC(ByteBuffer.wrap(input)), Arrays.toString(input));
  }

  @Test(dataProviderClass = NmisTestProvider.class, dataProvider = "360OhmsMyoHzResponse")
  public void testResponseMyoCRC(@Nonnull NmisRequest request, @Nonnull byte[] input) {
    Assert.assertNotNull(request);
    Assert.assertTrue(NmisProtocolByte.checkCRC(ByteBuffer.wrap(input)), Arrays.toString(input));
  }

  @Test(dataProviderClass = NmisTestProvider.class, dataProvider = "sequenceResponse")
  public void testResponseSequenceCRC(@Nonnull NmisRequest request, @Nonnull byte[] input) {
    Assert.assertNotNull(request);
    Assert.assertTrue(NmisProtocolByte.checkCRC(ByteBuffer.wrap(input)), Arrays.toString(input));
  }

  @Test(dataProviderClass = NmisTestProvider.class, dataProvider = "aliveAndChannelsResponse")
  public void testResponseAliveAndChannelsCRC(@Nonnull NmisAddress address, @Nonnull byte[] input) {
    if (NmisAddress.CHANNELS.contains(address)) {
      Assert.assertTrue(NmisProtocolByte.checkCRC(ByteBuffer.wrap(input)), Arrays.toString(input));
    }
  }
}