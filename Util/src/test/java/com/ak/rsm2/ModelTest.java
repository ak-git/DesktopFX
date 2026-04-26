package com.ak.rsm2;

import com.ak.math.ValuePair;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ModelTest {
  @Nested
  class Layer2RelativeTest {
    @ParameterizedTest
    @ValueSource(doubles = {-1.1, -1.0, -0.9, 0.0, 0.9, 1.0, 1.1})
    void get(double k) {
      Model.Layer2Relative layer2Relative = new Model.Layer2Relative(k, Math.clamp(Math.random() - 0.5, 0.0, 1.0));
      assertAll(layer2Relative.toString(),
          () -> Assertions.assertThat(layer2Relative.k().value()).isBetween(-1.0, 1.0),
          () -> Assertions.assertThat(layer2Relative.h()).isNotNegative()
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
      K k = K.of(rho1, rho2);
      Model.Layer2Relative layer2Relative = new Model.Layer2Relative(k, h);
      assertAll(layer2Relative.toString(),
          () -> Assertions.assertThat(layer2Relative.k().value()).isBetween(-1.0, 1.0),
          () -> Assertions.assertThat(layer2Relative.h()).isNotNegative(),
          () -> Assertions.assertThat(layer2Relative).hasToString(
              Stream.of(ValuePair.Name.K12.of(k.value(), 0.0), ValuePair.Name.H.of(h, 0.0))
                  .map(ValuePair::toString).collect(Collectors.joining("; "))
          )
      );
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
         2.0 | 10.0 | 0.0
        10.0 |  5.0 | 1.0
         0.0 |  5.0 | 1.0
        10.0 |  0.0 | 1.0
        """)
    void byPoint(double rho1, double rho2, double h) {
      K k = K.of(rho1, rho2);
      assertEquals(new Model.Layer2Relative(new double[] {k.value(), h}), new Model.Layer2Relative(k, h));
    }

    @ParameterizedTest
    @ValueSource(doubles = {-1.0})
    void negativeH(double h) {
      assertThatIllegalArgumentException().isThrownBy(() -> new Model.Layer2Relative(Math.random(), h))
          .withMessageStartingWith("h = ").withMessageEndingWith("must be non-negative");
    }
  }

  @Nested
  class Layer2AbsoluteTest {
    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
         2.0 | 10.0 | 0.0 | 0.150
        10.0 |  5.0 | 1.0 | -0.90
         0.0 |  5.0 | 1.0 | 0.150
        10.0 |  0.0 | 1.0 | 0.000
        """)
    void get(double rho1, double rho2, double h, double dh) {
      Model.Layer2Absolute layer2Absolute = new Model.Layer2Absolute(rho1, rho2, h, dh);
      assertAll(layer2Absolute.toString(),
          () -> Assertions.assertThat(layer2Absolute).hasToString(
              Stream.of(ValuePair.Name.RHO_1.of(rho1, 0.0), ValuePair.Name.RHO_2.of(rho2, 0.0),
                      ValuePair.Name.H.of(h, 0.0), ValuePair.Name.DH.of(dh, 0.0))
                  .map(ValuePair::toString).collect(Collectors.joining("; "))
          )
      );
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
         2.0 | 10.0 | 0.0 | 0.150
        10.0 |  5.0 | 1.0 | -0.90
         0.0 |  5.0 | 1.0 | 0.150
        10.0 |  0.0 | 1.0 | 0.000
        """)
    void byPoint(double rho1, double rho2, double h, double dh) {
      assertEquals(
          new Model.Layer2Absolute(new double[] {rho1, rho2, h, dh}),
          new Model.Layer2Absolute(rho1, rho2, h, dh));
    }

    @ParameterizedTest
    @ValueSource(doubles = {-1.0})
    void negativeH(double h) {
      assertThatIllegalArgumentException().isThrownBy(() -> new Model.Layer2Absolute(Math.random(), Math.random(), h, Math.random()))
          .withMessageStartingWith("h = ").withMessageEndingWith("must be non-negative");
    }
  }
}