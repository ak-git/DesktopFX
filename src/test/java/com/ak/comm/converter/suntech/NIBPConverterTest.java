package com.ak.comm.converter.suntech;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.EnumSet;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.comm.bytes.suntech.NIBPResponse;
import com.ak.comm.converter.Converter;
import com.ak.comm.converter.Variable;
import com.ak.comm.converter.Variables;
import com.ak.comm.log.LogTestUtils;
import org.testng.Assert;
import org.testng.annotations.Test;
import tec.uom.se.AbstractUnit;
import tec.uom.se.unit.Units;

import static com.ak.util.LogUtils.LOG_LEVEL_VALUES;

public class NIBPConverterTest {
  private static final Logger LOGGER = Logger.getLogger(NIBPConverter.class.getName());

  @Test
  public void testDataResponse() {
    testConverter(
        // Module response for a cuff pressure of 258mmHg:
        new byte[] {
            0x3E, // MODULE START BYTE = the ">" character (0x3E)
            0x05, // total number of bytes in the packet
            0x02, 0x01, // 258 mm Hg
            (byte) 0xBA // 0x100 - modulo 256 (Start byte + Packet byte + Data bytes)
        }, new int[] {
            258, 0, 0, 0, 0, 0
        });

    testConverter(
        // The Module will reply with a data packet containing 21 data bytes,
        // consisting of systolic, diastolic, heart rate, and other parameter data.
        // Total packet - 24 bytes
        new byte[] {
            0x3e, // MODULE START BYTE = the ">" character (0x3E)
            0x18, // 24 - total number of bytes in the packet

            0x73, 0x00, // 115 - Systolic value in mmHg (unsigned integer, LSB first)
            0x4a, 0x00, // 74 - Diastolic value in mmHg (unsigned integer, LSB first)
            0x1e, // 30 - Number of Heart Beats detected during the BP sample (unsigned byte)
            0x01, // BP Status (unsigned byte)

            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x31, 0x07, // Test Codes [ ] (8 unsigned bytes, TC [0] through TC [7] )
            0x55, // 85 - Heart Rate in beats per minute (unsigned byte)
            0x00, // Spare byte (not used)
            0x58, 0x00, // 88 - Mean Arterial Pressure (MAP) in mmHg (unsigned integer, LSB first)
            0x00, // Error Code (unsigned byte)
            0x00, // Spare byte (not used)
            0x00, // Spare byte (not used)
            (byte) 0xe9 // 0x100 - modulo 256 (Start byte + Packet byte + Data bytes)
        }, new int[] {
            0, 115, 74, 85, 88, 0
        });
  }

  @Test
  public void testInvalidFrame() {
    byte[] input = {
        0x3E, // MODULE START BYTE = the ">" character (0x3E)
        0x00, // INVALID
        0x02, 0x01, // 258 mm Hg
        (byte) 0xBA // 0x100 - modulo 256 (Start byte + Packet byte + Data bytes)
    };
    Assert.assertNull(new NIBPResponse.Builder(ByteBuffer.wrap(input)).build());
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
      }
    }), expected.length > 0);
  }

  @Test
  public void testVariableProperties() {
    EnumSet.complementOf(EnumSet.of(NIBPVariable.PRESSURE, NIBPVariable.IS_COMPLETED))
        .forEach(variable -> Assert.assertEquals(variable.options(), Collections.singleton(Variable.Option.TEXT_VALUE_BANNER)));
    EnumSet.complementOf(EnumSet.of(NIBPVariable.PULSE))
        .forEach(variable -> Assert.assertEquals(variable.getUnit(), NIBPVariable.PRESSURE.getUnit()));
    Assert.assertEquals(NIBPVariable.PULSE.getUnit(), AbstractUnit.ONE.divide(Units.MINUTE));
    Assert.assertEquals(NIBPVariable.IS_COMPLETED.options(), Collections.emptySet());
  }
}