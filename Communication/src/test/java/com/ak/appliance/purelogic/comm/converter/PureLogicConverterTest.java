package com.ak.appliance.purelogic.comm.converter;

import com.ak.appliance.purelogic.comm.bytes.PureLogicFrame;
import com.ak.comm.converter.Variable;
import com.ak.comm.converter.Variables;
import com.ak.comm.logging.LogTestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.logging.Logger;

import static com.ak.comm.bytes.LogUtils.LOG_LEVEL_VALUES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PureLogicConverterTest {
  private static final Logger LOGGER = Logger.getLogger(PureLogicConverter.class.getName());

  @ParameterizedTest
  @EnumSource(PureLogicAxisFrequency.class)
  void testDataResponse(PureLogicAxisFrequency axisFrequency) {
    testConverter(axisFrequency, "STEP+ 00320  \r\n", 300);
    testConverter(axisFrequency, "STEP- 00016  \r\n", -15);
    testConverter(axisFrequency, "PLC001-G2  \r\n", 0);
  }

  @Test
  void testInvalidFrame() {
    assertThat(PureLogicFrame.of(new StringBuilder("STEP+ 00dxx  \r\n"))).isEmpty();
    assertThat(PureLogicFrame.of(new StringBuilder("STEP+ 00dxx"))).isEmpty();
  }

  private static void testConverter(PureLogicAxisFrequency axisFrequency, String input, int expected) {
    PureLogicFrame frame = PureLogicFrame.of(new StringBuilder(input)).orElseThrow();
    assertTrue(LogTestUtils.isSubstituteLogLevel(LOGGER, LOG_LEVEL_VALUES,
        () -> {
          PureLogicConverter converter = new PureLogicConverter(axisFrequency);
          assertThat(converter.apply(frame)).isNotEmpty().allSatisfy(ints -> assertThat(ints).containsExactly(expected));
          converter.refresh(false);
          assertThat(converter.apply(frame)).isNotEmpty().allSatisfy(ints -> assertThat(ints).containsExactly(expected));
          converter.refresh(true);
          assertThat(converter.apply(frame)).isNotEmpty().allSatisfy(ints -> assertThat(ints).containsExactly(expected));
        },
        logRecord -> {
          assertTrue(logRecord.getMessage().contains(Integer.toString(expected)), logRecord::getMessage);
          for (PureLogicVariable v : PureLogicVariable.values()) {
            assertTrue(logRecord.getMessage().contains(Variables.toString(v)), logRecord::getMessage);
          }
        }));
  }

  @ParameterizedTest
  @EnumSource(value = PureLogicVariable.class)
  void testVariables(Variable<PureLogicVariable> variable) {
    assertThat(variable.options()).containsExactly(Variable.Option.VISIBLE);
    assertThat(variable.getUnit()).isEqualTo(PureLogicVariable.POSITION.getUnit());
  }
}