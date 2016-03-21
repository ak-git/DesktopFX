package com.ak.hardware.tnmi.comm.interceptor;

import java.util.Arrays;
import java.util.Optional;

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

  @Test(dataProviderClass = TnmiTestProvider.class, dataProvider = "allOhmsMyoOffResponse")
  public void testResponseOhms(TnmiRequest request, byte[] input) {
    Assert.assertEquals(request.toResponse(), TnmiResponse.newInstance(Arrays.copyOfRange(input, 1, input.length)));
  }

  @Test(dataProviderClass = TnmiTestProvider.class, dataProvider = "360OhmsMyoHzResponse")
  public void testResponseMyo(TnmiRequest request, byte[] input) {
    Assert.assertEquals(request.toResponse(), TnmiResponse.newInstance(input));
  }

  @Test(dataProviderClass = TnmiTestProvider.class, dataProvider = "sequenceResponse", timeOut = 10000)
  public void testResponseSequence(TnmiRequest request, byte[] input) {
    Assert.assertEquals(request.toResponse(), TnmiResponse.newInstance(input));
  }

  @Test(dataProviderClass = TnmiTestProvider.class, dataProvider = "aliveAndChannelsResponse", timeOut = 10000)
  public void testResponseAliveAndChannels(TnmiAddress address, byte[] input) {
    if (TnmiAddress.CHANNELS.contains(address)) {
      Optional.ofNullable(TnmiResponse.newInstance(input)).orElseThrow(NullPointerException::new);
    }
  }

  private static void testRequest(TnmiRequest request, byte[] expected) {
    Assert.assertEquals(request.getCodes(), expected, request.toString());
  }
}