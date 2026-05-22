package com.ak.rsm2;

import com.ak.util.Metrics;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.byLessThan;
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

  @Nested
  class Diff {
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

  @Nested
  class MaxDiff {
    @ParameterizedTest
    @CsvSource(delimiter = ',', textBlock = """
        30.971, 31.278, -0.05, METRE
        31.278, 30.971,  0.05, MILLI
        """)
    void apparent(double rBefore, double rAfter, double hDiff, Metrics.Length units) {
      TetrapolarMeasurement.MaxDiff measurement = TetrapolarMeasurement.builder().ohms(rBefore).thenOhms(rAfter).hDiffMax(hDiff, units).build();
      assertAll(measurement.toString(),
          () -> assertThat(measurement.ohms()).isEqualTo(rBefore),
          () -> assertThat(measurement.ohmsDiff()).isEqualTo(rAfter - rBefore),
          () -> assertThat(measurement.hDiffMax()).isEqualTo(units.toSI(hDiff))
      );
    }
  }

  @Nested
  class TwoMaxDiff {
    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
        129.040 | 0.1238 | 129.195 | 0.0985 | 0.090
        """)
    void apparent(double r1, double r2, double r1Diff, double r2Diff, double hDiffMax) {
      Metrics.Length units = Metrics.Length.MILLI;
      var measurement = TetrapolarMeasurement.builder().ohms(r1).thenOhms(r1 + r1Diff).hDiffMax(hDiffMax, units)
          .add(m2 -> m2.ohms(r2).thenOhms(r2 + r2Diff).hDiffMax(hDiffMax, units)).build();
      TetrapolarMeasurement.MaxDiff maxDiff = measurement.next();
      assertAll(maxDiff.toString(),
          () -> assertThat(maxDiff.ohms()).isEqualTo(r2),
          () -> assertThat(maxDiff.ohmsDiff()).isCloseTo(r2Diff, byLessThan(0.001)),
          () -> assertThat(maxDiff.hDiffMax()).isEqualTo(units.toSI(hDiffMax))
      );
    }
  }
}