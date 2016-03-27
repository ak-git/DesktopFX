package com.ak.hardware.tnmi.comm.interceptor;

import java.util.EnumSet;
import java.util.Optional;

import org.testng.Assert;
import org.testng.annotations.Test;

import static com.ak.hardware.tnmi.comm.interceptor.TnmiAddress.ALIVE;
import static com.ak.hardware.tnmi.comm.interceptor.TnmiAddress.CATCH_ELBOW;
import static com.ak.hardware.tnmi.comm.interceptor.TnmiAddress.CATCH_HAND;
import static com.ak.hardware.tnmi.comm.interceptor.TnmiAddress.DATA;
import static com.ak.hardware.tnmi.comm.interceptor.TnmiAddress.ROTATE_ELBOW;
import static com.ak.hardware.tnmi.comm.interceptor.TnmiAddress.ROTATE_HAND;

public final class TnmiAddressTest {
  @Test
  public <E> void testGetAddrRequest() {
    EnumSet<TnmiAddress> bad = EnumSet.of(ALIVE, CATCH_ELBOW, ROTATE_ELBOW, CATCH_HAND, ROTATE_HAND);
    for (TnmiAddress address : bad) {
      Assert.assertThrows(UnsupportedOperationException.class, address::getAddrRequest);
    }
    for (TnmiAddress address : EnumSet.complementOf(bad)) {
      if (address == DATA) {
        Assert.assertEquals(address.getAddrRequest(), address.getAddrResponse());
      }
      else {
        Assert.assertNotEquals(address.getAddrRequest(), address.getAddrResponse());
      }
    }
  }

  @Test(dataProviderClass = TnmiTestProvider.class, dataProvider = "aliveAndChannelsResponse")
  public void testFind(TnmiAddress address, byte[] input) {
    Assert.assertEquals(Optional.ofNullable(TnmiAddress.find(input)).orElse(ALIVE), address);
  }
}