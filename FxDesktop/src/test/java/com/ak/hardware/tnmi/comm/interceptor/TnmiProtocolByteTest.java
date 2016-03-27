package com.ak.hardware.tnmi.comm.interceptor;

import java.util.Arrays;
import java.util.EnumSet;

import org.testng.Assert;
import org.testng.annotations.Test;

public final class TnmiProtocolByteTest {
  @Test
  public void testIs() {
    Assert.assertFalse(TnmiProtocolByte.START.is((byte) 0x00));
    Assert.assertTrue(TnmiProtocolByte.START.is((byte) 0x7E));

    Assert.assertFalse(TnmiProtocolByte.LEN.is((byte) (TnmiProtocolByte.MAX_CAPACITY + 1)));
    Assert.assertTrue(TnmiProtocolByte.LEN.is((byte) TnmiProtocolByte.MAX_CAPACITY));

    for (TnmiProtocolByte b : EnumSet.complementOf(EnumSet.of(TnmiProtocolByte.START, TnmiProtocolByte.LEN))) {
      Assert.assertThrows(UnsupportedOperationException.class, () -> b.is((byte) 0x00));
    }
  }

  @Test(dataProviderClass = TnmiTestProvider.class, dataProvider = "allOhmsMyoOffResponse")
  public void testResponseOhmsCRC(TnmiRequest request, byte[] input) {
    Assert.assertTrue(TnmiProtocolByte.checkCRC(Arrays.copyOfRange(input, 1, input.length)), Arrays.toString(input));
    Assert.assertFalse(TnmiProtocolByte.checkCRC(input), Arrays.toString(input));
  }

  @Test(dataProviderClass = TnmiTestProvider.class, dataProvider = "360OhmsMyoHzResponse")
  public void testResponseMyoCRC(TnmiRequest request, byte[] input) {
    Assert.assertTrue(TnmiProtocolByte.checkCRC(input), Arrays.toString(input));
  }

  @Test(dataProviderClass = TnmiTestProvider.class, dataProvider = "sequenceResponse")
  public void testResponseSequenceCRC(TnmiRequest request, byte[] input) {
    Assert.assertTrue(TnmiProtocolByte.checkCRC(input), Arrays.toString(input));
  }

  @Test(dataProviderClass = TnmiTestProvider.class, dataProvider = "aliveAndChannelsResponse")
  public void testResponseAliveAndChannelsCRC(TnmiAddress address, byte[] input) {
    if (TnmiAddress.CHANNELS.contains(address)) {
      Assert.assertTrue(TnmiProtocolByte.checkCRC(input), Arrays.toString(input));
    }
  }
}