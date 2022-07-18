package com.ak.rsm.measurement;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import javax.annotation.Nonnegative;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.rsm.prediction.TetrapolarPrediction;
import com.ak.rsm.relative.RelativeMediumLayers;
import com.ak.rsm.resistance.Resistance;
import com.ak.rsm.resistance.TetrapolarResistance;
import com.ak.rsm.system.InexactTetrapolarSystem;
import com.ak.rsm.system.TetrapolarSystem;
import com.ak.util.Metrics;
import com.ak.util.Strings;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.byLessThan;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class TetrapolarMeasurementTest {
  static Stream<Arguments> tetrapolarMeasurements() {
    return Stream.of(
        arguments(
            TetrapolarMeasurement.ofSI(0.01).system(1.0, 2.0).rho(900.1),
            "1000 000   2000 000      10 0       1959          900   27 0",
            900.1),
        arguments(
            TetrapolarMeasurement.ofSI(0.1).system(2.0, 1.0).rho(900.2),
            "2000 000   1000 000      100 0       909          900   270 1",
            900.2
        ),
        arguments(
            TetrapolarMeasurement.ofMilli(0.01).system(10.0, 30.0).rho(8.1),
            "10 000   30 000      0 0       77          8 10   0 016",
            8.1
        ),
        arguments(
            TetrapolarMeasurement.ofMilli(0.1).system(50.0, 30.0).rho(8.2),
            "50 000   30 000      0 1       61          8 2   0 11",
            8.2
        ),
        arguments(
            TetrapolarMeasurement.ofMilli(0.1).system(10.0, 30.0).rho1(1.0).rho2(1.0).h(Math.random()),
            "10 000   30 000      0 1       36          1 00   0 020",
            1.0
        ),
        arguments(
            TetrapolarMeasurement.ofMilli(0.1).system(10.0, 30.0).rho1(10.0).rho2(1.0).h(5.0),
            "10 000   30 000      0 1       36          3 39   0 068",
            3.39
        ),
        arguments(
            TetrapolarMeasurement.ofMilli(0.1).system(10.0, 20.0)
                .rho1(8.0).rho2(2.0).rho3(1.0).hStep(5.0).p(1, 1),
            "10 000   20 000      0 1       20          5 7   0 17",
            5.72
        ),

        arguments(
            TetrapolarMeasurement.ofMilli(0.1).system(30.0, 60.0).ofOhms(1.0 / Math.PI),
            "30 000   60 000      0 1       85          0 0225   0 00023",
            9.0 / 400.0
        ),
        arguments(
            TetrapolarMeasurement.ofMilli(0.1).system(90.0, 30.0).ofOhms(1.0 / Math.PI),
            "90 000   30 000      0 1       154          0 0600   0 00040",
            3.0 / 50.0
        ),
        arguments(
            TetrapolarMeasurement.ofMilli(0.1).system(40.0, 80.0).ofOhms(1.0 / Math.PI),
            "40 000   80 000      0 1       124          0 0300   0 00023",
            3.0 / 100.0
        ),

        arguments(
            TetrapolarMeasurement.of(
                new InexactTetrapolarSystem(0.1, new TetrapolarSystem(Metrics.fromMilli(10.0), Metrics.fromMilli(30.0)))
            ).rho(8.1),
            "10 000   30 000      100 0       4          8   162 0",
            8.1
        )
    );
  }

  @ParameterizedTest
  @MethodSource("tetrapolarMeasurements")
  @ParametersAreNonnullByDefault
  void test(Measurement measurement, String expected, @Nonnegative double resistivity) {
    assertAll(measurement.toString(),
        () -> assertThat(measurement.toString().replaceAll("\\D", " ").strip()).isEqualTo(expected),
        () -> assertThat(measurement.resistivity()).isCloseTo(resistivity, byLessThan(0.01)),
        () -> assertThat(measurement.toPrediction(RelativeMediumLayers.SINGLE_LAYER, 1.0))
            .isEqualTo(TetrapolarPrediction.of(measurement, RelativeMediumLayers.SINGLE_LAYER, 1.0))
    );
  }

  @Test
  void testMerge() {
    Measurement m1 = TetrapolarMeasurement.ofSI(0.1).system(10.0, 30.0).rho(1.0);
    assertThat(m1.merge(m1)).as(m1.toString()).satisfies(m -> {
      assertThat(m.resistivity()).isCloseTo(1.0, byLessThan(0.001));
      assertThat(m.inexact().getApparentRelativeError()).isCloseTo(0.014, byLessThan(0.001));
    });

    Measurement m2 = TetrapolarMeasurement.ofSI(0.001).system(10.0, 30.0).rho(10.0);
    assertThat(m1.merge(m2))
        .satisfies(m -> {
          assertThat(m).hasToString(m2.merge(m1).toString());
          assertThat(m.resistivity()).isCloseTo(9.91, byLessThan(0.01));
        });

    assertThat(m2.merge(m1).inexact().getApparentRelativeError()).isCloseTo(0.002, byLessThan(0.002));
  }

  static Stream<Arguments> tetrapolarMultiMeasurements() {
    return Stream.of(
        arguments(
            TetrapolarMeasurement.milli(0.1).system2(6.0).rho(1.0),
            "60001800001181000033 300001800001311000022",
            new double[] {1.0, 1.0}
        ),
        arguments(TetrapolarMeasurement.milli(0.1).system2(7.0).rho1(10.0).rho2(1.0).h(5.0),
            "700021000012255016 350002100001384300082",
            new double[] {5.46, 4.30}
        ),
        arguments(
            TetrapolarMeasurement.milli(0.1).system2(8.0).rho1(8.0).rho2(2.0).rho3(1.0).hStep(5.0).p(1, 1),
            "800024000012645011 400002400001453620060",
            new double[] {4.45, 3.62}
        ),

        arguments(
            TetrapolarMeasurement.milli(-0.1).withShiftError().system2(8.0)
                .rho1(8.0).rho2(2.0).rho3(1.0).hStep(5.0).p(1, 1),
            "790024100012744011 399002410001453660061",
            new double[] {4.42, 3.65}
        ),
        arguments(
            TetrapolarMeasurement.milli(0.1).withShiftError().system2(8.0)
                .rho1(8.0).rho2(2.0).rho3(1.0).hStep(5.0).p(1, 1),
            "810023900012645011 401002390001463580059",
            new double[] {4.49, 3.58}
        ),

        arguments(
            TetrapolarMeasurement.milli(0.1).system2(10.0).ofOhms(15.915, 23.873),
            "100003000001361000020 500003000001611000013",
            new double[] {1.0, 1.0}
        ),
        arguments(
            TetrapolarMeasurement.milli(0.1).system4(10.0).ofOhms(
                TetrapolarResistance.milli().system4(10.0).rho1(1.0).rho2(1.0).h(10.0)
                    .stream().mapToDouble(Resistance::ohms).toArray()
            ),
            "100003000001361000020 500003000001611000013 200004000001491000015 600004000001711000012",
            new double[] {1.0, 1.0, 1.0, 1.0}
        ),
        arguments(
            TetrapolarMeasurement.milli(0.1).system4(10.0).ofOhms(
                Measurements.fixOhms(15.915, 23.873, 10.61, 23.3425)
            ),
            "100003000001361000020 500003000001611000013 200004000001491000015 600004000001711000013",
            new double[] {1.0, 1.0, 1.0, 1.0}
        )
    );
  }

  @ParameterizedTest
  @MethodSource("tetrapolarMultiMeasurements")
  @ParametersAreNonnullByDefault
  void testMulti(Collection<Measurement> ms, String expected, double[] resistivity) {
    assertAll(ms.toString(),
        () -> assertThat(ms.stream().map(
                m -> m.toString().replaceAll("\\D", " ").strip().replaceAll(Strings.SPACE, Strings.EMPTY)
            ).collect(Collectors.joining(Strings.SPACE))
        ).isEqualTo(expected),
        () -> assertThat(ms.stream().mapToDouble(Measurement::resistivity).toArray()).containsExactly(resistivity, byLessThan(0.01))
    );
  }

  @RepeatedTest(3)
  void testInvalidOhms() {
    var builder = TetrapolarMeasurement.milli(0.01).system2(10.0);
    double[] rOhms = DoubleStream.generate(Math::random)
        .limit(Math.random() > 0.5 ? 1 : 3).toArray();
    assertThatIllegalArgumentException().isThrownBy(() -> builder.ofOhms(rOhms));
  }
}