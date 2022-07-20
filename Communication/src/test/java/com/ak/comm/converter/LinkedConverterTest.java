package com.ak.comm.converter;

import java.nio.ByteOrder;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.comm.bytes.BufferFrame;
import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.IntsAcceptor;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import tec.uom.se.AbstractUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class LinkedConverterTest {
  static Stream<Arguments> variables() {
    return Stream.of(
        arguments(
            new BufferFrame(new byte[] {1, 2, 0, 0, 0, 3, 0, 0, 0}, ByteOrder.LITTLE_ENDIAN),
            new int[] {2 + 3, 2 - 3, 0}
        )
    );
  }

  public static Stream<Arguments> variables2() {
    return Stream.of(
        arguments(
            new BufferFrame(new byte[] {1, 2, 0, 0, 0, 3, 0, 0, 0}, ByteOrder.LITTLE_ENDIAN),
            new int[] {(2 + 3) * (2 - 3)}
        )
    );
  }

  @ParameterizedTest
  @MethodSource("variables")
  @ParametersAreNonnullByDefault
  void testApply(BufferFrame frame, int[] output) {
    Converter<BufferFrame, TwoVariables> converter = new ToIntegerConverter<>(TwoVariables.class, 200);
    LinkedConverter<BufferFrame, TwoVariables, OperatorVariables> linkedConverter = LinkedConverter.of(converter, OperatorVariables.class);
    assertThat(linkedConverter.variables()).containsExactly(OperatorVariables.values());
    assertThat(linkedConverter.apply(frame)).containsExactly(output).hasSize(1);
  }

  @ParameterizedTest
  @MethodSource("variables2")
  @ParametersAreNonnullByDefault
  void testApply2(BufferFrame frame, int[] output) {
    Function<BufferFrame, Stream<int[]>> linkedConverter =
        LinkedConverter.of(new ToIntegerConverter<>(TwoVariables.class, 1000), OperatorVariables.class)
            .chainInstance(OperatorVariables2.class);
    assertThat(linkedConverter.apply(frame)).containsExactly(output).hasSize(1);
  }

  @ParameterizedTest
  @EnumSource(value = RefreshVariable.class)
  void testRecursive(@Nonnull Variable<RefreshVariable> variable) {
    assertThat(variable.getUnit()).isEqualTo(AbstractUnit.ONE);
    assertThat(variable.options()).containsSequence(Variable.Option.defaultOptions());
  }

  static Stream<BufferFrame> refreshVariables() {
    return Stream.of(new BufferFrame(new byte[] {1, 0, 0, 0, 10}, ByteOrder.BIG_ENDIAN));
  }

  @ParameterizedTest
  @MethodSource("refreshVariables")
  void testRefresh(@Nonnull BufferFrame frame) {
    LinkedConverter<BufferFrame, RefreshVariable, RefreshVariable> linkedConverter =
        LinkedConverter.of(new ToIntegerConverter<>(RefreshVariable.class, 1), RefreshVariable.class)
            .chainInstance(RefreshVariable.class);

    linkedConverter.refresh(false);
    assertThat(linkedConverter.apply(frame)).isEmpty();
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
          assertThat(values).containsExactly(10);
          assertThat(refreshCount).isEqualTo(1);
        }
      };
    }
  }
}