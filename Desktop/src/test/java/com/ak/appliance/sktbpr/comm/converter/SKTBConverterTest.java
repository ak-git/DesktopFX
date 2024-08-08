package com.ak.appliance.sktbpr.comm.converter;

import com.ak.appliance.sktbpr.comm.bytes.SKTBResponse;
import com.ak.comm.converter.Variables;
import com.ak.comm.log.LogTestUtils;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.logging.Logger;

import static com.ak.comm.bytes.LogUtils.LOG_LEVEL_VALUES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SKTBConverterTest {
  private static final Logger LOGGER = Logger.getLogger(SKTBConverter.class.getName());

  @Test
  void testDataResponse() {
    testConverter(new byte[] {-91, 9, 6, -123, 2, 0, 0, -84, 10}, new int[] {6, 27});
    testConverter(new byte[] {-91, 10, 6, 87, 3, 0, 0, -83, 10}, new int[] {8, 27});
  }

  private static void testConverter(byte[] input, int[] expected) {
    SKTBResponse.Builder builder = new SKTBResponse.Builder();
    builder.buffer().put(input);
    SKTBResponse response = builder.build().orElseThrow();
    assertTrue(LogTestUtils.isSubstituteLogLevel(LOGGER, LOG_LEVEL_VALUES,
        () -> assertThat(new SKTBConverter().apply(response)).isNotEmpty().allSatisfy(ints -> assertThat(ints).containsExactly(expected)),
        logRecord -> {
          Arrays.stream(expected).forEach(value ->
              assertThat(logRecord.getMessage()).withFailMessage(logRecord.getMessage()).contains(Integer.toString(value))
          );
          for (SKTBVariable v : SKTBVariable.values()) {
            assertTrue(logRecord.getMessage().contains(Variables.toString(v)), logRecord::getMessage);
          }
        })
    );
  }
}