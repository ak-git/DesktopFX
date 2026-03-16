package com.ak.rsm2;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.jupiter.api.Assertions.assertAll;

class ModelTest {
  @Nested
  class Layer2RelativeTest {
    @ParameterizedTest
    @ValueSource(doubles = {-1.1, -1.0, -0.9, 0.0, 0.9, 1.0, 1.1})
    void get(double k) {
      Model.Layer2Relative layer2Relative = new Model.Layer2Relative(k, Math.clamp(Math.random() - 0.5, 0.0, 1.0));
      assertAll(layer2Relative.toString(),
          () -> Assertions.assertThat(layer2Relative.k()).isBetween(-1.0, 1.0),
          () -> Assertions.assertThat(layer2Relative.hSI()).isNotNegative()
      );
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
         2.0 | 10.0 | 0.0
        10.0 |  5.0 | 1.0
         0.0 |  5.0 | 1.0
        10.0 |  0.0 | 1.0
        """)
    void get(double rho1, double rho2, double h) {
      Model.Layer2Relative layer2Relative = new Model.Layer2Relative(K.of(rho1, rho2), h);
      assertAll(layer2Relative.toString(),
          () -> Assertions.assertThat(layer2Relative.k()).isBetween(-1.0, 1.0),
          () -> Assertions.assertThat(layer2Relative.hSI()).isNotNegative()
      );
    }

    @ParameterizedTest
    @ValueSource(doubles = {-1.0})
    void negativeH(double h) {
      assertThatIllegalArgumentException().isThrownBy(() -> new Model.Layer2Relative(Math.random(), h))
          .withMessageStartingWith("hSI = ").withMessageEndingWith("must be non-negative");
    }
  }
}