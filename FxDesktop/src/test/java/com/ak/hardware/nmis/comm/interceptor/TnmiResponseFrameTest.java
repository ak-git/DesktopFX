package com.ak.hardware.nmis.comm.interceptor;

import java.util.Arrays;

import org.testng.Assert;
import org.testng.annotations.Test;

public final class TnmiResponseFrameTest {
  @Test(dataProviderClass = TnmiTestProvider.class, dataProvider = "invalidTestByteResponse")
  public void testNewInstance(byte[] input) {
    Assert.assertNotNull(TnmiAddress.find(input), Arrays.toString(input));
    Assert.assertTrue(TnmiProtocolByte.checkCRC(input), Arrays.toString(input));
    Assert.assertNull(TnmiResponseFrame.newInstance(input), Arrays.toString(input));
  }

  @Test(dataProviderClass = TnmiTestProvider.class, dataProvider = "sequenceResponse")
  public void testEquals(TnmiRequest request, byte[] input) {
    TnmiResponseFrame tnmiResponseFrame = TnmiResponseFrame.newInstance(input);
    Assert.assertNotNull(tnmiResponseFrame);
    Assert.assertNotEquals(request, tnmiResponseFrame, Arrays.toString(input));
    Assert.assertTrue(tnmiResponseFrame.equals(tnmiResponseFrame), tnmiResponseFrame.toString());
    Assert.assertEquals(request.toResponse().hashCode(), tnmiResponseFrame.hashCode());
  }
}