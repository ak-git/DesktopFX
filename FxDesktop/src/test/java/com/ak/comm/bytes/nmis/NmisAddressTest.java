package com.ak.comm.bytes.nmis;

import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.Optional;
import java.util.stream.BaseStream;
import java.util.stream.IntStream;

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

  @Test(dataProviderClass = NmisExtractorTestProvider.class, dataProvider = "extractNone")
  public static void testExtractorNone(byte[] from) {
    NmisResponseFrame response = new NmisResponseFrame.Builder(ByteBuffer.wrap(from)).build();
    Assert.assertNotNull(response);
    Assert.assertEquals(response.extractTime().count(), 0);
    ByteBuffer destination = ByteBuffer.allocate(from.length);
    response.extractData(destination);
    Assert.assertEquals(destination.position(), 0);
  }

  @Test(dataProviderClass = NmisExtractorTestProvider.class, dataProvider = "extractTime")
  public static void testExtractorValues(byte[] from, BaseStream<Integer, IntStream> expected) {
    Optional.ofNullable(new NmisResponseFrame.Builder(ByteBuffer.wrap(from)).build())
        .ifPresent(response -> Assert.assertEquals(response.extractTime().iterator(), expected.iterator()));
  }

  @Test(dataProviderClass = NmisExtractorTestProvider.class, dataProvider = "extractData")
  public static void testExtractorToBuffer(byte[] from, byte[] expected) {
    ByteBuffer destination = ByteBuffer.allocate(expected.length);
    Optional.ofNullable(new NmisResponseFrame.Builder(ByteBuffer.wrap(from)).build())
        .ifPresent(response -> {
          response.extractData(destination);
          Assert.assertEquals(destination.array(), expected);
        });
  }
}