package com.ak.comm.converter;

import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Stream;

import com.ak.comm.bytes.BufferFrame;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class LinkedConverterTest {
  private LinkedConverterTest() {
  }

  @DataProvider(name = "variables")
  public static Object[][] variables() {
    return new Object[][] {
        {new BufferFrame(new byte[] {1, 2, 0, 0, 0, 3, 0, 0, 0}, ByteOrder.LITTLE_ENDIAN),
            new int[] {2 + 3, 2 - 3}},
    };
  }

  @DataProvider(name = "variables2")
  public static Object[][] variables2() {
    return new Object[][] {
        {new BufferFrame(new byte[] {1, 2, 0, 0, 0, 3, 0, 0, 0}, ByteOrder.LITTLE_ENDIAN),
            new int[] {(2 + 3) * (2 - 3)}},
    };
  }

  @Test(dataProvider = "variables")
  public static void testApply(BufferFrame frame, int[] output) {
    ToIntegerConverter<TwoVariables> converter = new ToIntegerConverter<>(TwoVariables.class);
    SelectableConverter<TwoVariables, OperatorVariables> selectableConverter = new SelectableConverter<>(OperatorVariables.class);
    LinkedConverter<BufferFrame, TwoVariables, OperatorVariables> linkedConverter = new LinkedConverter<>(converter, selectableConverter);
    Assert.assertEquals(linkedConverter.variables(), selectableConverter.variables());
    Assert.assertEquals(linkedConverter.apply(frame).peek(ints -> Assert.assertEquals(ints, output,
        String.format("Actual %s, Expected %s", Arrays.toString(ints), Arrays.toString(output)))).count(), 1);
  }

  @Test(dataProvider = "variables2")
  public static void testApply2(BufferFrame frame, int[] output) {
    Function<BufferFrame, Stream<int[]>> linkedConverter =
        new LinkedConverter<>(
            new LinkedConverter<>(new ToIntegerConverter<>(TwoVariables.class),
                new SelectableConverter<>(OperatorVariables.class)),
            new SelectableConverter<>(OperatorVariables2.class)
        );

    Assert.assertEquals(linkedConverter.apply(frame).peek(ints -> Assert.assertEquals(ints, output,
        String.format("Actual %s, Expected %s", Arrays.toString(ints), Arrays.toString(output)))).count(), 1);
  }
}