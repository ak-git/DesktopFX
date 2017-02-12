package com.ak.comm.bytes.nmis;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.EnumSet;
import java.util.Optional;
import java.util.stream.BaseStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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

  @Test(dataProviderClass = NmisExtractorTestProvider.class, dataProvider = "extractorsNone")
  public static void testExtractorNone(byte[] from) {
    Stream.of(NmisAddress.values()).forEach(address ->
        Assert.assertEquals(
            NmisAddress.Extractor.from(address, NmisAddress.FrameField.NONE).extract(ByteBuffer.wrap(from)).count(), 0));
  }

  @Test(dataProviderClass = NmisExtractorTestProvider.class, dataProvider = "extractorValues")
  public static void testExtractorValues(NmisAddress.Extractor extractor, byte[] from, BaseStream<Integer, IntStream> expected) {
    Assert.assertEquals(extractor.extract(ByteBuffer.wrap(from).order(ByteOrder.LITTLE_ENDIAN)).iterator(), expected.iterator());
  }

  @Test(dataProviderClass = NmisExtractorTestProvider.class, dataProvider = "extractorToBuffer")
  public static void testExtractorToBuffer(NmisAddress.Extractor extractor, byte[] from, byte[] expected) {
    ByteBuffer destination = ByteBuffer.allocate(expected.length);
    extractor.extract(ByteBuffer.wrap(from), destination);
    Assert.assertEquals(destination.array(), expected);
  }
}