package com.ak.hardware.tnmi.comm.interceptor;

import java.util.Arrays;

import org.testng.Assert;
import org.testng.annotations.Test;

public final class TnmiResponseTest {
  @Test(dataProviderClass = TnmiTestProvider.class, dataProvider = "invalidTestByteResponse")
  public void testNewInstance(byte[] input) {
    Assert.assertNotNull(TnmiAddress.find(input), Arrays.toString(input));
    Assert.assertTrue(TnmiProtocolByte.checkCRC(input), Arrays.toString(input));
    Assert.assertNull(TnmiResponse.newInstance(input), Arrays.toString(input));
  }

  @Test(dataProviderClass = TnmiTestProvider.class, dataProvider = "sequenceResponse")
  public void testEquals(TnmiRequest request, byte[] input) {
    TnmiResponse tnmiResponse = TnmiResponse.newInstance(input);
    Assert.assertNotNull(tnmiResponse);
    Assert.assertNotEquals(request, tnmiResponse, Arrays.toString(input));
    Assert.assertTrue(tnmiResponse.equals(tnmiResponse), tnmiResponse.toString());
    Assert.assertEquals(request.toResponse().hashCode(), tnmiResponse.hashCode());
  }
}