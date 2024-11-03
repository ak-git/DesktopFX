package com.ak.rsm.resistance;

import com.ak.rsm.system.TetrapolarSystem;
import com.ak.util.Metrics;
import com.ak.util.Strings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.annotation.Nonnegative;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static tech.units.indriya.unit.Units.METRE;

class TetrapolarResistanceTest {
  private static final int SCALE = 10;

  static Stream<Arguments> tetrapolarResistivity() {
    return Stream.of(
        arguments(
            TetrapolarResistance.ofSI(1.0, 2.0).rho(900.1),
            "1000 000   2000 000     382 014        900 100",
            382.014,
            900.1
        ),
        arguments(
            TetrapolarResistance.ofSI(2.0, 1.0).rho(900.2),
            "2000 000   1000 000     382 057        900 200",
            382.057,
            900.2
        ),
        arguments(
            TetrapolarResistance.ofMilli(10.0, 30.0).rho(8.1),
            "10 000   30 000     128 916        8 100",
            128.916,
            8.1
        ),
        arguments(
            TetrapolarResistance.ofMilli(50.0, 30.0).rho(8.2),
            "50 000   30 000     195 761        8 200",
            195.761,
            8.2
        ),
        arguments(
            TetrapolarResistance.ofMilli(10.0, 30.0).rho1(1.0).rho2(1.0).h(Math.random()),
            "10 000   30 000     15 915        1 000",
            15.915,
            1.0
        ),
        arguments(
            TetrapolarResistance.ofMilli(10.0, 30.0).rho1(10.0).rho2(1.0).h(5.0),
            "10 000   30 000     53 901        3 387",
            53.901,
            3.39
        ),
        arguments(
            TetrapolarResistance.ofMilli(10.0, 20.0).rho1(8.0).rho2(2.0).rho3(1.0).hStep(5.0 / SCALE)
                .p(SCALE, SCALE).hChanged(TetrapolarResistance.LayersBuilder5.HChanged.H1),
            "10 000   20 000     242 751        5 720",
            242.751,
            5.72
        ),

        arguments(
            TetrapolarResistance.ofMilli(30.0, 60.0).ofOhms(1.0 / Math.PI),
            "30 000   60 000     0 318        0 023",
            0.318,
            9.0 / 400.0
        ),
        arguments(
            TetrapolarResistance.ofMilli(90.0, 30.0).ofOhms(1.0 / Math.PI),
            "90 000   30 000     0 318        0 060",
            0.318,
            3.0 / 50.0
        ),
        arguments(
            TetrapolarResistance.ofMilli(40.0, 80.0).ofOhms(1.0 / Math.PI),
            "40 000   80 000     0 318        0 030",
            0.318,
            3.0 / 100.0
        ),

        arguments(
            TetrapolarResistance.of(new TetrapolarSystem(Metrics.Length.MILLI.to(10.0, METRE), Metrics.Length.MILLI.to(30.0, METRE))).rho(8.1),
            "10 000   30 000     128 916        8 100",
            128.916,
            8.1
        ),

        arguments(
            TetrapolarResistance.ofSI(1.0, 2.0).ofOhms(382.014),
            "1000 000   2000 000     382 014        900 099",
            382.014,
            900.1
        )
    );
  }

  @ParameterizedTest
  @MethodSource("tetrapolarResistivity")
  void test(Resistance t, String expected, @Nonnegative double ohms, @Nonnegative double resistivity) {
    assertAll(t.toString(),
        () -> assertThat(t.toString().replaceAll("\\D", " ").strip()).isEqualTo(expected),
        () -> assertThat(t.ohms()).isCloseTo(ohms, byLessThan(0.01)),
        () -> assertThat(t.resistivity()).isCloseTo(resistivity, byLessThan(0.01))
    );
  }

  static Stream<Arguments> tetrapolarMultiResistivity() {
    return Stream.of(
        arguments(
            TetrapolarResistance.milli().system2(6.0).rho(1.0, 2.0),
            "600018000265261000 3000018000795772000",
            new double[] {1.0, 2.0}
        ),
        arguments(
            TetrapolarResistance.milli().system2(7.0).rho1(10.0).rho2(1.0).h(5.0),
            "7000210001241605461 35000210001467104302",
            new double[] {5.46, 4.30}
        ),
        arguments(
            TetrapolarResistance.milli().system2(8.0).rho1(8.0).rho2(2.0).rho3(1.0).hStep(5.0).p(1, 1)
                .hChanged(TetrapolarResistance.LayersBuilder5.HChanged.H1),
            "800024000886174454 40000240001079853619",
            new double[] {4.45, 3.62}
        ),

        arguments(
            TetrapolarResistance.milli().system4(6.0).rho(1.0, 2.0, 3.0, 4.0),
            "600018000265261000 3000018000795772000 12000240001061033000 36000240001697654000",
            new double[] {1.0, 2.0, 3.0, 4.0}
        ),
        arguments(
            TetrapolarResistance.milli().system4(7.0).rho1(10.0).rho2(1.0).h(5.0),
            "7000210001241605461 35000210001467104302 14000280001415264668 42000280001492924104",
            new double[] {5.461, 4.302, 4.668, 4.104}
        ),
        arguments(
            TetrapolarResistance.milli().system4(8.0).rho1(8.0).rho2(2.0).rho3(1.0).hStep(5.0).p(1, 1)
                .hChanged(TetrapolarResistance.LayersBuilder5.HChanged.H1),
            "800024000886174454 40000240001079853619 16000320001031643889 48000320001103903468",
            new double[] {4.454, 3.619, 3.889, 3.468}
        ),

        arguments(
            TetrapolarResistance.milli().system2(10.0).ofOhms(128.916, 195.761),
            "10000300001289168100 50000300001957618200",
            new double[] {8.1, 8.2}
        )
    );
  }

  @ParameterizedTest
  @MethodSource("tetrapolarMultiResistivity")
  void testMulti(Collection<Resistance> ms, String expected, double[] resistivity) {
    assertThat(
        ms.stream().map(
                m -> m.toString().replaceAll("\\D", " ").strip().replaceAll(Strings.SPACE, Strings.EMPTY)
            )
            .collect(Collectors.joining(Strings.SPACE))
    ).withFailMessage(ms::toString).isEqualTo(expected);
    assertThat(ms.stream().mapToDouble(Resistance::resistivity).toArray())
        .withFailMessage(ms::toString).containsExactly(resistivity, byLessThan(0.01));
  }

  @Test
  void testInvalidBuild() {
    var builder = TetrapolarResistance.milli().system2(10.0);
    assertThatIllegalArgumentException().isThrownBy(() -> builder.rho(1.0));
    assertThatIllegalArgumentException().isThrownBy(() -> builder.rho(1.0, 2.0, 3.0));
    assertThatIllegalArgumentException().isThrownBy(() -> builder.ofOhms(1.0, 2.0, 3.0));
  }

  @Test
  void testInvalidOhms2() {
    var builder = TetrapolarResistance.ofMilli(40.0, 80.0);
    assertThatIllegalArgumentException().isThrownBy(() -> builder.ofOhms(1.0 / Math.PI, 1.0 / Math.PI));
  }
}