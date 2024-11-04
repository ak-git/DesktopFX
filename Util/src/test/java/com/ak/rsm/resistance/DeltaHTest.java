package com.ak.rsm.resistance;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.EnumSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.jupiter.api.Assertions.assertAll;

class DeltaHTest {

  @Test
  void type() {
    assertThat(EnumSet.allOf(DeltaH.Type.class))
        .containsExactly(DeltaH.NULL.type(), DeltaH.H1.apply(0.0).type());
  }

  @ParameterizedTest
  @ValueSource(doubles = {Double.MIN_VALUE, Double.MAX_VALUE, -1.0, 0.0, 1.0})
  void value(double d) {
    assertThat(DeltaH.H1.apply(d).value()).isEqualTo(d);
  }

  @ParameterizedTest
  @ValueSource(doubles = {Double.MIN_VALUE, Double.MAX_VALUE, -1.0, 0.0, 1.0})
  void convert(double d) {
    assertThat(DeltaH.H1.apply(d).convert(x -> x / 2.0).value()).isEqualTo(d / 2.0);
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
  @ValueSource(doubles = {Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY})
  void testValueNotFinite(double notFinite) {
    assertThatIllegalArgumentException().isThrownBy(() -> DeltaH.H1.apply(notFinite))
        .withMessageStartingWith("Value is not finite = ")
        .withMessageEndingWith(Double.toString(notFinite));
  }
}