package com.ak.rsm2;

import com.ak.util.Metrics;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class TetrapolarMeasurementTest {
  @ParameterizedTest
  @CsvSource(delimiter = ',', textBlock = """
      30.971, 31.278, -0.05, METRE
      31.278, 30.971,  0.05, MILLI
      """)
  void apparent(double rBefore, double rAfter, double dH, Metrics.Length units) {
    TetrapolarMeasurement measurement = TetrapolarMeasurement.builder(units).ohms(rBefore).dh(dH).thenOhms(rAfter).build();
    assertAll(measurement.toString(),
        () -> assertThat(measurement.ohms()).isEqualTo(rBefore),
        () -> assertThat(measurement.dOhms()).isEqualTo(rAfter - rBefore),
        () -> assertThat(measurement.dh()).isEqualTo(units.toSI(dH))
    );
  }
}