package com.ak.comm.converter;

import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import com.ak.comm.bytes.BufferFrame;
import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.IntsAcceptor;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import tec.uom.se.AbstractUnit;

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

  @DataProvider(name = "refresh-variables")
  public static Object[][] variables3() {
    return new Object[][] {
        {new BufferFrame(new byte[] {1, 0, 0, 0, 10}, ByteOrder.BIG_ENDIAN)},
    };
  }

  @Test
  public static void testRecursive() {
    Assert.assertEquals(RefreshVariable.OUT.getUnit(), AbstractUnit.ONE);
    Assert.assertEquals(RefreshVariable.OUT.options(), Variable.Option.defaultOptions());
  }

  @Test(dataProvider = "refresh-variables")
  public static void testRefresh(BufferFrame frame) {
    LinkedConverter<BufferFrame, RefreshVariable, RefreshVariable> linkedConverter =
        new LinkedConverter<>(
            new LinkedConverter<>(new ToIntegerConverter<>(RefreshVariable.class, 1), RefreshVariable.class),
            RefreshVariable.class
        );

    linkedConverter.refresh();
    Assert.assertEquals(linkedConverter.apply(frame).count(), 0);
  }

  public enum RefreshVariable implements DependentVariable<RefreshVariable, RefreshVariable> {
    OUT;

    @Override
    public final Class<RefreshVariable> getInputVariablesClass() {
      return RefreshVariable.class;
    }

    @Override
    public final List<RefreshVariable> getInputVariables() {
      return Collections.singletonList(OUT);
    }


    @Override
    public DigitalFilter filter() {
      return new DigitalFilter() {
        private int refreshCount;

        @Override
        public void forEach(@Nonnull IntsAcceptor after) {
        }

        @Override
        public void reset() {
          refreshCount++;
        }

        @Override
        public int getOutputDataSize() {
          return 1;
        }

        @Override
        public void accept(@Nonnull int... values) {
          Assert.assertEquals(values, new int[] {10});
          Assert.assertEquals(refreshCount, 1);
        }
      };
    }
  }
}