package com.ak.rsm.measurement;

import com.ak.rsm.prediction.TetrapolarDerivativePrediction;
import com.ak.rsm.relative.Layer1RelativeMedium;
import com.ak.rsm.resistance.DerivativeResistivity;
import com.ak.rsm.resistance.Resistance;
import com.ak.rsm.resistance.TetrapolarResistance;
import com.ak.rsm.system.InexactTetrapolarSystem;
import com.ak.rsm.system.TetrapolarSystem;
import com.ak.util.Strings;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class TetrapolarDerivativeMeasurementTest {
  static Stream<Arguments> tetrapolarMeasurements() {
    return Stream.of(
        arguments(
            TetrapolarDerivativeMeasurement.ofSI(0.1).dh(Double.NaN).system(1.0, 2.0).rho(900.1, 1.0),
            "1000 000   2000 000      100 0       909         382 014         900   270 0              1 000",
            900.1,
            1.0,
            new InexactTetrapolarSystem(0.1, new TetrapolarSystem(1.0, 2.0))),
        arguments(
            TetrapolarDerivativeMeasurement.ofSI(0.1).dh(Double.NaN).system(2.0, 1.0).rho(900.2, -1.0),
            "2000 000   1000 000      100 0       909         382 057         900   270 1               1 000",
            900.2,
            -1.0,
            new InexactTetrapolarSystem(0.1, new TetrapolarSystem(1.0, 2.0))
        ),
        arguments(
            TetrapolarDerivativeMeasurement.ofMilli(0.1).dh(Double.NaN).system(10.0, 30.0).rho(8.1, 2.0),
            "10 000   30 000      0 1       36         128 916         8 1   0 16              2 000",
            8.1,
            2.0,
            new InexactTetrapolarSystem(0.0001, new TetrapolarSystem(0.01, 0.03))
        ),
        arguments(
            TetrapolarDerivativeMeasurement.ofMilli(1.0).dh(Double.NaN).system(50.0, 30.0).rho(8.2, -2.0),
            "50 000   30 000      1 0       28         195 761         8   1 1               2 000",
            8.2,
            -2.0,
            new InexactTetrapolarSystem(0.001, new TetrapolarSystem(0.03, 0.05))
        ),
        arguments(
            TetrapolarDerivativeMeasurement.ofMilli(0.1).dh(0.1).system(10.0, 30.0)
                .rho1(1.0).rho2(1.0).h(Math.random()),
            "10 000   30 000      0 1       36         15 915         1 00   0 020              0 000          0 000         0 100",
            1.0,
            0.0,
            new InexactTetrapolarSystem(0.0001, new TetrapolarSystem(0.01, 0.03))
        ),
        arguments(
            TetrapolarDerivativeMeasurement.ofMilli(0.1).dh(0.1).system(10.0, 30.0)
                .rho1(10.0).rho2(1.0).h(5.0),
            "10 000   30 000      0 1       36         53 901         3 39   0 068              31 312          1 661         0 100",
            3.39,
            31.312,
            new InexactTetrapolarSystem(0.0001, new TetrapolarSystem(0.01, 0.03))
        ),
        arguments(
            TetrapolarDerivativeMeasurement.ofMilli(0.1).dh(0.1).system(10.0, 20.0)
                .rho1(8.0).rho2(2.0).rho3(1.0).hStep(0.1).p(50, 50),
            "10 000   20 000      0 1       20         242 751         5 7   0 17              0 677          0 144         0 100",
            5.72,
            0.677,
            new InexactTetrapolarSystem(0.0001, new TetrapolarSystem(0.01, 0.02))
        ),

        arguments(
            TetrapolarDerivativeMeasurement.ofMilli(0.1).dh(0.01).system(30.0, 60.0)
                .ofOhms(1.0 / Math.PI, 2.0 / Math.PI),
            "30 000   60 000      0 1       85         0 318         0 0225   0 00023              135 000          0 318         0 010",
            9.0 / 400.0,
            9.0 / 400.0 * (60.0 / 0.01),
            new InexactTetrapolarSystem(0.0001, new TetrapolarSystem(0.03, 0.06))
        ),
        arguments(
            TetrapolarDerivativeMeasurement.ofMilli(0.1).dh(0.01).system(90.0, 30.0)
                .ofOhms(1.0 / Math.PI, 0.5 / Math.PI),
            "90 000   30 000      0 1       154         0 318         0 0600   0 00040               90 000           0 159         0 010",
            3.0 / 50.0,
            3.0 / 50.0 * (-30.0 / 0.01 / 2.0),
            new InexactTetrapolarSystem(0.0001, new TetrapolarSystem(0.09, 0.03))
        ),
        arguments(
            TetrapolarDerivativeMeasurement.ofMilli(0.1).dh(0.01).system(40.0, 80.0)
                .ofOhms(1.0 / Math.PI, 3.0 / Math.PI),
            "40 000   80 000      0 1       124         0 318         0 0300   0 00023              480 000          0 637         0 010",
            3.0 / 100.0,
            3.0 / 100.0 * (80.0 / 0.01 * 2.0),
            new InexactTetrapolarSystem(0.0001, new TetrapolarSystem(0.04, 0.08))
        )
    );
  }

  @ParameterizedTest
  @MethodSource("tetrapolarMeasurements")
  @ParametersAreNonnullByDefault
  void test(DerivativeMeasurement d, String expected, @Nonnegative double resistivity,
            double derivativeResistivity, InexactTetrapolarSystem system) {
    assertAll(d.toString(),
        () -> assertThat(d.toString().replaceAll("\\D", " ").strip()).isEqualTo(expected),
        () -> assertThat(d.resistivity()).isCloseTo(resistivity, byLessThan(0.01)),
        () -> assertThat(d.ohms()).isCloseTo(TetrapolarResistance.of(system.system()).rho(resistivity).ohms(), withinPercentage(0.1)),
        () -> assertThat(d.derivativeResistivity()).isCloseTo(derivativeResistivity, byLessThan(0.01)),
        () -> assertThat(d.inexact()).isEqualTo(system),
        () -> assertThat(d.toPrediction(Layer1RelativeMedium.SINGLE_LAYER, 1.0))
            .isEqualTo(TetrapolarDerivativePrediction.of(d, Layer1RelativeMedium.SINGLE_LAYER, 1.0))
    );
  }

  @Test
  void testMerge() {
    Measurement m1 = TetrapolarDerivativeMeasurement.ofSI(0.1).dh(Double.NaN).system(10.0, 30.0).rho(1.0, 2.0);
    assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> m1.merge(m1));
  }

  static Stream<Arguments> tetrapolarMultiMeasurements() {
    return Stream.of(
        arguments(
            TetrapolarDerivativeMeasurement.milli(0.1).dh(Double.NaN).system2(6.0).rho(1.0, 2.0, 3.0, 4.0),
            "60001800001182652610000333000 300001800001317957720000444000",
            new double[] {1.0, 2.0},
            new double[] {3.0, 4.0}
        ),
        arguments(
            TetrapolarDerivativeMeasurement.milli(0.1).dh(0.2).system2(7.0).rho1(10.0).rho2(1.0).h(5.0),
            "7000210000122124160550162476053610200 3500021000013814671043000822085267730200",
            new double[] {5.46, 4.30},
            new double[] {24.760, 20.852}
        ),
        arguments(
            TetrapolarDerivativeMeasurement.milli(0.1).dh(0.3).system2(8.0)
                .rho1(8.0).rho2(2.0).rho3(1.0).hStep(0.1).p(50, 50),
            "80002400001268861745011131403270300 400002400001451079853620060151605650300",
            new double[] {4.45, 3.62},
            new double[] {1.314, 1.516}
        ),

        arguments(
            TetrapolarDerivativeMeasurement.milli(-0.1).dh(0.01).withShiftError().system2(8.0)
                .rho1(8.0).rho2(2.0).rho3(1.0).hStep(0.01).p(500, 500),
            "79002410001278578444011138600110010 399002410001451109143660061154900200010",
            new double[] {4.42, 3.65},
            new double[] {1.385, 1.549}
        ),
        arguments(
            TetrapolarDerivativeMeasurement.milli(0.1).dh(0.01).withShiftError().system2(8.0)
                .rho1(8.0).rho2(2.0).rho3(1.0).hStep(0.01).p(500, 500),
            "81002390001269154745011134400110010 401002390001461051513580059156300190010",
            new double[] {4.49, 3.58},
            new double[] {1.344, 1.563}
        ),

        arguments(
            TetrapolarDerivativeMeasurement.milli(0.1).dh(0.01).system2(10.0)
                .ofOhms(1.0 / Math.PI, 2.0 / Math.PI,
                    2.0 / Math.PI, 1.0 / Math.PI),
            "100003000001360318002000000406000003180010 500003000001610637002670000364000003180010",
            new double[] {0.02, 0.027},
            new double[] {60.0, -40.0}
        ),

        arguments(
            TetrapolarDerivativeMeasurement.milli(0.1).dh(0.01).system4(10.0).ofOhms(
                DoubleStream.concat(
                    TetrapolarResistance.milli().system4(10.0).rho1(1.0).rho2(1.0).h(10.0)
                        .stream().mapToDouble(Resistance::ohms),
                    TetrapolarResistance.milli().system4(10.0).rho1(1.0).rho2(1.0).h(10.0 + 0.01)
                        .stream().mapToDouble(Resistance::ohms)
                ).toArray()
            ),
            "10000300000136159151000020000000000010 50000300000161238731000013000000000010 20000400000149212211000015000000000010 60000400000171254651000012000000000010",
            new double[] {1.0, 1.0, 1.0, 1.0},
            new double[] {0.0, 0.0, 0.0, 0.0}
        ),
        arguments(
            TetrapolarDerivativeMeasurement.milli(0.1).dh(0.01).system4(10.0).ofOhms(
                DoubleStream.concat(
                    TetrapolarResistance.milli().system4(10.0).rho1(1.0).rho2(10.0).h(10.0)
                        .stream().mapToDouble(Resistance::ohms),
                    TetrapolarResistance.milli().system4(10.0).rho1(1.0).rho2(10.0).h(10.0 + 0.01)
                        .stream().mapToDouble(Resistance::ohms)
                ).toArray()
            ),
            "10000300000136219691380028236900130010 50000300000161398971670022330900260010 20000400000149327971550023394700210010 60000400000171450531770022471900300010",
            new double[] {1.3803347238482202, 1.671206499066391, 1.5455064051064595, 1.7692129341322433},
            new double[] {-2.3685130919028903, -3.3087914656437785, -3.947243025762326, -4.71858988034235}
        ),
        arguments(
            TetrapolarDerivativeMeasurement.milli(0.1).dh(0.01).system4(10.0).ofOhms(
                DoubleStream.concat(
                    TetrapolarResistance.milli().system4(10.0)
                        .rho(1.0, 2.0, 3.0, 4.0).stream().mapToDouble(Resistance::ohms),
                    TetrapolarResistance.milli().system4(10.0)
                        .rho(1.1, 2.2, 3.3, 4.4).stream().mapToDouble(Resistance::ohms)
                ).toArray()
            ),
            "1000030000013615915100002030000015920010 5000030000016147746200002760000047750010 20000400000149636623000045120000063660010 6000040000017110185940000501600000101860010",
            new double[] {1.0, 2.0, 3.0, 4.0},
            new double[] {300.0, 600.0, 1200.0, 1600.0}
        )
    );
  }

  @ParameterizedTest
  @MethodSource("tetrapolarMultiMeasurements")
  @ParametersAreNonnullByDefault
  void testMulti(Collection<DerivativeMeasurement> ms, String expected, double[] resistivity, double[] derivativeResistivity) {
    assertAll(ms.toString(),
        () -> assertThat(ms.stream().map(
            m -> m.toString().replaceAll("\\D", " ").strip().replaceAll(Strings.SPACE, Strings.EMPTY)
        ).collect(Collectors.joining(Strings.SPACE))).isEqualTo(expected),
        () -> assertThat(ms.stream().mapToDouble(Measurement::resistivity).toArray()).containsExactly(resistivity, byLessThan(0.01)),
        () -> assertThat(ms.stream().mapToDouble(DerivativeMeasurement::derivativeResistivity).toArray()).containsExactly(derivativeResistivity, byLessThan(0.01))
    );
  }

  @RepeatedTest(3)
  void testInvalidOhms() {
    var builder = TetrapolarDerivativeMeasurement.ofSI(0.01).dh(0.1).system(10, 20.0);
    double[] rOhms = DoubleStream.generate(Math::random).limit(Math.random() > 0.5 ? 1 : 3).toArray();
    assertThatIllegalArgumentException().isThrownBy(() -> builder.ofOhms(rOhms));
  }

  @Test
  void testInvalidRhos() {
    var builder = TetrapolarDerivativeMeasurement.ofSI(0.01).dh(0.1).system(10, 20.0);
    assertThatIllegalStateException().isThrownBy(() -> builder.rho(1.0, 2.0, 3.0, 4.0));
  }

  static Stream<Arguments> derivativeMeasurements() {
    return Stream.of(
        arguments(
            TetrapolarDerivativeMeasurement.ofMilli(0.1).dh(0.1).system(6.0, 18.0)
                .rho1(9.0).rho2(1.0).h(5.0),
            TetrapolarDerivativeMeasurement.ofMilli(0.1).dh(0.1).system(6.0, 18.0)
                .rho1(9.0).rho2(9.0).rho3(1.0).hStep(0.1).p(25, 25)
        ),
        arguments(
            TetrapolarDerivativeMeasurement.ofMilli(0.1).dh(0.1).system(6.0, 18.0)
                .rho1(9.0).rho2(1.0).h(5.0),
            TetrapolarDerivativeMeasurement.ofMilli(0.1).dh(0.1).system(6.0, 18.0)
                .rho1(9.0).rho2(9.0).rho3(1.0).hStep(0.01).p(300, 200)
        ),
        arguments(
            TetrapolarDerivativeMeasurement.ofMilli(0.1).dh(0.1).system(6.0, 18.0)
                .rho1(1.0).rho2(9.0).h(10.0),
            TetrapolarDerivativeMeasurement.ofMilli(0.1).dh(0.1).system(6.0, 18.0)
                .rho1(1.0).rho2(1.0).rho3(9.0).hStep(0.1).p(50, 50)
        ),
        arguments(
            TetrapolarDerivativeMeasurement.ofMilli(0.1).dh(0.1).system(6.0, 18.0)
                .rho1(1.0).rho2(9.0).h(10.0),
            TetrapolarDerivativeMeasurement.ofMilli(0.1).dh(0.1).system(6.0, 18.0)
                .rho1(1.0).rho2(1.0).rho3(9.0).hStep(0.01).p(500, 500)
        )
    );
  }

  @ParameterizedTest
  @MethodSource("derivativeMeasurements")
  void testDerivativeMeasurements(@Nonnull DerivativeResistivity dm1, @Nonnull DerivativeResistivity dm2) {
    assertThat(dm1.resistivity()).isEqualTo(dm2.resistivity());
    assertThat(dm1.derivativeResistivity()).isCloseTo(dm2.derivativeResistivity(), byLessThan(0.01));
  }

  static Stream<Arguments> dResistivity() {
    return Stream.of(
        arguments(0.7, Double.POSITIVE_INFINITY, 10.0, 0.1),
        arguments(0.7, Double.POSITIVE_INFINITY, 20.0, -0.1),
        arguments(1.0, 1.0, 30.0, -0.1)
    );
  }

  @ParameterizedTest
  @MethodSource("dResistivity")
  void testDResistance(@Nonnegative double rho1, @Nonnegative double rho2, @Nonnegative double hmm, double dHmm) {
    double dRExpected = TetrapolarResistance.ofMilli(10.0, 30.0).rho1(rho1).rho2(rho2).h(hmm + dHmm).ohms() -
        TetrapolarResistance.ofMilli(10.0, 30.0).rho1(rho1).rho2(rho2).h(hmm).ohms();
    var dR = TetrapolarDerivativeMeasurement.ofMilli(0.1).dh(dHmm).system(10.0, 30.0)
        .rho1(rho1).rho2(rho2).h(hmm);
    assertThat(dR.dOhms()).as("%s".formatted(dR)).isCloseTo(dRExpected, byLessThan(1.0e-8));
  }

  @Test
  void testResistance() {
    DerivativeMeasurement m = TetrapolarDerivativeMeasurement.ofMilli(0.1).dh(0.201).system(10.0, 20.0)
        .ofOhms(100.0, 101.0);
    assertThat(m.ohms()).isCloseTo(100.0, byLessThan(1.0e-8));
    assertThat(m.dOhms()).isCloseTo(1.0, byLessThan(1.0e-8));
  }

  @Test
  void testInvalid3Layer() {
    var builder = TetrapolarDerivativeMeasurement.milli(0.1).dh(Double.NaN)
        .system2(6.0).rho1(9.0).rho2(1.0).rho3(4.0).hStep(0.1);
    assertThatIllegalArgumentException().isThrownBy(() -> builder.p(50, 50))
        .withMessage("dh NULL is not supported in 3-layer model");

    var builder2 = TetrapolarDerivativeMeasurement.milli(0.1).dh(0.01)
        .system2(6.0).rho1(9.0).rho2(1.0).rho3(4.0).hStep(0.1);
    assertThatIllegalArgumentException().isThrownBy(() -> builder2.p(50, 50))
        .withMessageContaining("|dh| < hStep");
  }
}