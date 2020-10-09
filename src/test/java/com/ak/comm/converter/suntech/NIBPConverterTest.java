package com.ak.comm.converter.suntech;

import java.nio.ByteBuffer;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.comm.bytes.suntech.NIBPResponse;
import com.ak.comm.converter.Converter;
import com.ak.comm.converter.Variables;
import com.ak.comm.log.LogTestUtils;
import org.testng.Assert;
import org.testng.annotations.Test;
import tec.uom.se.unit.Units;

import static com.ak.util.LogUtils.LOG_LEVEL_VALUES;

public class NIBPConverterTest {
  private static final Logger LOGGER = Logger.getLogger(NIBPConverter.class.getName());

  @Test
  public void testDataResponse() {
    testConverter(new byte[] {0x3E, 0x05, 0x02, 0x01, (byte) 0xBA}, new int[] {258});
  }

  @ParametersAreNonnullByDefault
  private static void testConverter(byte[] input, int[] expected) {
    NIBPResponse frame = new NIBPResponse.Builder(ByteBuffer.wrap(input)).build();
    Assert.assertNotNull(frame);
    Assert.assertEquals(LogTestUtils.isSubstituteLogLevel(LOGGER, LOG_LEVEL_VALUES, () -> {
      Converter<NIBPResponse, NIBPVariable> converter = new NIBPConverter();
      Stream<int[]> stream = converter.apply(frame);
      if (expected.length == 0) {
        Assert.assertEquals(stream.count(), 0);
      }
      else {
        stream.forEach(ints -> Assert.assertEquals(ints, expected));
      }
    }, logRecord -> {
      for (int v : expected) {
        Assert.assertTrue(logRecord.getMessage().contains(Integer.toString(v)));
      }
      for (NIBPVariable v : NIBPVariable.values()) {
        Assert.assertTrue(logRecord.getMessage().contains(Variables.toString(v)), logRecord.getMessage());
        Assert.assertEquals(v.getUnit().getSystemUnit(), Units.PASCAL);
      }
    }), expected.length > 0);
  }
}