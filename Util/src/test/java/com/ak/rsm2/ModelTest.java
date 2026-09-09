package com.ak.rsm2;

import com.ak.math.ValuePair;
import com.ak.util.Metrics;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.security.SecureRandom;
import java.util.random.RandomGenerator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.jupiter.api.Assertions.assertAll;

class ModelTest {
  private static final RandomGenerator RANDOM = new SecureRandom();

  @Nested
  class Layer2RelativeTest {
    @ParameterizedTest
    @ValueSource(doubles = {-1.1, -1.0, -0.9, 0.0, 0.9, 1.0, 1.1})
    void get(double k) {
      Model.Layer2Relative layer2Relative = new Model.Layer2Relative(K.of(k), Math.clamp(RANDOM.nextDouble() - 0.5, 0.0, 1.0));
      assertAll(layer2Relative.toString(),
          () -> assertThat(layer2Relative.k().value()).isBetween(-1.0, 1.0),
          () -> assertThat(layer2Relative.h()).isNotNegative()
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
          () -> assertThat(layer2Relative.k().value()).isBetween(-1.0, 1.0),
          () -> assertThat(layer2Relative.h()).isNotNegative(),
          () -> assertThat(layer2Relative).hasToString(
              Stream.of(ValuePair.Name.K12.of(k.value(), 0.0), ValuePair.Name.H.of(h, 0.0))
                  .map(ValuePair::toString).collect(Collectors.joining("; "))
          )
      );
    }

    @ParameterizedTest
    @ValueSource(doubles = {-1.0})
    void negativeH(double h) {
      assertThatIllegalArgumentException().isThrownBy(() -> new Model.Layer2Relative(K.of(RANDOM.nextDouble()), h))
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
      Model.Layer2RelativeDh layer2Relative = new Model.Layer2RelativeDh(k, h, dh);
      assertAll(layer2Relative.toString(),
          () -> assertThat(layer2Relative.layer2Relative().k().value()).isBetween(-1.0, 1.0),
          () -> assertThat(layer2Relative.layer2Relative().h()).isNotNegative(),
          () -> assertThat(layer2Relative.dh()).isEqualTo(dh),
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
      assertThatIllegalArgumentException().isThrownBy(() -> new Model.Layer2RelativeDh(K.of(RANDOM.nextDouble()), h, RANDOM.nextDouble()))
          .withMessageStartingWith("h = ").withMessageEndingWith("must be non-negative");
    }
  }

  @Nested
  class Layer3AbsoluteTest {
    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
         2.0 | 8.0 | 4.0 | 1.0 | 2.0
         2.0 | 1.0 | 4.0 | 2.0 | 1.0
        """)
    void get(double rho1, double rho2, double rho3, double h1mm, double h2mh1mm) {
      double hStep = Metrics.Length.MILLI.toSI(0.01);
      double h1 = Metrics.Length.MILLI.toSI(h1mm);
      double h2mh1 = Metrics.Length.MILLI.toSI(h2mh1mm);
      Model.Layer3Absolute layer3Absolute = new Model.Layer3Absolute(rho1, rho2, rho3, hStep, new Model.P(h1 / hStep, h2mh1 / hStep));
      assertAll(layer3Absolute.toString(),
          () -> assertThat(layer3Absolute.rho1()).isNotNegative(),
          () -> assertThat(layer3Absolute.rho2()).isNotNegative(),
          () -> assertThat(layer3Absolute.rho3()).isNotNegative(),
          () -> assertThat(layer3Absolute.hStep()).isNotNegative(),
          () -> assertThat(layer3Absolute.p().p1()).isNotNegative(),
          () -> assertThat(layer3Absolute.p().p2mp1()).isNotNegative(),
          () -> assertThat(layer3Absolute.p().pSum()).isEqualTo(layer3Absolute.p().p1() + layer3Absolute.p().p2mp1()),
          () -> assertThat(layer3Absolute).hasToString(
              Stream.of(
                      ValuePair.Name.RHO_1.of(rho1, 0.0),
                      ValuePair.Name.RHO_2.of(rho2, 0.0),
                      ValuePair.Name.RHO_3.of(rho3, 0.0),
                      ValuePair.Name.H1.of(h1, 0.0),
                      ValuePair.Name.H2.of(h1 + h2mh1, 0.0)
                  )
                  .map(ValuePair::toString).collect(Collectors.joining("; "))
          )
      );
    }

    @ParameterizedTest
    @ValueSource(doubles = {-1.0})
    void negativeH(double h) {
      assertThatIllegalArgumentException().isThrownBy(() -> new Model.Layer3Absolute(
              RANDOM.nextDouble(), RANDOM.nextDouble(), RANDOM.nextDouble(), h, new Model.P(0, 0))
          )
          .withMessageStartingWith("hStep = ").withMessageEndingWith("must be non-negative");
    }
  }

  @Nested
  class Layer3AbsoluteDRho2Test {
    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
         2.0 | 8.0 | 4.0 | 1.0 | 2.0 | 0.01 | 0.18 | 0.1
         2.0 | 1.0 | 4.0 | 2.0 | 1.0 | 0.02 | 0.09 | -0.2
        """)
    void get(double rho1, double rho2, double rho3, double h1mm, double h2mh1mm, double dh1mm, double dh2mm, double dRho2) {
      double hStep = Metrics.Length.MILLI.toSI(0.01);
      double h1 = Metrics.Length.MILLI.toSI(h1mm);
      double h2mh1 = Metrics.Length.MILLI.toSI(h2mh1mm);
      double dh1 = Metrics.Length.MILLI.toSI(dh1mm);
      double dh2mh1 = Metrics.Length.MILLI.toSI(dh2mm);
      Model.Layer3AbsoluteDRho2 layer3AbsoluteDRho2 = new Model.Layer3AbsoluteDRho2(
          new Model.Layer3Absolute(rho1, rho2, rho3, hStep, new Model.P(h1 / hStep, h2mh1 / hStep)),
          new Model.P(dh1 / hStep, dh2mh1 / hStep), dRho2
      );
      Model.Layer3Absolute layer3Absolute = layer3AbsoluteDRho2.layer3Absolute();
      assertAll(layer3AbsoluteDRho2.toString(),
          () -> assertThat(layer3Absolute.rho1()).isNotNegative(),
          () -> assertThat(layer3Absolute.rho2()).isNotNegative(),
          () -> assertThat(layer3Absolute.rho3()).isNotNegative(),
          () -> assertThat(layer3Absolute.hStep()).isNotNegative(),
          () -> assertThat(layer3Absolute.p().p1()).isNotNegative(),
          () -> assertThat(layer3Absolute.p().p2mp1()).isNotNegative(),
          () -> assertThat(layer3Absolute.p().pSum()).isEqualTo(layer3Absolute.p().p1() + layer3Absolute.p().p2mp1()),
          () -> assertThat(layer3AbsoluteDRho2.dp().p1()).isNotNegative(),
          () -> assertThat(layer3AbsoluteDRho2.dp().p2mp1()).isNotNegative(),
          () -> assertThat(layer3AbsoluteDRho2.dp().pSum()).isEqualTo(layer3AbsoluteDRho2.dp().p1() + layer3AbsoluteDRho2.dp().p2mp1()),
          () -> assertThat(layer3AbsoluteDRho2.dRho2()).isNotZero(),
          () -> assertThat(layer3AbsoluteDRho2).hasToString(
              Stream.of(
                      ValuePair.Name.RHO_1.of(rho1, 0.0),
                      ValuePair.Name.RHO_2.of(rho2, 0.0),
                      ValuePair.Name.RHO_3.of(rho3, 0.0),
                      ValuePair.Name.H1.of(h1, 0.0),
                      ValuePair.Name.H2.of(h1 + h2mh1, 0.0),
                      ValuePair.Name.DH1.of(dh1, 0.0),
                      ValuePair.Name.DH2.of(dh1 + dh2mh1, 0.0),
                      ValuePair.Name.D_RHO_2.of(dRho2, 0.0)
                  )
                  .map(ValuePair::toString).collect(Collectors.joining("; "))
          )
      );
    }

    @ParameterizedTest
    @ValueSource(doubles = {-1.0})
    void negativeH(double h) {
      assertThatIllegalArgumentException().isThrownBy(() -> new Model.Layer3AbsoluteDRho2(new Model.Layer3Absolute(
              RANDOM.nextDouble(), RANDOM.nextDouble(), RANDOM.nextDouble(),
              h, new Model.P(0, 0)
          ), new Model.P(0, 0), 1.0))
          .withMessageStartingWith("hStep = ").withMessageEndingWith("must be non-negative");
    }
  }
}