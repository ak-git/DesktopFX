package com.ak.comm.bytes.purelogic;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import javax.annotation.Nonnull;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class PureLogicFrameTest {
  @DataProvider(name = "requests")
  public static Object[][] requests() {
    return new Object[][] {
        {PureLogicFrame.StepCommand.MICRON_015.action(true), "STEP -00016\n"},
        {PureLogicFrame.StepCommand.MICRON_015.action(false), "STEP +00016\n"},
        {PureLogicFrame.StepCommand.MICRON_150.action(true), "STEP -00160\n"},
        {PureLogicFrame.StepCommand.MICRON_150.action(false), "STEP +00160\n"},
    };
  }

  @Test(dataProvider = "requests")
  public void testRequest(@Nonnull PureLogicFrame request, @Nonnull String expected) {
    ByteBuffer buffer = ByteBuffer.allocate(expected.length());
    request.writeTo(buffer);
    Assert.assertEquals(new String(buffer.array(), StandardCharsets.UTF_8), expected, request.toString());
    Assert.assertTrue(request.toString().contains(expected.strip()), request.toString());
  }
}