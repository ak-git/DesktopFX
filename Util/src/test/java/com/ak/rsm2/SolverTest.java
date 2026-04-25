package com.ak.rsm2;

import com.ak.util.Metrics;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class SolverTest {
  private static final Logger LOGGER = LoggerFactory.getLogger(SolverTest.class);

  @Nested
  class WaterTest {
    @Disabled("26.734, 46.074, 26.719, 46.028, 0.450")
    @ParameterizedTest
    @CsvSource(delimiter = ',', textBlock = """
        30.971, 61.860, 31.278, 62.479, -0.05
        16.761, 32.246, 16.821, 32.383, -0.05
        13.338, 23.903, 13.357, 23.953, -0.05
        12.187, 20.567, 12.194, 20.589, -0.05
        11.710, 18.986, 11.714, 18.998, -0.05
        11.482, 18.152, 11.484, 18.158, -0.05
        11.361, 17.674, 11.362, 17.678, -0.05
        """)
    void water(double r1, double r2, double r1After, double r2After, double hDiffMilli) {
      Solver solver = Solver.<TetrapolarMeasurement.TetrapolarDiffMeasurement>of(10.0, Metrics.Length.MILLI, Model.Layer2Relative::new)
          .system1x3(m -> m.ohms(r1).thenOhms(r1After).hDiff(hDiffMilli, Metrics.Length.MILLI))
          .system5x3(m -> m.ohms(r2).thenOhms(r2After).hDiff(hDiffMilli, Metrics.Length.MILLI))
          .build();
      LOGGER.atInfo().log(solver::toString);
    }
  }

  @Nested
  class DiffTest {
    @Disabled
    @ParameterizedTest
    @CsvSource(delimiter = ',', textBlock = """
        135.1687, 203.1126, 135.4509, 203.6958, 0.150
        """)
    void diff(double r1, double r2, double r1After, double r2After, double hDiffMilli) {
      Solver solver = Solver.<TetrapolarMeasurement.TetrapolarDiffMeasurement>of(7.0, Metrics.Length.MILLI, Model.Layer2Relative::new)
          .system1x3(m -> m.ohms(r1).thenOhms(r1After).hDiff(hDiffMilli, Metrics.Length.MILLI))
          .system5x3(m -> m.ohms(r2).thenOhms(r2After).hDiff(hDiffMilli, Metrics.Length.MILLI))
          .build();
      LOGGER.atInfo().log(solver::toString);
    }
  }

  @Nested
  class MaxDiffTest {
    @Disabled
    @ParameterizedTest
    @CsvSource(delimiter = ',', textBlock = """
        135.1687, 203.1126, 135.4509, 203.6958, 0.150
        """)
    void diffMax(double r1, double r2, double r1After, double r2After, double hDiffMilli) {
      Solver solver = Solver.<TetrapolarMeasurement.TetrapolarMaxDiffMeasurement>of(7.0, Metrics.Length.MILLI, Model.Layer2Absolute::new)
          .system1x3(m -> m.ohms(r1).thenOhms(r1After).hDiffMax(hDiffMilli, Metrics.Length.MILLI))
          .system5x3(m -> m.ohms(r2).thenOhms(r2After).hDiffMax(hDiffMilli, Metrics.Length.MILLI))
          .build();
      LOGGER.atInfo().log(solver::toString);
    }
  }
}
