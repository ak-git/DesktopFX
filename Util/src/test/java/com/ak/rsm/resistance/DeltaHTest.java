package com.ak.rsm.resistance;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.function.DoubleFunction;
import java.util.function.DoubleUnaryOperator;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertAll;

class DeltaHTest {
  private final Collection<DoubleFunction<DeltaH>> nonNullFunctions;

  DeltaHTest() {
    nonNullFunctions = List.of(DeltaH.H1, DeltaH.H2, DeltaH.H1_H2);
    assertThat(nonNullFunctions).hasSize(EnumSet.complementOf(EnumSet.of(DeltaH.Type.NONE)).size());
  }

  @Test
  void type() {
    EnumSet<DeltaH.Type> none = EnumSet.of(DeltaH.Type.NONE);
    assertAll(
        () -> assertThat(none).containsExactly(DeltaH.NULL.type()),
        () -> assertThat(
            EnumSet.complementOf(none))
            .containsExactlyElementsOf(nonNullFunctions.stream().map(f -> f.apply(0.0).type()).toList())
    );
  }

  @ParameterizedTest
  @ValueSource(doubles = {Double.MIN_VALUE, Double.MAX_VALUE, -1.0, 0.0, 1.0})
  void value(double d) {
    nonNullFunctions.forEach(f -> assertThat(f.apply(d).value()).isEqualTo(d));
  }

  @ParameterizedTest
  @ValueSource(doubles = {Double.MIN_VALUE, Double.MAX_VALUE, -1.0, 0.0, 1.0})
  void convert(double d) {
    nonNullFunctions.forEach(f -> assertThat(f.apply(d).convert(x -> x / 2.0).value()).isEqualTo(d / 2.0));
  }

  @Test
  void testNull() {
    assertAll(DeltaH.NULL.toString(),
        () -> assertThat(DeltaH.NULL.value()).isNaN(),
        () -> assertThat(DeltaH.NULL.convert(x -> {
              assertThat(x).isNaN();
              return x;
            }
        ).value()).isNaN()
    );
  }

  @ParameterizedTest
  @NullSource
  void testNullConverter(DoubleUnaryOperator converter) {
    nonNullFunctions.forEach(f -> assertThatNullPointerException().isThrownBy(() -> f.apply(0.0).convert(converter)));
  }

  @ParameterizedTest
  @ValueSource(doubles = {Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY})
  void testValueNotFinite(double notFinite) {
    nonNullFunctions.forEach(f ->
        assertThatIllegalArgumentException().isThrownBy(() -> f.apply(notFinite))
            .withMessageStartingWith("Value is not finite = ")
            .withMessageEndingWith(Double.toString(notFinite))
    );
  }
}