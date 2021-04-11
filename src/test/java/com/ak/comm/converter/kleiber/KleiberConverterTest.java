package com.ak.comm.converter.kleiber;

import java.nio.ByteOrder;
import java.util.Collections;
import java.util.EnumSet;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.converter.FloatToIntegerConverter;
import com.ak.comm.converter.Variable;
import com.ak.comm.converter.Variables;
import com.ak.comm.log.LogTestUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import static com.ak.comm.core.LogUtils.LOG_LEVEL_VALUES;

public class KleiberConverterTest {
  private static final Logger LOGGER = Logger.getLogger(FloatToIntegerConverter.class.getName());

  @Test
  public void testDataResponse() {
    testConverter(new byte[] {
        (byte) 0xaa,
        0, 0, (byte) 0x80, 0x3f,
        0, 0, (byte) 0x80, 0x3f,
        0, 0, (byte) 0x80, 0x3f,
        0, 0, (byte) 0x80, 0x3f,
        0, 0, (byte) 0x80, 0x3f,
        0, 0, (byte) 0x80, 0x3f,
        0, 0, (byte) 0x80, 0x3f,
        0, 0, (byte) 0xfa, (byte) 0xc2,
        (byte) 0xbb,

        (byte) 0xaa,
    }, new int[] {10, 10, 10, 10, 10, 10, 10, 0});
  }

  @ParametersAreNonnullByDefault
  private static void testConverter(byte[] input, int[] expected) {
    BufferFrame frame = new BufferFrame(input, ByteOrder.LITTLE_ENDIAN);
    Assert.assertNotNull(frame);
    Assert.assertTrue(LogTestUtils.isSubstituteLogLevel(LOGGER, LOG_LEVEL_VALUES,
        () -> {
          FloatToIntegerConverter<KleiberVariable> converter = new FloatToIntegerConverter<>(KleiberVariable.class, 1000);
          Stream<int[]> stream = converter.apply(frame);
          stream.forEach(ints -> Assert.assertEquals(ints, expected));
        },
        logRecord -> {
          for (KleiberVariable v : KleiberVariable.values()) {
            Assert.assertTrue(logRecord.getMessage().contains(Variables.toString(v)), logRecord.getMessage());
          }
        }));
  }

  @Test
  public void testVariableProperties() {
    EnumSet.allOf(KleiberVariable.class)
        .forEach(variable -> Assert.assertEquals(variable.options(), Collections.singleton(Variable.Option.VISIBLE)));
    EnumSet.allOf(KleiberVariable.class)
        .forEach(variable -> Assert.assertEquals(variable.getUnit(), KleiberVariable.M1.getUnit()));
  }
}