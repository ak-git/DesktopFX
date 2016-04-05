package com.ak.hardware.tnmi.comm.interceptor;

import java.util.Arrays;
import java.util.EnumSet;

import org.testng.Assert;
import org.testng.annotations.Test;

import static com.ak.hardware.tnmi.comm.interceptor.TnmiProtocolByte.LEN;
import static com.ak.hardware.tnmi.comm.interceptor.TnmiProtocolByte.MAX_CAPACITY;
import static com.ak.hardware.tnmi.comm.interceptor.TnmiProtocolByte.START;
import static com.ak.hardware.tnmi.comm.interceptor.TnmiProtocolByte.checkCRC;

public final class TnmiProtocolByteTest {
  @Test
  public void testIs() {
    Assert.assertFalse(START.is((byte) 0x00));
    Assert.assertTrue(START.is((byte) 0x7E));

    Assert.assertFalse(LEN.is((byte) (MAX_CAPACITY + 1)));
    Assert.assertTrue(LEN.is((byte) MAX_CAPACITY));

    for (TnmiProtocolByte b : EnumSet.complementOf(EnumSet.of(START, LEN))) {
      Assert.assertThrows(UnsupportedOperationException.class, () -> b.is((byte) 0x00));
    }
  }

  @Test(dataProviderClass = TnmiTestProvider.class, dataProvider = "allOhmsMyoOffResponse")
  public void testResponseOhmsCRC(TnmiRequest request, byte[] input) {
    Assert.assertTrue(checkCRC(Arrays.copyOfRange(input, 1, input.length)), Arrays.toString(input));
    Assert.assertFalse(checkCRC(input), Arrays.toString(input));
  }

  @Test(dataProviderClass = TnmiTestProvider.class, dataProvider = "360OhmsMyoHzResponse")
  public void testResponseMyoCRC(TnmiRequest request, byte[] input) {
    Assert.assertTrue(checkCRC(input), Arrays.toString(input));
  }

  @Test(dataProviderClass = TnmiTestProvider.class, dataProvider = "sequenceResponse")
  public void testResponseSequenceCRC(TnmiRequest request, byte[] input) {
    Assert.assertTrue(checkCRC(input), Arrays.toString(input));
  }

  @Test(dataProviderClass = TnmiTestProvider.class, dataProvider = "aliveAndChannelsResponse")
  public void testResponseAliveAndChannelsCRC(TnmiAddress address, byte[] input) {
    if (TnmiAddress.CHANNELS.contains(address)) {
      Assert.assertTrue(checkCRC(input), Arrays.toString(input));
    }
  }
}