package com.ak.comm.converter;

import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;
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
            new int[] {2 + 3, 2 - 3, 0}},
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
    ToIntegerConverter<TwoVariables> converter = new ToIntegerConverter<>(TwoVariables.class, 200);
    LinkedConverter<BufferFrame, TwoVariables, OperatorVariables> linkedConverter = new LinkedConverter<>(converter, OperatorVariables.class);
    Assert.assertEquals(linkedConverter.variables(), Stream.of(OperatorVariables.values()).collect(Collectors.toList()));
    Assert.assertEquals(linkedConverter.apply(frame).peek(ints -> Assert.assertEquals(ints, output,
        String.format("Actual %s, Expected %s", Arrays.toString(ints), Arrays.toString(output)))).count(), 1);
  }

  @Test(dataProvider = "variables2")
  public static void testApply2(BufferFrame frame, int[] output) {
    Function<BufferFrame, Stream<int[]>> linkedConverter =
        new LinkedConverter<>(
            new LinkedConverter<>(new ToIntegerConverter<>(TwoVariables.class, 1000), OperatorVariables.class),
            OperatorVariables2.class
        );

    Assert.assertEquals(linkedConverter.apply(frame).peek(ints -> Assert.assertEquals(ints, output,
        String.format("Actual %s, Expected %s", Arrays.toString(ints), Arrays.toString(output)))).count(), 1);
  }
}