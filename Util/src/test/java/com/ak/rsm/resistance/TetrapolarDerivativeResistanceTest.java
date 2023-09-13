package com.ak.rsm.resistance;

import com.ak.rsm.system.TetrapolarSystem;
import com.ak.util.Metrics;
import com.ak.util.Strings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.annotation.Nonnegative;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class TetrapolarDerivativeResistanceTest {
  static Stream<Arguments> tetrapolarResistivity() {
    return Stream.of(
        arguments(
            TetrapolarDerivativeResistance.ofSI(1.0, 2.0).dh(Double.NaN).rho(900.1, 1.0),
            "1000 000   2000 000     382 014        900 100              1 000",
            382.014,
            900.1,
            1.0,
            new TetrapolarSystem(1.0, 2.0)
        ),
        arguments(
            TetrapolarDerivativeResistance.ofSI(2.0, 1.0).dh(Double.NaN).rho(900.2, -2.0),
            "2000 000   1000 000     382 057        900 200               2 000",
            382.057,
            900.2,
            -2.0,
            new TetrapolarSystem(1.0, 2.0)
        ),
        arguments(
            TetrapolarDerivativeResistance.ofMilli(10.0, 30.0).dh(Double.NaN).rho(8.1, -1.0),
            "10 000   30 000     128 916        8 100               1 000",
            128.916,
            8.1,
            -1.0,
            new TetrapolarSystem(0.01, 0.03)
        ),
        arguments(
            TetrapolarDerivativeResistance.ofMilli(50.0, 30.0).dh(Double.NaN).rho(8.2, 2.0),
            "50 000   30 000     195 761        8 200              2 000",
            195.761,
            8.2,
            2.0,
            new TetrapolarSystem(0.03, 0.05)
        ),
        arguments(
            TetrapolarDerivativeResistance.ofMilli(10.0, 30.0).dh(0.1).rho1(1.0).rho2(1.0).h(Math.random()),
            "10 000   30 000     15 915        1 000              0 000           0 100",
            15.915,
            1.0,
            0.0,
            new TetrapolarSystem(0.01, 0.03)
        ),
        arguments(
            TetrapolarDerivativeResistance.ofMilli(10.0, 30.0).dh(0.1).rho1(10.0).rho2(1.0).h(5.0),
            "10 000   30 000     53 901        3 387              31 312           0 100",
            53.901,
            3.39,
            31.312,
            new TetrapolarSystem(0.01, 0.03)
        ),
        arguments(
            TetrapolarDerivativeResistance.ofMilli(10.0, 20.0).dh(0.1).rho1(8.0).rho2(2.0).rho3(1.0).hStep(0.1).p(50, 50),
            "10 000   20 000     242 751        5 720              13 215           0 100",
            242.751,
            5.72,
            13.214859951943403,
            new TetrapolarSystem(0.01, 0.02)
        ),

        arguments(
            TetrapolarDerivativeResistance.ofMilli(30.0, 60.0).dh(0.01).ofOhms(1.0 / Math.PI, 2.0 / Math.PI),
            "30 000   60 000     0 318        0 023              135 000           0 010",
            0.318,
            9.0 / 400.0,
            9.0 / 400.0 * (60.0 / 0.01),
            new TetrapolarSystem(0.03, 0.06)
        ),
        arguments(
            TetrapolarDerivativeResistance.ofMilli(90.0, 30.0).dh(0.01).ofOhms(1.0 / Math.PI, 0.5 / Math.PI),
            "90 000   30 000     0 318        0 060               90 000           0 010",
            0.318,
            3.0 / 50.0,
            3.0 / 50.0 * (-30.0 / 0.01 / 2.0),
            new TetrapolarSystem(0.09, 0.03)
        ),
        arguments(
            TetrapolarDerivativeResistance.ofMilli(40.0, 80.0).dh(0.01).ofOhms(1.0 / Math.PI, 3.0 / Math.PI),
            "40 000   80 000     0 318        0 030              480 000           0 010",
            0.318,
            3.0 / 100.0,
            3.0 / 100.0 * (80.0 / 0.01 * 2.0),
            new TetrapolarSystem(0.04, 0.08)
        ),

        arguments(
            TetrapolarDerivativeResistance.of(new TetrapolarSystem(Metrics.fromMilli(10.0), Metrics.fromMilli(30.0)))
                .dh(Double.NaN).rho(8.1, -8.1),
            "10 000   30 000     128 916        8 100               8 100",
            128.916,
            8.1,
            -8.1,
            new TetrapolarSystem(0.01, 0.03)
        )
    );
  }

  @ParameterizedTest
  @MethodSource("tetrapolarResistivity")
  @ParametersAreNonnullByDefault
  void test(DerivativeResistance d, String expected,
            @Nonnegative double ohms, @Nonnegative double resistivity,
            double derivativeResistivity, TetrapolarSystem system) {
    assertAll(d.toString(),
        () -> assertThat(d.toString().replaceAll("\\D", " ").strip()).isEqualTo(expected),
        () -> assertThat(d.ohms()).isCloseTo(ohms, byLessThan(0.01)),
        () -> assertThat(d.resistivity()).isCloseTo(resistivity, byLessThan(0.01)),
        () -> assertThat(d.derivativeResistivity()).isCloseTo(derivativeResistivity, byLessThan(0.01)),
        () -> assertThat(d.system()).isEqualTo(system)
    );
  }

  static Stream<Arguments> tetrapolarMultiResistivity() {
    return Stream.of(
        arguments(
            TetrapolarDerivativeResistance.milli().dh(Double.NaN).system2(6.0).rho(1.0, 2.0, 3.0, 4.0),
            "6000180002652610003000 30000180007957720004000",
            new double[] {1.0, 2.0},
            new double[] {3.0, 4.0}
        ),
        arguments(
            TetrapolarDerivativeResistance.milli().dh(0.2).system2(7.0).rho1(10.0).rho2(1.0).h(5.0),
            "7000210001241605461247600200 35000210001467104302208520200",
            new double[] {5.46, 4.30},
            new double[] {24.760, 20.852}
        ),
        arguments(
            TetrapolarDerivativeResistance.milli().dh(0.3).system2(8.0).rho1(8.0).rho2(2.0).rho3(1.0).hStep(0.1).p(50, 50),
            "800024000886174454189590300 40000240001079853619158430300",
            new double[] {4.45, 3.62},
            new double[] {18.958526157968976, 15.842787775068425}
        ),
        arguments(
            TetrapolarDerivativeResistance.milli().dh(0.01).system2(10.0)
                .ofOhms(1.0 / Math.PI, 2.0 / Math.PI,
                    2.0 / Math.PI, 1.0 / Math.PI),
            "100003000003180020600000010 500003000006370027400000010",
            new double[] {0.02, 0.027},
            new double[] {60.0, -40.0}
        )
    );
  }

  @ParameterizedTest
  @MethodSource("tetrapolarMultiResistivity")
  @ParametersAreNonnullByDefault
  void testMulti(Collection<DerivativeResistance> ms, String expected, double[] resistivity, double[] derivativeResistivity) {
    assertAll(ms.toString(),
        () -> assertThat(ms.stream().map(
            m -> m.toString().replaceAll("\\D", " ").strip().replaceAll(Strings.SPACE, Strings.EMPTY)
        ).collect(Collectors.joining(Strings.SPACE))).isEqualTo(expected),
        () -> assertThat(ms.stream().mapToDouble(Resistance::resistivity).toArray()).containsExactly(resistivity, byLessThan(0.01)),
        () -> assertThat(ms.stream().mapToDouble(DerivativeResistance::derivativeResistivity).toArray()).containsExactly(derivativeResistivity, byLessThan(0.01))
    );
  }

  @Test
  void testInvalidRhos() {
    var builder = TetrapolarDerivativeResistance.milli().dh(0.1).system2(10.0);
    assertThatIllegalArgumentException().isThrownBy(() -> builder.rho(1.0));
    assertThatIllegalArgumentException().isThrownBy(() -> builder.rho(1.0, 2.0, 3.0));
    assertThatIllegalStateException().isThrownBy(() -> builder.rho(1.0, 2.0, 3.0, 4.0));
  }

  @Test
  void testInvalidOhms() {
    var builder = TetrapolarDerivativeResistance.milli().dh(0.0).system2(10.0);
    assertThatIllegalArgumentException().isThrownBy(() -> builder.ofOhms(1.0, 2.0, 3.0));
  }

  @Test
  void testInvalidOhms2() {
    var builder = TetrapolarDerivativeResistance.ofMilli(40.0, 80.0).dh(-0.1);
    assertThatIllegalArgumentException().isThrownBy(() -> builder.ofOhms(1.0));
  }

  @Test
  void testInvalid3Layer() {
    var builder = TetrapolarDerivativeResistance.milli().dh(Double.NaN)
        .system2(6.0).rho1(9.0).rho2(1.0).rho3(4.0).hStep(0.1);
    assertThatIllegalArgumentException().isThrownBy(() -> builder.p(50, 50))
        .withMessage("dh NULL is not supported in 3-layer model");

    var builder2 = TetrapolarDerivativeResistance.milli().dh(0.01)
        .system2(6.0).rho1(9.0).rho2(1.0).rho3(4.0).hStep(0.1);
    assertThatIllegalArgumentException().isThrownBy(() -> builder2.p(50, 50))
        .withMessageContaining("|dh| < hStep");
  }
}