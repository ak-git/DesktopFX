package com.ak.rsm.measurement;

import com.ak.math.ValuePair;
import com.ak.rsm.relative.Layer1RelativeMedium;
import com.ak.rsm.relative.Layer2RelativeMedium;
import com.ak.rsm.resistance.Resistance;
import com.ak.rsm.resistance.TetrapolarResistance;
import com.ak.util.Metrics;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Random;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.data.Percentage.withPercentage;

class MeasurementsTest {
  @Test
  void testGetBaseL() {
    Collection<DerivativeMeasurement> measurements = TetrapolarDerivativeMeasurement.milli(0.1).dh(0.21).system2(7.0)
        .ofOhms(122.3, 199.0, 122.3 + 0.1, 199.0 + 0.4);
    assertThat(Measurements.getBaseL(measurements)).isCloseTo(Metrics.fromMilli(7.0 * 3), withPercentage(1.0));
  }

  @RepeatedTest(10)
  void testGetRho1() {
    double randomRho = new Random().nextDouble(10.0) + 1.0;
    double sPUmm = 7.0;
    var measurements = TetrapolarMeasurement.milli(0.1).system2(sPUmm)
        .ofOhms(TetrapolarResistance.milli().system2(sPUmm).rho(randomRho, randomRho).stream().mapToDouble(Resistance::ohms).toArray());
    assertThat(
        Measurements.getRho1(measurements, Layer1RelativeMedium.SINGLE_LAYER)
    ).satisfies(valuePair -> assertThat(valuePair.value()).isCloseTo(randomRho, byLessThan(valuePair.absError())));

    var measurements2 = TetrapolarMeasurement.milli(0.1).system2(sPUmm)
        .ofOhms(
            TetrapolarResistance.milli().system2(sPUmm).rho1(randomRho).rho2(Double.POSITIVE_INFINITY).h(sPUmm)
                .stream().mapToDouble(Resistance::ohms).toArray()
        );
    assertThat(
        Measurements.getRho1(measurements2,
            new Layer2RelativeMedium(ValuePair.Name.K12.of(1.0, 0.01),
                ValuePair.Name.H_L.of(Metrics.fromMilli(sPUmm) / Measurements.getBaseL(measurements2), 0.0)
            )
        )
    ).satisfies(valuePair -> assertThat(valuePair.value()).isCloseTo(randomRho, within(valuePair.absError())));
  }

  @Test
  void testFixOhms() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> Measurements.fixOhms(1234))
        .withMessageStartingWith("Needs 4 or 8 values, but found: ").withMessageContaining("1234");
    assertThat(Measurements.fixOhms(122.3, 199.0, 66.0, 202.0))
        .containsExactly(122.3, 199.0, 66.0 * 2, 202.0 * 2 - 66.0 * 2);
    assertThat(Measurements.fixOhms(122.3, 199.0, 66.0, 202.0,
        122.3 + 0.1, 199.0 + 0.4, 66.0 + 0.1, 202.0 + 0.25))
        .containsExactly(122.3, 199.0, 66.0 * 2, 202.0 * 2 - 66.0 * 2,
            122.3 + 0.1, 199.0 + 0.4, (66.0 + 0.1) * 2, (202.0 + 0.25) * 2 - (66.0 + 0.1) * 2);
  }
}