package com.ak.comm.converter.purelogic;

import java.util.Collections;
import java.util.EnumSet;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.comm.bytes.purelogic.PureLogicFrame;
import com.ak.comm.converter.Variable;
import com.ak.comm.converter.Variables;
import com.ak.comm.log.LogTestUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import static com.ak.util.LogUtils.LOG_LEVEL_VALUES;

public class PureLogicConverterTest {
  private static final Logger LOGGER = Logger.getLogger(PureLogicConverter.class.getName());

  @Test
  public void testDataResponse() {
    testConverter("STEP+ 00320  \r\n", 300);
    testConverter("STEP- 00016  \r\n", -15);
  }

  @Test
  public void testInvalidFrame() {
    Assert.assertNull(PureLogicFrame.of(new StringBuilder("STEP+ 00dxx  \r\n")));
  }

  @ParametersAreNonnullByDefault
  private static void testConverter(String input, int expected) {
    PureLogicFrame frame = PureLogicFrame.of(new StringBuilder(input));
    Assert.assertNotNull(frame);
    Assert.assertTrue(LogTestUtils.isSubstituteLogLevel(LOGGER, LOG_LEVEL_VALUES,
        () -> {
          PureLogicConverter converter = new PureLogicConverter();
          Stream<int[]> stream = converter.apply(frame);
          stream.forEach(ints -> Assert.assertEquals(ints, new int[] {expected}));
        },
        logRecord -> {
          Assert.assertTrue(logRecord.getMessage().contains(Integer.toString(expected)));
          for (PureLogicVariable v : PureLogicVariable.values()) {
            Assert.assertTrue(logRecord.getMessage().contains(Variables.toString(v)), logRecord.getMessage());
          }
        }));
  }

  @Test
  public void testVariableProperties() {
    EnumSet.allOf(PureLogicVariable.class)
        .forEach(variable -> Assert.assertEquals(variable.options(), Collections.singleton(Variable.Option.VISIBLE)));
    EnumSet.allOf(PureLogicVariable.class)
        .forEach(variable -> Assert.assertEquals(variable.getUnit(), PureLogicVariable.POSITION.getUnit()));
  }
}