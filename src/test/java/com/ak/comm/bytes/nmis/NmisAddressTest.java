package com.ak.comm.bytes.nmis;

import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.BaseStream;
import java.util.stream.IntStream;

import com.ak.comm.bytes.LogUtils;
import com.ak.comm.log.LogTestUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import static com.ak.comm.bytes.nmis.NmisAddress.ALIVE;
import static com.ak.comm.bytes.nmis.NmisAddress.CATCH_ELBOW;
import static com.ak.comm.bytes.nmis.NmisAddress.CATCH_HAND;
import static com.ak.comm.bytes.nmis.NmisAddress.DATA;
import static com.ak.comm.bytes.nmis.NmisAddress.ROTATE_ELBOW;
import static com.ak.comm.bytes.nmis.NmisAddress.ROTATE_HAND;

public class NmisAddressTest {
  private static final Logger LOGGER = Logger.getLogger(NmisAddress.class.getName());

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

  @Test(dataProviderClass = NmisTestProvider.class, dataProvider = "nullResponse")
  public void testNotFound(byte[] input) {
    Assert.assertTrue(LogTestUtils.isSubstituteLogLevel(LOGGER, LogUtils.LOG_LEVEL_ERRORS,
        () -> Assert.assertNull(NmisAddress.find(ByteBuffer.wrap(input))),
        logRecord -> Assert.assertTrue(logRecord.getMessage().endsWith("Address -12 not found"), logRecord.getMessage()))
    );
  }

  @Test(dataProviderClass = NmisTestProvider.class, dataProvider = "aliveAndChannelsResponse")
  public void testFind(NmisAddress address, byte[] input) {
    Assert.assertEquals(Objects.requireNonNull(NmisAddress.find(ByteBuffer.wrap(input))), address);
  }

  @Test(dataProviderClass = NmisExtractorTestProvider.class, dataProvider = "extractNone")
  public void testExtractorNone(byte[] from) {
    NmisResponseFrame response = new NmisResponseFrame.Builder(ByteBuffer.wrap(from)).build();
    Assert.assertNotNull(response);
    Assert.assertEquals(response.extractTime().count(), 0);
    ByteBuffer destination = ByteBuffer.allocate(from.length);
    response.extractData(destination);
    Assert.assertEquals(destination.position(), 0);
  }

  @Test(dataProviderClass = NmisExtractorTestProvider.class, dataProvider = "extractTime")
  public void testExtractorValues(byte[] from, BaseStream<Integer, IntStream> expected) {
    Optional.ofNullable(new NmisResponseFrame.Builder(ByteBuffer.wrap(from)).build())
        .ifPresent(response -> Assert.assertEquals(response.extractTime().iterator(), expected.iterator()));
  }

  @Test(dataProviderClass = NmisExtractorTestProvider.class, dataProvider = "extractData")
  public void testExtractorToBuffer(byte[] from, byte[] expected) {
    ByteBuffer destination = ByteBuffer.allocate(expected.length);
    Optional.ofNullable(new NmisResponseFrame.Builder(ByteBuffer.wrap(from)).build())
        .ifPresent(response -> {
          response.extractData(destination);
          Assert.assertEquals(destination.array(), expected);
        });
  }
}