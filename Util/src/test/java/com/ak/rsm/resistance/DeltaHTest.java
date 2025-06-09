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
    nonNullFunctions = List.of(DeltaH.H1, DeltaH.H2);
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
  @ValueSource(doubles = {Double.MIN_VALUE, Double.MAX_VALUE, 0.0, 1.0})
  void value(double d) {
    nonNullFunctions.forEach(f -> assertThat(f.apply(d).value()).isEqualTo(d));
  }

  @ParameterizedTest
  @ValueSource(doubles = {Double.MIN_VALUE, Double.MAX_VALUE, 0.0, 1.0})
  void convert(double d) {
    nonNullFunctions.forEach(f -> assertThat(f.apply(d).convert(x -> x / 2.0).value()).isEqualTo(d / 2.0));
  }

  @Test
  void testNull() {
    assertAll(DeltaH.NULL.toString(),
        () -> assertThat(DeltaH.NULL.value()).isNaN(),
        () -> assertThat(DeltaH.NULL.next()).isEqualTo(DeltaH.NULL),
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

  @Test
  void testH1andH2Type() {
    DeltaH h1andH2 = DeltaH.ofH1andH2(1.0, 2.0);
    assertAll(h1andH2.toString(),
        () -> assertThat(h1andH2.type()).isEqualTo(DeltaH.Type.H1),
        () -> assertThat(h1andH2.value()).isEqualTo(1.0),
        () -> assertThat(h1andH2.convert(x -> x / 10.0)).isEqualTo(DeltaH.H1.apply(0.1))
    );

    DeltaH next = h1andH2.next();
    assertAll(next.toString(),
        () -> assertThat(next.type()).isEqualTo(DeltaH.Type.H2),
        () -> assertThat(next.value()).isEqualTo(2.0),
        () -> assertThat(next.convert(x -> x * 10.0)).isEqualTo(DeltaH.H2.apply(20.0))
    );
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

  @ParameterizedTest
  @ValueSource(doubles = -1.0)
  void testValueNegative(double negative) {
    nonNullFunctions.forEach(f ->
        assertThatIllegalArgumentException().isThrownBy(() -> f.apply(negative))
            .withMessageStartingWith("Value < 0 = ")
            .withMessageEndingWith("%f".formatted(negative))
    );
  }
}