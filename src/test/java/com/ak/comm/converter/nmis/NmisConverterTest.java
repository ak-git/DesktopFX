package com.ak.comm.converter.nmis;

import java.nio.ByteBuffer;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.comm.bytes.nmis.NmisAddress;
import com.ak.comm.bytes.nmis.NmisResponseFrame;
import com.ak.comm.bytes.nmis.NmisTestProvider;
import com.ak.comm.converter.Converter;
import com.ak.comm.converter.Variables;
import com.ak.comm.log.LogTestUtils;
import org.testng.Assert;
import org.testng.annotations.Test;
import tec.uom.se.unit.MetricPrefix;
import tec.uom.se.unit.Units;

import static com.ak.util.LogUtils.LOG_LEVEL_VALUES;

public class NmisConverterTest {
  private static final Logger LOGGER = Logger.getLogger(NmisConverter.class.getName());
  private static final int[] EMPTY_INTS = {};

  @Test(dataProviderClass = NmisTestProvider.class, dataProvider = "aliveAndChannelsResponse")
  @ParametersAreNonnullByDefault
  public void testAliveAndChannelsResponse(NmisAddress address, byte[] input) {
    testConverter(address, input, EMPTY_INTS);
  }

  @Test(dataProviderClass = NmisTestProvider.class, dataProvider = "dataResponse")
  @ParametersAreNonnullByDefault
  public void testDataResponse(byte[] input, int[] expected) {
    testConverter(NmisAddress.DATA, input, expected);
  }

  @ParametersAreNonnullByDefault
  private static void testConverter(NmisAddress address, byte[] input, int[] expected) {
    NmisResponseFrame frame = new NmisResponseFrame.Builder(ByteBuffer.wrap(input)).build();
    if (NmisAddress.ALIVE == address) {
      Assert.assertNull(frame);
    }
    else {
      Assert.assertNotNull(frame);
      Assert.assertEquals(LogTestUtils.isSubstituteLogLevel(LOGGER, LOG_LEVEL_VALUES, () -> {
        Converter<NmisResponseFrame, NmisVariable> converter = new NmisConverter();
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
        for (NmisVariable v : NmisVariable.values()) {
          Assert.assertTrue(logRecord.getMessage().contains(Variables.toString(v)), logRecord.getMessage());
          Assert.assertEquals(v.getUnit(), MetricPrefix.MILLI(Units.SECOND));
        }
      }), expected.length > 0);
    }
  }
}