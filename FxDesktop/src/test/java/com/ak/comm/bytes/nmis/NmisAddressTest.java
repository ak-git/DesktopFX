package com.ak.comm.bytes.nmis;

import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.Optional;

import org.testng.Assert;
import org.testng.annotations.Test;

import static com.ak.comm.bytes.nmis.NmisAddress.ALIVE;
import static com.ak.comm.bytes.nmis.NmisAddress.CATCH_ELBOW;
import static com.ak.comm.bytes.nmis.NmisAddress.CATCH_HAND;
import static com.ak.comm.bytes.nmis.NmisAddress.DATA;
import static com.ak.comm.bytes.nmis.NmisAddress.ROTATE_ELBOW;
import static com.ak.comm.bytes.nmis.NmisAddress.ROTATE_HAND;

public class NmisAddressTest {
  private NmisAddressTest() {
  }

  @Test
  public static void testGetAddrRequest() {
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
  public static void testFind(NmisAddress address, byte[] input) {
    Assert.assertEquals(Optional.ofNullable(NmisAddress.find(ByteBuffer.wrap(input))).orElse(ALIVE), address);
  }
}