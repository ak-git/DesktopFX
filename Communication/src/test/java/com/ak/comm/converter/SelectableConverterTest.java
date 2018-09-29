package com.ak.comm.converter;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Stream;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class SelectableConverterTest {
  private SelectableConverterTest() {
  }

  @DataProvider(name = "variables")
  public static Object[][] variables() {
    return new Object[][] {
        {new int[] {1, 2}, new int[] {1 + 2, 1 - 2}},
    };
  }

  @Test(dataProvider = "variables")
  public static void testApply(int[] input, int[] output) {
    Function<Stream<int[]>, Stream<int[]>> converter = new SelectableConverter<>(OperatorVariables.class, 1000);
    Assert.assertEquals(converter.apply(Stream.of(input)).peek(ints -> Assert.assertEquals(ints, output,
        String.format("Actual %s, Expected %s", Arrays.toString(ints), Arrays.toString(output)))).count(), 1);
  }
}