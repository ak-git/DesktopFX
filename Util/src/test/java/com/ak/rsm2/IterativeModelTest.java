package com.ak.rsm2;

import com.ak.math.ValuePair;
import com.ak.util.Metrics;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.jupiter.api.Assertions.assertAll;

class IterativeModelTest {
  @Nested
  class Layer2RelativeTest {
    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
         2.0 | 10.0 | 0.0
        10.0 |  5.0 | 1.0
         0.0 |  5.0 | 1.0
        10.0 |  0.0 | 1.0
        """)
    void get(double rho1, double rho2, double hmm) {
      K k = K.of(rho1, rho2);
      double h = Metrics.Length.MILLI.toSI(hmm);
      IterativeModel.Layer2Relative layer2Relative = new IterativeModel.Layer2Relative(new double[] {k.value(), h});
      assertAll(layer2Relative.toString(),
          () -> Assertions.assertThat(layer2Relative.k().value()).isBetween(-1.0, 1.0),
          () -> Assertions.assertThat(layer2Relative.h()).isNotNegative(),
          () -> Assertions.assertThat(layer2Relative.toModel()).isEqualTo(new Model.Layer2Relative(k, h)),
          () -> Assertions.assertThat(layer2Relative).hasToString(
              Stream.of(ValuePair.Name.K12.of(k.value(), 0.0), ValuePair.Name.H.of(h, 0.0))
                  .map(ValuePair::toString).collect(Collectors.joining("; "))
          )
      );
    }

    @ParameterizedTest
    @ValueSource(doubles = {-1.0})
    void negativeH(double h) {
      assertThatIllegalArgumentException().isThrownBy(() -> new IterativeModel.Layer2Relative(K.of(Math.random()), h))
          .withMessageStartingWith("h = ").withMessageEndingWith("must be non-negative");
    }
  }

  @Nested
  class Layer2RelativeDhTest {
    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
         2.0 | 10.0 | 0.0 | 0.180
        10.0 |  5.0 | 1.0 | -0.09
         0.0 |  5.0 | 1.0 | 0.180
        10.0 |  0.0 | 1.0 | 0.000
        """)
    void get(double rho1, double rho2, double hmm, double dhmm) {
      K k = K.of(rho1, rho2);
      double h = Metrics.Length.MILLI.toSI(hmm);
      double dh = Metrics.Length.MILLI.toSI(dhmm);
      IterativeModel.Layer2RelativeDh layer2Relative = new IterativeModel.Layer2RelativeDh(new double[] {k.value(), h, dh}
      );
      assertAll(layer2Relative.toString(),
          () -> Assertions.assertThat(layer2Relative.k().value()).isBetween(-1.0, 1.0),
          () -> Assertions.assertThat(layer2Relative.h()).isNotNegative(),
          () -> Assertions.assertThat(layer2Relative.dh()).isEqualTo(dh),
          () -> Assertions.assertThat(layer2Relative.toModel()).isEqualTo(new Model.Layer2Relative(k, h)),
          () -> Assertions.assertThat(layer2Relative).hasToString(
              Stream.of(ValuePair.Name.K12.of(k.value(), 0.0), ValuePair.Name.H.of(h, 0.0),
                      ValuePair.Name.DH.of(dh, 0.0))
                  .map(ValuePair::toString).collect(Collectors.joining("; "))
          )
      );
    }

    @ParameterizedTest
    @ValueSource(doubles = {-1.0})
    void negativeH(double h) {
      assertThatIllegalArgumentException().isThrownBy(() -> new IterativeModel.Layer2RelativeDh(K.of(Math.random()), h, Math.random()))
          .withMessageStartingWith("h = ").withMessageEndingWith("must be non-negative");
    }
  }
}