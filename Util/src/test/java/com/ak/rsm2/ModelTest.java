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

class ModelTest {
  @Nested
  class Layer2RelativeTest {
    @ParameterizedTest
    @ValueSource(doubles = {-1.1, -1.0, -0.9, 0.0, 0.9, 1.0, 1.1})
    void get(double k) {
      Model.Layer2Relative layer2Relative = new Model.Layer2Relative(K.of(k), Math.clamp(Math.random() - 0.5, 0.0, 1.0));
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
    @ValueSource(doubles = {-1.0})
    void negativeH(double h) {
      assertThatIllegalArgumentException().isThrownBy(() -> new Model.Layer2Relative(K.of(Math.random()), h))
          .withMessageStartingWith("h = ").withMessageEndingWith("must be non-negative");
    }
  }

  @Nested
  class Layer3AbsoluteTest {
    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
         2.0 | 8.0 | 4.0 | 1.0 | 2.0 | 0.180
         2.0 | 1.0 | 4.0 | 2.0 | 1.0 | -0.09
        """)
    void get(double rho1, double rho2, double rho3, double h1mm, double h2mh1mm, double dhmm) {
      double hStep = Metrics.Length.MILLI.toSI(0.01);
      double h1 = Metrics.Length.MILLI.toSI(h1mm);
      double h2mh1 = Metrics.Length.MILLI.toSI(h2mh1mm);
      double dh = Metrics.Length.MILLI.toSI(dhmm);
      Model.Layer3Absolute layer3Absolute = new Model.Layer3Absolute(rho1, rho2, rho3, hStep,
          new Model.P(h1 / hStep, h2mh1 / hStep),
          new Model.P((h1 + dh * 2.0 / 9.0) / hStep, (h2mh1 + dh * 7.0 / 9.0) / hStep)
      );
      assertAll(layer3Absolute.toString(),
          () -> Assertions.assertThat(layer3Absolute.rho1()).isNotNegative(),
          () -> Assertions.assertThat(layer3Absolute.rho2()).isNotNegative(),
          () -> Assertions.assertThat(layer3Absolute.rho3()).isNotNegative(),
          () -> Assertions.assertThat(layer3Absolute.hStep()).isNotNegative(),
          () -> Assertions.assertThat(layer3Absolute.p().p1()).isNotNegative(),
          () -> Assertions.assertThat(layer3Absolute.p().p2mp1()).isNotNegative(),
          () -> Assertions.assertThat(layer3Absolute.p().pSum()).isEqualTo(layer3Absolute.p().p1() + layer3Absolute.p().p2mp1()),
          () -> Assertions.assertThat(layer3Absolute.pAfter().p1()).isNotNegative(),
          () -> Assertions.assertThat(layer3Absolute.pAfter().p2mp1()).isNotNegative(),
          () -> Assertions.assertThat(layer3Absolute.pAfter().pSum()).isEqualTo(layer3Absolute.pAfter().p1() + layer3Absolute.pAfter().p2mp1()),
          () -> Assertions.assertThat(layer3Absolute).hasToString(
              Stream.of(
                      ValuePair.Name.RHO_1.of(rho1, 0.0),
                      ValuePair.Name.RHO_2.of(rho2, 0.0),
                      ValuePair.Name.RHO_3.of(rho3, 0.0),
                      ValuePair.Name.H1.of(h1, 0.0),
                      ValuePair.Name.H2.of(h1 + h2mh1, 0.0),
                      ValuePair.Name.DH1.of(dh * 2.0 / 9.0, 0.0),
                      ValuePair.Name.DH2.of(dh, 0.0)
                  )
                  .map(ValuePair::toString).collect(Collectors.joining("; "))
          )
      );
    }

    @ParameterizedTest
    @ValueSource(doubles = {-1.0})
    void negativeH(double h) {
      assertThatIllegalArgumentException().isThrownBy(() -> new Model.Layer3Absolute(Math.random(), Math.random(), Math.random(),
              h, new Model.P(0, 0), new Model.P(0, 0)))
          .withMessageStartingWith("hStep = ").withMessageEndingWith("must be non-negative");
    }
  }
}