package com.ak.rsm2;

import com.ak.math.ValuePair;
import com.ak.util.Metrics;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
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
          () -> assertThat(layer2Relative.k().value()).isBetween(-1.0, 1.0),
          () -> assertThat(layer2Relative.h()).isNotNegative(),
          () -> assertThat(layer2Relative.toModel()).isEqualTo(new Model.Layer2Relative(k, h)),
          () -> assertThat(layer2Relative).hasToString(
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
          () -> assertThat(layer2Relative.k().value()).isBetween(-1.0, 1.0),
          () -> assertThat(layer2Relative.h()).isNotNegative(),
          () -> assertThat(layer2Relative.dh()).isEqualTo(dh),
          () -> assertThat(layer2Relative.toModel()).isEqualTo(new Model.Layer2Relative(k, h)),
          () -> assertThat(layer2Relative).hasToString(
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
         2.0 | 8.0 | 4.0 | 1.0 | 2.0 | 0.02 | 0.07 | 0.01
         2.0 | 1.0 | 4.0 | 1.0 | 2.0 | 0.02 | 0.07 | 0.02
        """)
    void get(double rho1, double rho2, double rho3, double h1mm, double h2mh1mm, double dh1mm, double dh2mh1mm, double dRho2) {
      double hStep = Metrics.Length.MILLI.toSI(0.01);
      double h1 = Metrics.Length.MILLI.toSI(h1mm);
      double h2mh1 = Metrics.Length.MILLI.toSI(h2mh1mm);
      double dh1 = Metrics.Length.MILLI.toSI(dh1mm);
      double dh2mh1 = Metrics.Length.MILLI.toSI(dh2mh1mm);

      IterativeModel.Layer3Absolute layer3Absolute = IterativeModel.Layer3Absolute.builder(hStep)
          .variables(new double[] {rho1, rho2, rho3, h1, h2mh1, dh1, dh2mh1, dRho2}).build();
      assertAll(layer3Absolute.toString(),
          () -> assertThat(layer3Absolute.rho1()).isNotNegative(),
          () -> assertThat(layer3Absolute.rho2()).isNotNegative(),
          () -> assertThat(layer3Absolute.rho3()).isNotNegative(),
          () -> assertThat(layer3Absolute.p().p1()).isNotNegative(),
          () -> assertThat(layer3Absolute.p().p2mp1()).isNotNegative(),
          () -> assertThat(layer3Absolute.p().pSum()).isEqualTo(layer3Absolute.p().p1() + layer3Absolute.p().p2mp1()),
          () -> assertThat(layer3Absolute.dp().p1()).isEqualTo(2),
          () -> assertThat(layer3Absolute.dp().p2mp1()).isEqualTo(7),
          () -> assertThat(layer3Absolute.dp().pSum()).isEqualTo(9),
          () -> assertThat(layer3Absolute.dRho2()).isNotNegative(),
          () -> assertThat(layer3Absolute.toModel(new Model.P(2, 3), dRho2))
              .isEqualTo(new Model.Layer3Absolute(rho1, rho2 + dRho2, rho3, hStep, layer3Absolute.p().add(new Model.P(2, 3)))),
          () -> assertThat(layer3Absolute).hasToString(
              Stream.of(
                      ValuePair.Name.RHO_1.of(rho1, 0.0),
                      ValuePair.Name.RHO_2.of(rho2, 0.0),
                      ValuePair.Name.RHO_3.of(rho3, 0.0),
                      ValuePair.Name.H1.of(h1, 0.0),
                      ValuePair.Name.H2.of(h2mh1 + h1, 0.0),
                      ValuePair.Name.DH1.of(dh1, 0.0),
                      ValuePair.Name.DH2.of(dh2mh1 + dh1, 0.0),
                      ValuePair.Name.D_RHO_2.of(dRho2, 0.0)
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