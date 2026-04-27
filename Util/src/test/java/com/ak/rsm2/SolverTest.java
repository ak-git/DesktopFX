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
      Solver solver = Solver.<TetrapolarMeasurement.Diff>of(10.0, Metrics.Length.MILLI, Model.Layer2Relative::new)
          .system1x3(m -> m.ohms(r1).thenOhms(r1After).hDiff(hDiffMilli, Metrics.Length.MILLI))
          .system5x3(m -> m.ohms(r2).thenOhms(r2After).hDiff(hDiffMilli, Metrics.Length.MILLI))
          .build();
      LOGGER.atInfo().log(solver::toString);
    }
  }

  @Nested
  class DiffTest {
    @Disabled("e8422_2023_05_25_14_04_43")
    @ParameterizedTest
    @CsvSource(delimiter = ',', textBlock = """
        135.1687, 203.1126, 0.2822034, 0.5831683, 0.150
        137.0167, 207.4542, 0.3620525, 0.7709094, 0.150
        139.6433, 212.6400, 0.4942724, 0.9339182, 0.150
        140.7461, 215.4297, 0.4942724, 0.9339182, 0.150
        """)
    void hDiff(double r1, double r2, double r1Diff, double r2Diff, double hDiffMilli) {
      Solver solver = Solver.<TetrapolarMeasurement.Diff>of(7.0, Metrics.Length.MILLI, Model.Layer2Relative::new)
          .system1x3(m -> m.ohms(r1).thenOhms(r1 + r1Diff).hDiff(hDiffMilli, Metrics.Length.MILLI))
          .system5x3(m -> m.ohms(r2).thenOhms(r2 + r2Diff).hDiff(hDiffMilli, Metrics.Length.MILLI))
          .build();
      LOGGER.atInfo().log(solver::toString);
    }
  }

  @Nested
  class MaxDiffTest {
    @Disabled("e8422_2023_05_25_14_04_43")
    @ParameterizedTest
    @CsvSource(delimiter = ',', textBlock = """
        135.1687, 203.1126, 0.2822034, 0.5831683, 0.150
        137.0167, 207.4542, 0.3620525, 0.7709094, 0.150
        139.6433, 212.6400, 0.4942724, 0.9339182, 0.150
        140.7461, 215.4297, 0.4942724, 0.9339182, 0.150
        """)
    void hDiffMaxAbsolute(double r1, double r2, double r1Diff, double r2Diff, double hDiffMax) {
      Solver solver = Solver.<TetrapolarMeasurement.MaxDiffAbsolute>of(7.0, Metrics.Length.MILLI, Model.Layer2Absolute::new)
          .system1x3(m -> m.ohms(r1).thenOhms(r1 + r1Diff).hDiffMaxAbsolute(hDiffMax, Metrics.Length.MILLI))
          .system5x3(m -> m.ohms(r2).thenOhms(r2 + r2Diff).hDiffMaxAbsolute(hDiffMax, Metrics.Length.MILLI))
          .build();
      LOGGER.atInfo().log(solver::toString);
    }

    @Disabled("e8422_2023_05_25_14_04_43")
    @ParameterizedTest
    @CsvSource(delimiter = ',', textBlock = """
        135.1687, 203.1126, 0.2822034, 0.5831683, 0.150
        137.0167, 207.4542, 0.3620525, 0.7709094, 0.150
        139.6433, 212.6400, 0.4942724, 0.9339182, 0.150
        140.7461, 215.4297, 0.4942724, 0.9339182, 0.150
        """)
    void hDiffMaxRelative(double r1, double r2, double r1Diff, double r2Diff, double hDiffMax) {
      Solver solver = Solver.<TetrapolarMeasurement.MaxDiffRelative>of(7.0, Metrics.Length.MILLI, Model.Layer2RelativeDH::new)
          .system1x3(m -> m.ohms(r1).thenOhms(r1 + r1Diff).hDiffMaxRelative(hDiffMax, Metrics.Length.MILLI))
          .system5x3(m -> m.ohms(r2).thenOhms(r2 + r2Diff).hDiffMaxRelative(hDiffMax, Metrics.Length.MILLI))
          .build();
      LOGGER.atInfo().log(solver::toString);
    }
  }
}
