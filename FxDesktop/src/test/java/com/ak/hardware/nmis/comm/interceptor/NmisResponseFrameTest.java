package com.ak.hardware.nmis.comm.interceptor;

import java.util.Arrays;

import org.testng.Assert;
import org.testng.annotations.Test;

public final class NmisResponseFrameTest {
  @Test(dataProviderClass = NmisTestProvider.class, dataProvider = "invalidTestByteResponse")
  public void testNewInstance(byte[] input) {
    Assert.assertNotNull(NmisAddress.find(input), Arrays.toString(input));
    Assert.assertTrue(NmisProtocolByte.checkCRC(input), Arrays.toString(input));
    Assert.assertNull(NmisResponseFrame.newInstance(input), Arrays.toString(input));
  }

  @Test(dataProviderClass = NmisTestProvider.class, dataProvider = "sequenceResponse")
  public void testEquals(NmisRequest request, byte[] input) {
    NmisResponseFrame nmisResponseFrame = NmisResponseFrame.newInstance(input);
    Assert.assertNotNull(nmisResponseFrame);
    Assert.assertNotEquals(request, nmisResponseFrame, Arrays.toString(input));
    Assert.assertTrue(nmisResponseFrame.equals(nmisResponseFrame), nmisResponseFrame.toString());
    Assert.assertEquals(request.toResponse().hashCode(), nmisResponseFrame.hashCode());
  }
}