package com.ak.comm.bytes.nmis;

import java.util.stream.IntStream;

import org.testng.annotations.DataProvider;

public class NmisExtractorTestProvider {
  private static final byte[] EMPTY_BYTES = {};

  private NmisExtractorTestProvider() {
  }

  @DataProvider(name = "extractNone")
  public static Object[][] extractNone() {
    return new Object[][] {
        new Object[] {
            // channel 41 CATCH_ELBOW
            new byte[] {0x7E, 0x41, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0xC7}
        },
    };
  }

  @DataProvider(name = "extractTime")
  public static Object[][] extractTime() {
    return new Object[][] {
        new Object[] {
            // no Time, alive
            new byte[] {0x7E, -12, 0x7E, 0x40, 0x08, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0xC7},
            IntStream.empty()
        },
        new Object[] {
            // Time, but NO Data, empty frame
            new byte[] {0x7e, 0x45, 0x02, 0x3f, 0x00, 0x04},
            IntStream.of(0x3f)
        },
        new Object[] {
            // Time and Data
            new byte[] {0x7e, 0x45, 0x09, 0x44, 0x00, 0x01, 0x05, 0x0b, (byte) 0xe0, (byte) 0xb1, (byte) 0xe1, 0x7a, 0x0d},
            IntStream.of(0x44)
        },
    };
  }

  @DataProvider(name = "extractData")
  public static Object[][] extractData() {
    return new Object[][] {
        new Object[] {
            // no Data, alive
            new byte[] {0x7E, -12, 0x7E, 0x40, 0x08, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0xC7},
            EMPTY_BYTES
        },
        new Object[] {
            // no Data, empty frame
            new byte[] {0x7e, 0x45, 0x02, 0x3f, 0x00, 0x04},
            EMPTY_BYTES
        },
        new Object[] {
            // Time and Data
            new byte[] {0x7e, 0x45, 0x09, 0x44, 0x00, 0x01, 0x05, 0x0b, (byte) 0xe0, (byte) 0xb1, (byte) 0xe1, 0x7a, 0x0d},
            new byte[] {0x01, 0x05, 0x0b, (byte) 0xe0, (byte) 0xb1, (byte) 0xe1, 0x7a}
        },
    };
  }
}
