package com.ak.comm.converter.inner;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.ak.comm.converter.Variables;
import com.ak.comm.util.LogUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

public class UnLocalizedVariablesTest {
  private static final Logger LOGGER = Logger.getLogger(Variables.class.getName());

  private UnLocalizedVariablesTest() {
  }

  @Test
  public static void testToString() {
    Assert.assertTrue(LogUtils.isSubstituteLogLevel(LOGGER, Level.CONFIG,
        () -> Assert.assertEquals(Variables.toString(UnLocalizedVariables.MISSING_RESOURCE),
            UnLocalizedVariables.MISSING_RESOURCE.name()), logRecord -> {
          Assert.assertTrue(logRecord.getMessage().contains(UnLocalizedVariables.MISSING_RESOURCE.name()));
          Assert.assertNull(logRecord.getThrown());
        }));
  }
}