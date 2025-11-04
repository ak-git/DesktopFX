package com.ak.rsm.resistance;

import com.ak.rsm.system.TetrapolarSystem;
import com.ak.util.Metrics;
import com.ak.util.Strings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static tech.units.indriya.unit.Units.METRE;

class TetrapolarDerivativeResistanceTest {
  private static final int SCALE = 10;

  static Stream<Arguments> tetrapolarResistivityNulls() {
    return Stream.of(
        arguments(
            TetrapolarDerivativeResistance.ofSI(1.0, 2.0).dh(DeltaH.NULL).rho(900.1, 1.0),
            "1000 000   2000 000     382 014        900 100              1 000",
            382.014,
            900.1,
            1.0,
            new TetrapolarSystem(1.0, 2.0)
        ),
        arguments(
            TetrapolarDerivativeResistance.ofSI(2.0, 1.0).dh(DeltaH.NULL).rho(900.2, -2.0),
            "2000 000   1000 000     382 057        900 200               2 000",
            382.057,
            900.2,
            -2.0,
            new TetrapolarSystem(1.0, 2.0)
        ),
        arguments(
            TetrapolarDerivativeResistance.ofMilli(10.0, 30.0).dh(DeltaH.NULL).rho(8.1, -1.0),
            "10 000   30 000     128 916        8 100               1 000",
            128.916,
            8.1,
            -1.0,
            new TetrapolarSystem(0.01, 0.03)
        ),
        arguments(
            TetrapolarDerivativeResistance.ofMilli(50.0, 30.0).dh(DeltaH.NULL).rho(8.2, 2.0),
            "50 000   30 000     195 761        8 200              2 000",
            195.761,
            8.2,
            2.0,
            new TetrapolarSystem(0.03, 0.05)
        ),
        arguments(
            TetrapolarDerivativeResistance.of(
                    new TetrapolarSystem(Metrics.Length.MILLI.to(10.0, METRE), Metrics.Length.MILLI.to(30.0, METRE))
                )
                .dh(DeltaH.NULL).rho(8.1, -8.1),
            "10 000   30 000     128 916        8 100               8 100",
            128.916,
            8.1,
            -8.1,
            new TetrapolarSystem(0.01, 0.03)
        )
    );
  }

  static Stream<Arguments> tetrapolarResistivityH1() {
    return Stream.of(
        arguments(
            TetrapolarDerivativeResistance.ofMilli(10.0, 30.0).dh(DeltaH.H1.apply(0.1)).rho1(1.0).rho2(1.0).h(Math.random()),
            "10 000   30 000     15 915        1 000              0 000           0 100",
            15.915,
            1.0,
            0.0,
            new TetrapolarSystem(0.01, 0.03)
        ),
        arguments(
            TetrapolarDerivativeResistance.ofMilli(10.0, 30.0).dh(DeltaH.H1.apply(0.1)).rho1(10.0).rho2(1.0).h(5.0),
            "10 000   30 000     53 901        3 387              31 312           0 100",
            53.901,
            3.39,
            31.312,
            new TetrapolarSystem(0.01, 0.03)
        ),
        arguments(
            TetrapolarDerivativeResistance.ofMilli(10.0, 20.0).dh(DeltaH.H1.apply(0.1)).rho1(8.0).rho2(2.0).rho3(1.0)
                .hStep(0.1 / SCALE).p(50 * SCALE, 50 * SCALE),
            "10 000   20 000     242 751        5 720              13 215           0 100",
            242.751,
            5.72,
            13.214859951943403,
            new TetrapolarSystem(0.01, 0.02)
        ),
        arguments(
            TetrapolarDerivativeResistance.ofMilli(30.0, 60.0).dh(DeltaH.H1.apply(0.01))
                .ofOhms(1.0 / Math.PI, 2.0 / Math.PI),
            "30 000   60 000     0 318        0 023              135 000           0 010",
            0.318,
            9.0 / 400.0,
            9.0 / 400.0 * (60.0 / 0.01),
            new TetrapolarSystem(0.03, 0.06)
        ),
        arguments(
            TetrapolarDerivativeResistance.ofMilli(90.0, 30.0).dh(DeltaH.H1.apply(0.01))
                .ofOhms(1.0 / Math.PI, 0.5 / Math.PI),
            "90 000   30 000     0 318        0 060               90 000           0 010",
            0.318,
            3.0 / 50.0,
            3.0 / 50.0 * (-30.0 / 0.01 / 2.0),
            new TetrapolarSystem(0.09, 0.03)
        ),
        arguments(
            TetrapolarDerivativeResistance.ofMilli(40.0, 80.0).dh(DeltaH.H1.apply(0.01))
                .ofOhms(1.0 / Math.PI, 3.0 / Math.PI),
            "40 000   80 000     0 318        0 030              480 000           0 010",
            0.318,
            3.0 / 100.0,
            3.0 / 100.0 * (80.0 / 0.01 * 2.0),
            new TetrapolarSystem(0.04, 0.08)
        )
    );
  }

  static Stream<Arguments> tetrapolarResistivityH2() {
    return Stream.of(
        arguments(
            TetrapolarDerivativeResistance.ofMilli(10.0, 30.0).dh(DeltaH.H2.apply(0.1)).rho1(1.0).rho2(1.0).h(Math.random()),
            "10 000   30 000     15 915        1 000              0 000           0 100",
            15.915,
            1.0,
            0.0,
            new TetrapolarSystem(0.01, 0.03)
        ),
        arguments(
            TetrapolarDerivativeResistance.ofMilli(10.0, 30.0).dh(DeltaH.H2.apply(0.1)).rho1(10.0).rho2(1.0).h(5.0),
            "10 000   30 000     53 901        3 387              31 312           0 100",
            53.901,
            3.39,
            31.312,
            new TetrapolarSystem(0.01, 0.03)
        ),
        arguments(
            TetrapolarDerivativeResistance.ofMilli(10.0, 20.0).dh(DeltaH.H2.apply(0.1)).rho1(8.0).rho2(2.0).rho3(1.0)
                .hStep(0.1 / SCALE).p(50 * SCALE, 50 * SCALE),
            "10 000   20 000     242 751        5 720              0 677           0 100",
            242.751,
            5.72,
            0.6769285537755465,
            new TetrapolarSystem(0.01, 0.02)
        ),
        arguments(
            TetrapolarDerivativeResistance.ofMilli(30.0, 60.0).dh(DeltaH.H2.apply(0.01))
                .ofOhms(1.0 / Math.PI, 2.0 / Math.PI),
            "30 000   60 000     0 318        0 023              135 000           0 010",
            0.318,
            9.0 / 400.0,
            9.0 / 400.0 * (60.0 / 0.01),
            new TetrapolarSystem(0.03, 0.06)
        ),
        arguments(
            TetrapolarDerivativeResistance.ofMilli(90.0, 30.0).dh(DeltaH.H2.apply(0.01))
                .ofOhms(1.0 / Math.PI, 0.5 / Math.PI),
            "90 000   30 000     0 318        0 060               90 000           0 010",
            0.318,
            3.0 / 50.0,
            3.0 / 50.0 * (-30.0 / 0.01 / 2.0),
            new TetrapolarSystem(0.09, 0.03)
        ),
        arguments(
            TetrapolarDerivativeResistance.ofMilli(40.0, 80.0).dh(DeltaH.H2.apply(0.01))
                .ofOhms(1.0 / Math.PI, 3.0 / Math.PI),
            "40 000   80 000     0 318        0 030              480 000           0 010",
            0.318,
            3.0 / 100.0,
            3.0 / 100.0 * (80.0 / 0.01 * 2.0),
            new TetrapolarSystem(0.04, 0.08)
        )
    );
  }

  @ParameterizedTest
  @MethodSource({"tetrapolarResistivityNulls", "tetrapolarResistivityH1", "tetrapolarResistivityH2"})
  void test(DerivativeResistance d, String expected, double ohms, double resistivity,
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
            TetrapolarDerivativeResistance.milli().dh(DeltaH.NULL).system2(6.0).rho(1.0, 2.0, 3.0, 4.0),
            "6000180002652610003000 30000180007957720004000",
            new double[] {1.0, 2.0},
            new double[] {3.0, 4.0}
        ),
        arguments(
            TetrapolarDerivativeResistance.milli().dh(DeltaH.H1.apply(0.2)).system2(7.0).rho1(10.0).rho2(1.0).h(5.0),
            "7000210001241605461247600200 35000210001467104302208520200",
            new double[] {5.46, 4.30},
            new double[] {24.760, 20.852}
        ),
        arguments(
            TetrapolarDerivativeResistance.milli().dh(DeltaH.H1.apply(0.3)).system2(8.0).rho1(8.0).rho2(2.0).rho3(1.0)
                .hStep(0.1 / SCALE).p(50 * SCALE, 50 * SCALE),
            "800024000886174454189590300 40000240001079853619158430300",
            new double[] {4.45, 3.62},
            new double[] {18.958526157968976, 15.842787775068425}
        ),
        arguments(
            TetrapolarDerivativeResistance.milli().dh(DeltaH.H1.apply(0.01)).system2(10.0)
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
  void testMulti(Collection<DerivativeResistance> ms, String expected, double[] resistivity, double[] derivativeResistivity) {
    assertAll(ms.toString(),
        () -> assertThat(ms.stream().map(
            m -> m.toString().replaceAll("\\D", " ").strip().replaceAll(Strings.SPACE, Strings.EMPTY)
        ).collect(Collectors.joining(Strings.SPACE))).isEqualTo(expected),
        () -> assertThat(ms.stream().mapToDouble(Resistance::resistivity).toArray()).containsExactly(resistivity, byLessThan(0.01)),
        () -> assertThat(ms.stream().mapToDouble(DerivativeResistance::derivativeResistivity).toArray()).containsExactly(derivativeResistivity, byLessThan(0.01))
    );
  }

  static Stream<Arguments> dResistivity2() {
    return Stream.of(
        arguments(0.7, Double.POSITIVE_INFINITY, 10.0, 0.1),
        arguments(0.7, Double.POSITIVE_INFINITY, 20.0, -0.1),
        arguments(1.0, 1.0, 30.0, -0.1)
    );
  }

  @ParameterizedTest
  @MethodSource("dResistivity2")
  void testDResistivity2(double rho1, double rho2, double hmm, double dHmm) {
    TetrapolarResistance.PreBuilder<DerivativeResistance> resistanceBuilder =
        TetrapolarDerivativeResistance.ofMilli(10.0, 20.0).dh(DeltaH.H1.apply(dHmm));
    DerivativeResistance theory = resistanceBuilder.rho1(rho1).rho2(rho2).h(hmm);

    TetrapolarResistance.LayersBuilder2<Resistance> builder2 = TetrapolarResistance.ofMilli(10.0, 20.0)
        .rho1(rho1).rho2(rho2);
    DerivativeResistance measured = resistanceBuilder.ofOhms(builder2.h(hmm).ohms(), builder2.h(hmm + dHmm).ohms());
    assertThat(theory).isEqualTo(measured);
  }

  static Stream<Arguments> dResistivity3() {
    return Stream.of(
        arguments(DeltaH.H1.apply(0.1), 1, 0),
        arguments(DeltaH.H1.apply(-0.2), -2, 0),
        arguments(DeltaH.H2.apply(0.2), 0, 2),
        arguments(DeltaH.H2.apply(-0.2), 0, -2),
        arguments(DeltaH.ofH1andH2(0.1, 0.2), 1, 2)
    );
  }

  @ParameterizedTest
  @MethodSource("dResistivity3")
  void testDResistivity3(DeltaH dh, int dp1, int dp2) {
    double rho1 = 2.0;
    double rho2 = 10.0;
    double rho3 = 5.0;
    int p1 = 10;
    int p2mp1 = 20;
    double dHmm = 0.1;
    TetrapolarDerivativeResistance.PreBuilder preBuilder = TetrapolarDerivativeResistance.ofMilli(10.0, 20.0);
    TetrapolarResistance.PreBuilder<DerivativeResistance> theoryBuilder = preBuilder.dh(dh);
    DerivativeResistance theory = theoryBuilder.rho1(rho1).rho2(rho2).rho3(rho3).hStep(dHmm).p(p1, p2mp1);
    TetrapolarResistance.LayersBuilder4<Resistance> builder3 = TetrapolarResistance.ofMilli(10.0, 20.0)
        .rho1(rho1).rho2(rho2).rho3(rho3).hStep(dHmm);

    DerivativeResistance measured1 = preBuilder.dh(DeltaH.H1.apply(Arrays.stream(dh.values()).sum())).ofOhms(builder3.p(p1, p2mp1).ohms(), builder3.p(p1 + dp1, p2mp1 + dp2).ohms());
    DerivativeResistance measured2 = preBuilder.dh(DeltaH.H2.apply(Arrays.stream(dh.values()).sum())).ofOhms(builder3.p(p1, p2mp1).ohms(), builder3.p(p1 + dp1, p2mp1 + dp2).ohms());
    assertThat(theory).isEqualTo(measured1).isEqualTo(measured2);
    if (dh.values().length == 2) {
      DerivativeResistance measured3 = preBuilder.dh(dh).ofOhms(builder3.p(p1, p2mp1).ohms(), builder3.p(p1 + dp1, p2mp1 + dp2).ohms());
      assertThat(theory).isEqualTo(measured3);
    }
  }

  @Test
  void testInvalidRhos() {
    var builder = TetrapolarDerivativeResistance.milli().dh(DeltaH.H1.apply(0.1)).system2(10.0);
    assertThatIllegalArgumentException().isThrownBy(() -> builder.rho(1.0));
    assertThatIllegalArgumentException().isThrownBy(() -> builder.rho(1.0, 2.0, 3.0));
    assertThatIllegalStateException().isThrownBy(() -> builder.rho(1.0, 2.0, 3.0, 4.0));
  }

  @Test
  void testInvalidOhms() {
    var builder = TetrapolarDerivativeResistance.milli().dh(DeltaH.H1.apply(0.0)).system2(10.0);
    assertThatIllegalArgumentException().isThrownBy(() -> builder.ofOhms(1.0, 2.0, 3.0));
  }

  @Test
  void testInvalidOhms2() {
    var builder = TetrapolarDerivativeResistance.ofMilli(40.0, 80.0).dh(DeltaH.H1.apply(-0.1));
    assertThatIllegalArgumentException().isThrownBy(() -> builder.ofOhms(1.0));
  }

  @Test
  void testInvalid3LayerNullDh() {
    var builder = TetrapolarDerivativeResistance.milli().dh(DeltaH.NULL)
        .system2(6.0).rho1(9.0).rho2(1.0).rho3(4.0).hStep(0.1);
    assertThatIllegalArgumentException().isThrownBy(() -> builder.p(50, 50))
        .withMessage("dh NULL is not supported in 3-layer model");
  }

  @Test
  void testInvalid3LayerLowDh() {
    var builder = TetrapolarDerivativeResistance.milli().dh(DeltaH.H1.apply(0.01))
        .system2(6.0).rho1(9.0).rho2(1.0).rho3(4.0).hStep(0.1);
    assertThatIllegalArgumentException().isThrownBy(() -> builder.p(50, 50))
        .withMessageStartingWith("|dh = ")
        .withMessageContaining("< |hStep = ");
  }
}