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

  @Nested
  class Layer3AbsoluteTest {
    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
         2.0 | 8.0 | 4.0 | 1.0 | 2.0 | 0.02 | 0.07
         2.0 | 1.0 | 4.0 | 2.0 | 1.0 | 0.02 | 0.07
        """)
    void get(double rho1, double rho2, double rho3, double h1mm, double h2mh1mm, double dh1mm, double dh2mh1mm) {
      double hStep = Metrics.Length.MILLI.toSI(0.01);
      double h1 = Metrics.Length.MILLI.toSI(h1mm);
      double h2mh1 = Metrics.Length.MILLI.toSI(h2mh1mm);
      double dh1 = Metrics.Length.MILLI.toSI(dh1mm);
      double dh2mh1 = Metrics.Length.MILLI.toSI(dh2mh1mm);

      IterativeModel.Layer3Absolute layer3Absolute = IterativeModel.Layer3Absolute.builder(hStep, new Model.P(2, 7))
          .variables(new double[] {rho1, rho2, rho3, h1, h2mh1, dh1, dh2mh1}).build();
      assertAll(layer3Absolute.toString(),
          () -> Assertions.assertThat(layer3Absolute.rho1()).isNotNegative(),
          () -> Assertions.assertThat(layer3Absolute.rho2()).isNotNegative(),
          () -> Assertions.assertThat(layer3Absolute.rho3()).isNotNegative(),
          () -> Assertions.assertThat(layer3Absolute.p().p1()).isNotNegative(),
          () -> Assertions.assertThat(layer3Absolute.p().p2mp1()).isNotNegative(),
          () -> Assertions.assertThat(layer3Absolute.p().pSum()).isEqualTo(layer3Absolute.p().p1() + layer3Absolute.p().p2mp1()),
          () -> Assertions.assertThat(layer3Absolute.dp().p1()).isEqualTo(2),
          () -> Assertions.assertThat(layer3Absolute.dp().p2mp1()).isEqualTo(7),
          () -> Assertions.assertThat(layer3Absolute.dp().pSum()).isEqualTo(9),
          () -> Assertions.assertThat(layer3Absolute.toModel(new Model.P(1, 2), new Model.P(2, 3)))
              .isEqualTo(new Model.Layer3Absolute(rho1, rho2, rho3, hStep, new Model.P(1, 2), new Model.P(3, 5))),
          () -> Assertions.assertThat(layer3Absolute).hasToString(
              Stream.of(
                      ValuePair.Name.RHO_1.of(rho1, 0.0),
                      ValuePair.Name.RHO_2.of(rho2, 0.0),
                      ValuePair.Name.RHO_3.of(rho3, 0.0),
                      ValuePair.Name.H1.of(Math.min(h1, h2mh1), 0.0),
                      ValuePair.Name.H2.of(h1 + h2mh1, 0.0),
                      ValuePair.Name.DH1.of(Metrics.Length.MILLI.toSI(0.02), 0.0),
                      ValuePair.Name.DH2.of(Metrics.Length.MILLI.toSI(0.09), 0.0)
                  )
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