package com.ak.rsm2;

import com.ak.util.Metrics;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class TetrapolarDiffMeasurementTest {
  @ParameterizedTest
  @CsvSource(delimiter = ',', textBlock = """
      30.971, 31.278, -0.05, METRE
      31.278, 30.971,  0.05, MILLI
      """)
  void apparent(double rBefore, double rAfter, double hDiff, Metrics.Length units) {
    TetrapolarMeasurement.Diff measurement = TetrapolarMeasurement.builder().ohms(rBefore).thenOhms(rAfter).hDiff(hDiff, units).build();
    assertAll(measurement.toString(),
        () -> assertThat(measurement.ohms()).isEqualTo(rBefore),
        () -> assertThat(measurement.ohmsDiff()).isEqualTo(rAfter - rBefore),
        () -> assertThat(measurement.hDiff()).isEqualTo(units.toSI(hDiff))
    );
  }
}