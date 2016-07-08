package com.ak.hardware.rsce.comm.interceptor;

import javax.annotation.Nonnull;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public final class CRC16IBMChecksumTest {
  @DataProvider(name = "checksum")
  public static Object[][] ohms() {
    int[][] input = {
        {0x01, 0x05, 0x0C, 0x00, 0x00},
        {0x01, 0x05, 0x0C, 0x20, 0x4E},
        {0x01, 0x05, 0x0C, 0xE0, 0xB1},

        {0x01, 0x05, 0x0C, 0xA0, 0x0F},
        {0x01, 0x03, 0x00}
    };

    int[] expected = {
        0x0FD9, 0xFB40, 0xBB50, 0x0BE1, 0xF020
    };

    Object[][] values = new Object[expected.length][2];
    for (int i = 0; i < values.length; i++) {
      values[i][0] = input[i];
      values[i][1] = expected[i];
    }
    return values;
  }

  @Test(dataProvider = "checksum")
  public void testGetValue(@Nonnull int[] input, int expectedSum) {
    CRC16IBMChecksum checksum = new CRC16IBMChecksum();
    Assert.assertThrows(CloneNotSupportedException.class, checksum::clone);
    Assert.assertThrows(UnsupportedOperationException.class, () -> checksum.update(null, 0, 0));

    for (int i = 0, bytesLength = input.length; i < bytesLength; i++) {
      for (int b : input) {
        checksum.update(b);
      }
      Assert.assertEquals(checksum.getValue(), expectedSum, String.format("Test [%d], checksum %s", i, checksum));
      checksum.reset();
    }
  }
}