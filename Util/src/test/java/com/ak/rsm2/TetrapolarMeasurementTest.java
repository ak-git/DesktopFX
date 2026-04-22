package com.ak.rsm2;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class TetrapolarMeasurementTest {
  @ParameterizedTest
  @CsvSource(delimiter = ',', textBlock = """
      30.971, 31.278
      31.278, 30.971
      """)
  void apparent(double rBefore, double rAfter) {
    TetrapolarMeasurement measurement = TetrapolarMeasurement.builder().ohms(rBefore).thenOhms(rAfter).build();
    assertAll(measurement.toString(),
        () -> assertThat(measurement.ohms()).isEqualTo(rBefore),
        () -> assertThat(measurement.ohmsDiff()).isEqualTo(rAfter - rBefore)
    );
  }
}