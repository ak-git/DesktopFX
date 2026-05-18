package com.ak.rsm2;

import com.ak.util.Metrics;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class MeasurementsTest {
  @Test
  void empty() {
    assertThatIllegalArgumentException().isThrownBy(() -> Measurements.builder().build())
        .withMessage("measurements must not be empty");
  }

  @ParameterizedTest
  @CsvSource(delimiter = '|', textBlock = """
      129.040 | 0.1238 | 129.195 | 0.0985 | 0.090
      """)
  void measurements(double r1, double r1Diff, double r1F, double r1DiffF, double hDiffMax) {
    Measurements<TetrapolarMeasurement> measurements = Measurements.builder()
        .add(s -> s.ohms(r1).thenOhms(r1 + r1Diff).hDiffMax(hDiffMax, Metrics.Length.MILLI))
        .add(s -> s.ohms(r1F).thenOhms(r1F + r1DiffF).hDiffMax(hDiffMax, Metrics.Length.MILLI))
        .build();
    assertThat(measurements.measurements()).hasSize(2);
  }
}