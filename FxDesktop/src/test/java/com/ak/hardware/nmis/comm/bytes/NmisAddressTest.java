package com.ak.hardware.nmis.comm.bytes;

import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.Optional;

import org.testng.Assert;
import org.testng.annotations.Test;

import static com.ak.hardware.nmis.comm.bytes.NmisAddress.ALIVE;
import static com.ak.hardware.nmis.comm.bytes.NmisAddress.CATCH_ELBOW;
import static com.ak.hardware.nmis.comm.bytes.NmisAddress.CATCH_HAND;
import static com.ak.hardware.nmis.comm.bytes.NmisAddress.DATA;
import static com.ak.hardware.nmis.comm.bytes.NmisAddress.ROTATE_ELBOW;
import static com.ak.hardware.nmis.comm.bytes.NmisAddress.ROTATE_HAND;

public final class NmisAddressTest {
  @Test
  public void testGetAddrRequest() {
    EnumSet<NmisAddress> bad = EnumSet.of(ALIVE, CATCH_ELBOW, ROTATE_ELBOW, CATCH_HAND, ROTATE_HAND);
    for (NmisAddress address : bad) {
      Assert.assertThrows(UnsupportedOperationException.class, address::getAddrRequest);
    }
    for (NmisAddress address : EnumSet.complementOf(bad)) {
      if (address == DATA) {
        Assert.assertEquals(address.getAddrRequest(), address.getAddrResponse());
      }
      else {
        Assert.assertNotEquals(address.getAddrRequest(), address.getAddrResponse());
      }
    }
  }

  @Test(dataProviderClass = NmisTestProvider.class, dataProvider = "aliveAndChannelsResponse")
  public void testFind(NmisAddress address, byte[] input) {
    Assert.assertEquals(Optional.ofNullable(NmisAddress.find(ByteBuffer.wrap(input))).orElse(ALIVE), address);
  }
}