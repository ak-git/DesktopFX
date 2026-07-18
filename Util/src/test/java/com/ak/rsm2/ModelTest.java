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
  class Layer2AbsoluteTest {
    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
         2.0 | 10.0 | 0.0
        10.0 |  5.0 | 1.0
         0.0 |  5.0 | 1.0
        10.0 |  0.0 | 1.0
        """)
    void get(double rho1, double rho2, double h) {
      Model.Layer2Absolute layer2Absolute = new Model.Layer2Absolute(rho1, rho2, h);
      assertAll(layer2Absolute.toString(),
          () -> Assertions.assertThat(layer2Absolute.rho1()).isNotNegative(),
          () -> Assertions.assertThat(layer2Absolute.rho2()).isNotNegative(),
          () -> Assertions.assertThat(layer2Absolute.h()).isNotNegative(),
          () -> Assertions.assertThat(layer2Absolute).hasToString(
              Stream.of(ValuePair.Name.RHO_1.of(rho1, 0.0),
                      ValuePair.Name.RHO_2.of(rho2, 0.0),
                      ValuePair.Name.H.of(h, 0.0))
                  .map(ValuePair::toString).collect(Collectors.joining("; "))
          )
      );
    }

    @ParameterizedTest
    @ValueSource(doubles = {-1.0})
    void negative(double x) {
      assertThatIllegalArgumentException().isThrownBy(() -> new Model.Layer2Absolute(x, x, x))
          .withMessageEndingWith("must be non-negative");
    }
  }

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
  class Layer3RelativeTest {
    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
         2.0 | 8.0 | 4.0 | 1.0 | 2.0 | 0.180
         2.0 | 1.0 | 4.0 | 2.0 | 1.0 | -0.09
        """)
    void get(double rho1, double rho2, double rho3, double h1mm, double h2mh1mm, double dhmm) {
      K k12 = K.of(rho1, rho2);
      K k23 = K.of(rho2, rho3);
      double hStep = Metrics.Length.MILLI.toSI(0.01);
      double h1 = Metrics.Length.MILLI.toSI(h1mm);
      double h2mh1 = Metrics.Length.MILLI.toSI(h2mh1mm);
      double dh = Metrics.Length.MILLI.toSI(dhmm);
      Model.Layer3Relative layer3Relative = new Model.Layer3Relative(k12, k23, hStep,
          new Model.Layer3Relative.P(h1 / hStep, h2mh1 / hStep),
          new Model.Layer3Relative.P((h1 + dh * 2.0 / 9.0) / hStep, (h2mh1 + dh * 7.0 / 9.0) / hStep)
      );
      assertAll(layer3Relative.toString(),
          () -> Assertions.assertThat(layer3Relative.k12().value()).isBetween(-1.0, 1.0),
          () -> Assertions.assertThat(layer3Relative.k23().value()).isBetween(-1.0, 1.0),
          () -> Assertions.assertThat(layer3Relative.hStep()).isNotNegative(),
          () -> Assertions.assertThat(layer3Relative.p().p1()).isNotNegative(),
          () -> Assertions.assertThat(layer3Relative.p().p2mp1()).isNotNegative(),
          () -> Assertions.assertThat(layer3Relative.p().pSum()).isEqualTo(layer3Relative.p().p1() + layer3Relative.p().p2mp1()),
          () -> Assertions.assertThat(layer3Relative.pAfter().p1()).isNotNegative(),
          () -> Assertions.assertThat(layer3Relative.pAfter().p2mp1()).isNotNegative(),
          () -> Assertions.assertThat(layer3Relative.pAfter().pSum()).isEqualTo(layer3Relative.pAfter().p1() + layer3Relative.pAfter().p2mp1()),
          () -> Assertions.assertThat(layer3Relative).hasToString(
              Stream.of(
                      ValuePair.Name.K12.of(k12.value(), 0.0),
                      ValuePair.Name.K23.of(k23.value(), 0.0),
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
      assertThatIllegalArgumentException().isThrownBy(() -> new Model.Layer3Relative(K.of(Math.random()), K.of(Math.random()), h,
              new Model.Layer3Relative.P(0, 0), new Model.Layer3Relative.P(0, 0)))
          .withMessageStartingWith("hStep = ").withMessageEndingWith("must be non-negative");
    }
  }
}