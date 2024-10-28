package com.ak.appliance.kleiber.comm.converter;

import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.converter.FloatToIntegerConverter;
import com.ak.comm.converter.Variable;
import com.ak.comm.converter.Variables;
import com.ak.comm.logging.LogTestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.nio.ByteOrder;
import java.util.logging.Logger;

import static com.ak.comm.bytes.LogUtils.LOG_LEVEL_VALUES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KleiberConverterTest {
  private static final Logger LOGGER = Logger.getLogger(FloatToIntegerConverter.class.getName());

  @Test
  void testDataResponse() {
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

  private static void testConverter(byte[] input, int[] expected) {
    BufferFrame frame = new BufferFrame(input, ByteOrder.LITTLE_ENDIAN);
    assertNotNull(frame);
    assertTrue(
        LogTestUtils.isSubstituteLogLevel(LOGGER, LOG_LEVEL_VALUES,
            () -> {
              FloatToIntegerConverter<KleiberVariable> converter = new FloatToIntegerConverter<>(KleiberVariable.class, 1000);
              assertThat(converter.apply(frame)).isNotEmpty().allSatisfy(ints -> assertThat(ints).containsExactly(expected));
            },
            logRecord -> {
              for (KleiberVariable v : KleiberVariable.values()) {
                assertTrue(logRecord.getMessage().contains(Variables.toString(v)), logRecord::getMessage);
              }
            })
    );
  }

  @ParameterizedTest
  @EnumSource(value = KleiberVariable.class)
  void testVariableProperties(Variable<KleiberVariable> variable) {
    assertThat(variable.options()).containsExactly(Variable.Option.VISIBLE);
    assertThat(variable.getUnit()).isEqualTo(KleiberVariable.M1.getUnit());
  }
}