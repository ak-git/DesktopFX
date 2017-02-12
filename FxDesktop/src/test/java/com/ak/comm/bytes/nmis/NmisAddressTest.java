package com.ak.comm.bytes.nmis;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.EnumSet;
import java.util.Optional;
import java.util.stream.BaseStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static com.ak.comm.bytes.nmis.NmisAddress.ALIVE;
import static com.ak.comm.bytes.nmis.NmisAddress.CATCH_ELBOW;
import static com.ak.comm.bytes.nmis.NmisAddress.CATCH_HAND;
import static com.ak.comm.bytes.nmis.NmisAddress.DATA;
import static com.ak.comm.bytes.nmis.NmisAddress.ROTATE_ELBOW;
import static com.ak.comm.bytes.nmis.NmisAddress.ROTATE_HAND;

public class NmisAddressTest {
  private static final byte[] EMPTY_BYTES = {};

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

  @DataProvider(name = "extractorsNone")
  public static Object[][] extractorsNone() {
    return new Object[][] {
        new Object[] {
            // alive
            new byte[] {0x7E, -12, 0x7E, 0x40, 0x08, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0xC7}
        },
        new Object[] {
            // channel 41 CATCH_ELBOW
            new byte[] {0x7E, 0x41, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0xC7}
        },
        new Object[] {
            // NO Data, empty frame
            new byte[] {0x7e, 0x45, 0x02, 0x3f, 0x00, 0x04}
        },
    };
  }

  @Test(dataProvider = "extractorsNone")
  public static void testExtractorNone(byte[] from) {
    Stream.of(NmisAddress.values()).forEach(address ->
        Assert.assertEquals(
            NmisAddress.Extractor.from(address, NmisAddress.FrameField.NONE).extract(ByteBuffer.wrap(from)).count(), 0));
  }

  @DataProvider(name = "extractorValues")
  public static Object[][] extractorValues() {
    return new Object[][] {
        new Object[] {
            // no Time, alive
            NmisAddress.Extractor.NONE,
            new byte[] {0x7E, -12, 0x7E, 0x40, 0x08, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0xC7},
            IntStream.empty()
        },
        new Object[] {
            // Time, but NO Data, empty frame
            NmisAddress.Extractor.DATA_TIME,
            new byte[] {0x7e, 0x45, 0x02, 0x3f, 0x00, 0x04},
            IntStream.of(0x3f)
        },
        new Object[] {
            // Time and Data
            NmisAddress.Extractor.DATA_DATA,
            new byte[] {0x7e, 0x45, 0x09, 0x44, 0x00, 0x01, 0x05, 0x0b, (byte) 0xe0, (byte) 0xb1, (byte) 0xe1, 0x7a, 0x0d},
            IntStream.empty()
        },
    };
  }

  @Test(dataProvider = "extractorValues")
  public static void testExtractorValues(NmisAddress.Extractor extractor, byte[] from, BaseStream<Integer, IntStream> expected) {
    Assert.assertEquals(extractor.extract(ByteBuffer.wrap(from).order(ByteOrder.LITTLE_ENDIAN)).iterator(), expected.iterator());
  }

  @DataProvider(name = "extractorToBuffer")
  public static Object[][] extractorToBuffer() {
    return new Object[][] {
        new Object[] {
            // no Data, alive
            NmisAddress.Extractor.NONE,
            new byte[] {0x7E, -12, 0x7E, 0x40, 0x08, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0xC7},
            EMPTY_BYTES
        },
        new Object[] {
            // no Data, empty frame
            NmisAddress.Extractor.DATA_TIME,
            new byte[] {0x7e, 0x45, 0x02, 0x3f, 0x00, 0x04},
            EMPTY_BYTES
        },
        new Object[] {
            // Time and Data
            NmisAddress.Extractor.DATA_DATA,
            new byte[] {0x7e, 0x45, 0x09, 0x44, 0x00, 0x01, 0x05, 0x0b, (byte) 0xe0, (byte) 0xb1, (byte) 0xe1, 0x7a, 0x0d},
            new byte[] {0x01, 0x05, 0x0b, (byte) 0xe0, (byte) 0xb1, (byte) 0xe1, 0x7a}
        },
    };
  }

  @Test(dataProvider = "extractorToBuffer")
  public static void testExtractorToBuffer(NmisAddress.Extractor extractor, byte[] from, byte[] expected) {
    ByteBuffer destination = ByteBuffer.allocate(expected.length);
    extractor.extract(ByteBuffer.wrap(from), destination);
    Assert.assertEquals(destination.array(), expected);
  }
}