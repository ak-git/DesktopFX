package com.ak.appliance.suntech.comm.converter;

import com.ak.appliance.suntech.comm.bytes.NIBPResponse;
import com.ak.comm.bytes.BufferFrame;
import com.ak.comm.converter.Converter;
import com.ak.comm.converter.Variable;
import com.ak.comm.converter.Variables;
import com.ak.comm.logging.LogTestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import tech.units.indriya.AbstractUnit;
import tech.units.indriya.unit.Units;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static com.ak.comm.bytes.LogUtils.LOG_LEVEL_ERRORS;
import static com.ak.comm.bytes.LogUtils.LOG_LEVEL_VALUES;
import static org.assertj.core.api.Assertions.assertThat;

class NIBPConverterTest {
  private static final Logger LOGGER = Logger.getLogger(NIBPConverter.class.getName());

  @Test
  void testDataResponse() {
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

  private static void testConverter(byte[] input, int[] expected) {
    NIBPResponse frame = new NIBPResponse.Builder(ByteBuffer.wrap(input)).build().orElseThrow();
    assertThat(
        LogTestUtils.isSubstituteLogLevel(LOGGER, LOG_LEVEL_VALUES, () -> {
          Converter<NIBPResponse, NIBPVariable> converter = new NIBPConverter();
          Stream<int[]> stream = converter.apply(frame);
          if (expected.length == 0) {
            assertThat(stream.count()).isZero();
          }
          else {
            assertThat(stream).isNotEmpty().allSatisfy(ints -> assertThat(ints).containsExactly(expected));
          }
        }, logRecord -> {
          for (int v : expected) {
            assertThat(logRecord.getMessage()).contains(Integer.toString(v));
          }
          for (NIBPVariable v : NIBPVariable.values()) {
            assertThat(logRecord.getMessage()).contains(Variables.toString(v));
          }
        }))
        .isEqualTo(expected.length > 0);
  }

  @ParameterizedTest
  @EnumSource(mode = EnumSource.Mode.EXCLUDE, value = NIBPVariable.class, names = "IS_COMPLETED")
  void testTexValueBanner(Variable<NIBPVariable> variable) {
    assertThat(variable.options()).contains(Variable.Option.TEXT_VALUE_BANNER);
  }

  @ParameterizedTest
  @EnumSource(mode = EnumSource.Mode.EXCLUDE, value = NIBPVariable.class, names = "PULSE")
  void testGetUnit(Variable<NIBPVariable> variable) {
    assertThat(variable.getUnit()).isEqualTo(NIBPVariable.PRESSURE.getUnit());
  }

  @Test
  void testVariableProperties() {
    assertThat(NIBPVariable.PULSE.getUnit()).isEqualTo(AbstractUnit.ONE.divide(Units.MINUTE));
    assertThat(NIBPVariable.IS_COMPLETED.options()).isEmpty();
  }

  @Nested
  class NIBPConverterTestLogger {
    private static final Logger LOGGER = Logger.getLogger(BufferFrame.class.getName());
    private final AtomicInteger exceptionCounter = new AtomicInteger();

    @BeforeEach
    void setUp() {
      LOGGER.setFilter(r -> {
        assertThat(r.getThrown()).isNull();
        exceptionCounter.incrementAndGet();
        return false;
      });
      LOGGER.setLevel(LOG_LEVEL_ERRORS);
    }

    @AfterEach
    void tearDown() {
      LOGGER.setFilter(null);
      LOGGER.setLevel(Level.INFO);
    }

    @Test
    void testInvalidFrame() {
      byte[] input = {
          0x3E, // MODULE START BYTE = the ">" character (0x3E)
          0x00, // INVALID
          0x02, 0x01, // 258 mm Hg
          (byte) 0xBA // 0x100 - modulo 256 (Start byte + Packet byte + Data bytes)
      };
      assertThat(new NIBPResponse.Builder(ByteBuffer.wrap(input)).build()).isEmpty();
      assertThat(exceptionCounter.get()).isOne();
    }
  }
}