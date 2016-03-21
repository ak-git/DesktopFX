package com.ak.hardware.tnmi.comm.interceptor;

import org.testng.Assert;
import org.testng.annotations.Test;

public final class TnmiTest {
  @Test(dataProviderClass = TnmiTestProvider.class, dataProvider = "allOhmsMyoOff")
  public void testRequestOhms(TnmiRequest request, byte[] expected) {
    testRequest(request, expected);
  }

  @Test(dataProviderClass = TnmiTestProvider.class, dataProvider = "360OhmsMyoHz")
  public void testRequestMyo(TnmiRequest request, byte[] expected) {
    testRequest(request, expected);
  }

  @Test(dataProviderClass = TnmiTestProvider.class, dataProvider = "sequence")
  public void testRequestSequence(TnmiRequest request, byte[] expected) {
    testRequest(request, expected);
  }

  private static void testRequest(TnmiRequest request, byte[] expected) {
    Assert.assertEquals(request.getCodes(), expected, request.toString());
  }
}
