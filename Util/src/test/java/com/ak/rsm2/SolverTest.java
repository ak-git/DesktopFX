package com.ak.rsm2;

import com.ak.math.ValuePair;
import com.ak.util.Metrics;
import com.ak.util.Numbers;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

class SolverTest {
  private static final Logger LOGGER = LoggerFactory.getLogger(SolverTest.class);

  @Nested
  class WaterTest {
    @Disabled("""
        -data Error Norm Base=0,0238 alpha = 0 data Error Norm Shift=0,0006 total data Error Norm=0,0244-
        -alpha=0,0013- k₁₂ = 0,945; h = 04,521 mm
        -alpha=0,0013- k₁₂ = 0,934; h = 09,498 mm
        -alpha=0,0013- k₁₂ = 0,881; h = 13,875 mm
        -alpha=0,0008- k₁₂ = 0,907; h = 19,134 mm
        -alpha=0,0023- k₁₂ = 0,551; h = 18,888 mm
        -alpha=0,0023- k₁₂ = 0,319; h = 19,451 mm
        -alpha=0,0007- k₁₂ = 0,885; h = 32,435 mm
        """)
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
      Solver.Solution solution = Solver.<TetrapolarMeasurement.Diff>of(10.0, Metrics.Length.MILLI)
          .system1x3(m -> m.ohms(r1).thenOhms(r1After).hDiff(hDiffMilli, Metrics.Length.MILLI))
          .system5x3(m -> m.ohms(r2).thenOhms(r2After).hDiff(hDiffMilli, Metrics.Length.MILLI))
          .origin(systems -> {
            double hMax = systems.mapToDouble(inexact -> inexact.hMax(K.PLUS_ONE)).min().orElseThrow();
            return new Model.Layer2Relative(K.PLUS_ONE, hMax);
          })
          .build().solve();
      LOGGER.atWarn().log(solution::toString);
      Assertions.assertThat(solution).isNotNull();
    }

    @Disabled("""
        -data Error Norm Base=0,0238 alpha = 0 data Error Norm Shift=0,0031 total data Error Norm=0,0269-
        -alpha=0,0015- k₁₂ = 0,942; h = 04,328 mm; Δh = -0,048 mm
        -alpha=0,0013- k₁₂ = 0,934; h = 09,498 mm; Δh = -0,050 mm
        -alpha=0,0013- k₁₂ = 0,881; h = 13,875 mm; Δh = -0,050 mm
        -alpha=0,0011- k₁₂ = 0,884; h = 18,925 mm; Δh = -0,050 mm
        -alpha=0,0021- k₁₂ = 0,600; h = 18,907 mm; Δh = -0,046 mm
        -alpha=0,0023- k₁₂ = 0,466; h = 19,539 mm; Δh = -0,034 mm
        -alpha=0,0002- k₁₂ = 0,957; h = 31,734 mm; Δh = -0,042 mm
        """)
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
    void waterMax(double r1, double r2, double r1After, double r2After, double hDiffMilli) {
      Solver.Solution solution = Solver.<TetrapolarMeasurement.MaxDiff>of(10.0, Metrics.Length.MILLI)
          .system1x3(m -> m.ohms(r1).thenOhms(r1After).hDiffMax(hDiffMilli, Metrics.Length.MILLI))
          .system5x3(m -> m.ohms(r2).thenOhms(r2After).hDiffMax(hDiffMilli, Metrics.Length.MILLI))
          .origin(systems -> {
            double hMax = systems.mapToDouble(inexact -> inexact.hMax(K.PLUS_ONE)).min().orElseThrow();
            return new Model.Layer2RelativeDh(new Model.Layer2Relative(K.PLUS_ONE, hMax), Metrics.Length.MILLI.toSI(hDiffMilli));
          })
          .build().solve();
      LOGGER.atWarn().log(solution::toString);
      Assertions.assertThat(solution).isNotNull();
    }
  }

  @Nested
  class DiffTest {
    @Disabled("""
        e8422_2023_05_25_14_04_43
        -data Error Norm Base=0,0339 alpha = 0 data Error Norm Shift=0,0000 total data Error Norm=0,0339-
        -alpha=0,0050- k₁₂ = -0,211; h = 8,353 mm
        -alpha=0,0065- k₁₂ = -0,284; h = 8,546 mm
        -alpha=0,0070- k₁₂ = -0,238; h = 7,123 mm
        -alpha=0,0070- k₁₂ = -0,231; h = 7,064 mm
        """)
    @ParameterizedTest
    @CsvSource(delimiter = ',', textBlock = """
        135.1687, 203.1126, 0.2822034, 0.5831683, 0.150
        137.0167, 207.4542, 0.3620525, 0.7709094, 0.150
        139.6433, 212.6400, 0.4942724, 0.9339182, 0.150
        140.7461, 215.4297, 0.4942724, 0.9339182, 0.150
        """)
    void hDiff(double r1, double r2, double r1Diff, double r2Diff, double hDiffMilli) {
      Solver.Solution solution = Solver.<TetrapolarMeasurement.Diff>of(7.0, Metrics.Length.MILLI)
          .system1x3(m -> m.ohms(r1).thenOhms(r1 + r1Diff).hDiff(hDiffMilli, Metrics.Length.MILLI))
          .system5x3(m -> m.ohms(r2).thenOhms(r2 + r2Diff).hDiff(hDiffMilli, Metrics.Length.MILLI))
          .origin(systems -> {
            double hMax = systems.mapToDouble(inexact -> inexact.hMax(K.PLUS_ONE)).min().orElseThrow();
            return new Model.Layer2Relative(K.MINUS_ONE, hMax);
          })
          .build().solve();
      LOGGER.atWarn().log(solution::toString);
      Assertions.assertThat(solution).isNotNull();
    }

    @Disabled("""
        e8422_2023_05_25_14_04_43
        -data Error Norm Base=0,0339 alpha = 0 data Error Norm Shift=0,0000 total data Error Norm=0,0339-
        -alpha=0,0058- k₁₂ = -0,247; h = 8,326 mm; Δh = 0,127 mm
        -alpha=0,0072- k₁₂ = -0,323; h = 8,514 mm; Δh = 0,130 mm
        -alpha=0,0075- k₁₂ = -0,253; h = 7,101 mm; Δh = 0,140 mm
        -alpha=0,0072- k₁₂ = -0,248; h = 7,061 mm; Δh = 0,140 mm
        """)
    @ParameterizedTest
    @CsvSource(delimiter = ',', textBlock = """
        135.1687, 203.1126, 0.2822034, 0.5831683, 0.150
        137.0167, 207.4542, 0.3620525, 0.7709094, 0.150
        139.6433, 212.6400, 0.4942724, 0.9339182, 0.150
        140.7461, 215.4297, 0.4942724, 0.9339182, 0.150
        """)
    void hDiffMax(double r1, double r2, double r1Diff, double r2Diff, double hDiffMilli) {
      Solver.Solution solution = Solver.<TetrapolarMeasurement.MaxDiff>of(7.0, Metrics.Length.MILLI)
          .system1x3(m -> m.ohms(r1).thenOhms(r1 + r1Diff).hDiffMax(hDiffMilli, Metrics.Length.MILLI))
          .system5x3(m -> m.ohms(r2).thenOhms(r2 + r2Diff).hDiffMax(hDiffMilli, Metrics.Length.MILLI))
          .origin(systems -> {
            double hMax = systems.mapToDouble(inexact -> inexact.hMax(K.PLUS_ONE)).min().orElseThrow();
            return new Model.Layer2RelativeDh(new Model.Layer2Relative(K.MINUS_ONE, hMax), Metrics.Length.MILLI.toSI(hDiffMilli));
          })
          .build().solve();
      LOGGER.atWarn().log(solution::toString);
      Assertions.assertThat(solution).isNotNull();
    }
  }

  @Nested
  class TwoMaxDiffTest {
    @Disabled("""
        Оценка предела h
        -alpha=0,0083- k₁₂ = -0,417; h = 9,154 mm
        """)
    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
        124.861 | 184.182 | 0.2400 | 0.5270 | 0.090
        """)
    void hDiff(double r1, double r2, double r1Diff, double r2Diff, double hDiffMilli) {
      Solver.Solution solution = Solver.<TetrapolarMeasurement.Diff>of(7.0, Metrics.Length.MILLI)
          .system1x3(m -> m.ohms(r1).thenOhms(r1 + r1Diff).hDiff(hDiffMilli, Metrics.Length.MILLI))
          .system5x3(m -> m.ohms(r2).thenOhms(r2 + r2Diff).hDiff(hDiffMilli, Metrics.Length.MILLI))
          .origin(systems -> {
            double hMax = systems.mapToDouble(inexact -> inexact.hMax(K.PLUS_ONE)).min().orElseThrow();
            return new Model.Layer2Relative(K.MINUS_ONE, hMax);
          })
          .build().solve();
      LOGGER.atWarn().log(solution::toString);
      Assertions.assertThat(solution).isNotNull();
    }

    @Disabled("""
        Оценка перемещения индентора по двухслойной модели
        -alpha=0,0078- k₁₂ = -0,538; h = 10,047 mm; Δh = 0,051 mm 2025-04-23 E-9712 ak 6 мм
        -alpha=0,0085- k₁₂ = -0,417; h = 7,781 mm; Δh = 0,075 mm
        """)
    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
        129.195 | 200.848 | 0.0985 | 0.2742 | 0.090
        124.861 | 184.182 | 0.2400 | 0.5270 | 0.090
        """)
    void hDiffMaxLayer2(double r1, double r2, double r1Diff, double r2Diff, double hDiffMilli) {
      Solver.Solution solution = Solver.<TetrapolarMeasurement.MaxDiff>of(6.0, Metrics.Length.MILLI)
          .system1x3(m -> m.ohms(r1).thenOhms(r1 + r1Diff).hDiffMax(hDiffMilli, Metrics.Length.MILLI))
          .system5x3(m -> m.ohms(r2).thenOhms(r2 + r2Diff).hDiffMax(hDiffMilli, Metrics.Length.MILLI))
          .origin(systems -> {
            double hMax = systems.mapToDouble(inexact -> inexact.hMax(K.PLUS_ONE)).min().orElseThrow();
            return new Model.Layer2RelativeDh(new Model.Layer2Relative(K.MINUS_ONE, hMax), Metrics.Length.MILLI.toSI(hDiffMilli));
          })
          .build().solve();
      LOGGER.atWarn().log(solution::toString);
      Assertions.assertThat(solution).isNotNull();
    }

    @Disabled("""
        Оценка толщины жира по двухслойной модели
        -alpha=0,0015- k₁₂ = -0,999; h = 13,543 mm; Δh = 0,137 mm 2025-04-23 E-9712 ak 6 мм
        -alpha=0,0020- k₁₂ = -0,999; h = 11,082 mm; Δh = 0,071 mm 2025-04-23 E-9712 ak 6 мм
        -alpha=0,4470- k₁₂ = -0,788; h = 3,878 mm; Δh = 0,006 mm
        """)
    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
        129.040 | 200.188 | 0.155 | 0.660 | 0.180
        124.634 | 183.863 | 0.227 | 0.319 | 0.180
        """)
    void hDiffFat(double r1, double r2, double r1Diff, double r2Diff, double hDiffMilli) {
      Solver.Solution solution = Solver.<TetrapolarMeasurement.ZeroDiff>of(6.0, Metrics.Length.MILLI)
          .system1x3(m -> m.ohms(r1).thenOhms(r1 + r1Diff).hDiffZero(hDiffMilli, Metrics.Length.MILLI))
          .system5x3(m -> m.ohms(r2).thenOhms(r2 + r2Diff).hDiffZero(hDiffMilli, Metrics.Length.MILLI))
          .origin(systems -> {
            double hMax = systems.mapToDouble(inexact -> inexact.hMax(K.PLUS_ONE)).min().orElseThrow();
            return new Model.Layer2RelativeDh(new Model.Layer2Relative(K.MINUS_ONE, hMax), Metrics.Length.MILLI.toSI(hDiffMilli));
          })
          .build().solve();
      LOGGER.atWarn().log(solution::toString);
      Assertions.assertThat(solution).isNotNull();
    }

    @Disabled("""
        2 - 8 - 4 / 1.5, 2.0, -30/90 + 60/120
        111.909, 169.731, 0.217, 0.075, -0.499, -0.234, -0.090, 0.180
        
        129.008, 199.456, 0.0948, 0.2517, -0.1881, -0.5382, 0.09, -0.18
        """)
    @ParameterizedTest
    @CsvSource(delimiter = ',', textBlock = """
        111.909, 169.731, 0.217, 0.075, -0.499, -0.234, -0.090, 0.180
        """)
    void hDiffMaxLayer3(double r1, double r2, double r1SmallDiff, double r2SmallDiff,
                        double r1BigDiff, double r2BigDiff, double hSmallDiffMax, double hBigDiffMax) {
      Collection<Solver.Solution> solution = Solver.<TetrapolarMeasurement.TwoMaxDiff>of(6.0, Metrics.Length.MILLI)
          .system1x3(m -> m.ohms(r1).thenOhms(r1 + r1SmallDiff).hDiffMax(hSmallDiffMax, Metrics.Length.MILLI)
              .add(m2 -> m2.ohms(r1).thenOhms(r1 + r1BigDiff).hDiffMax(hBigDiffMax, Metrics.Length.MILLI))
          )
          .system5x3(m -> m.ohms(r2).thenOhms(r2 + r2SmallDiff).hDiffMax(hSmallDiffMax, Metrics.Length.MILLI)
              .add(m2 -> m2.ohms(r2).thenOhms(r2 + r2BigDiff).hDiffMax(hBigDiffMax, Metrics.Length.MILLI))
          )
          .origin(inexact -> {
                List<ElectrodeSystem.Inexact> systems = inexact.toList();
                double apparent1 = Resistivity.of(systems.getFirst()).apparent(r1);
                double apparent2 = Resistivity.of(systems.getLast()).apparent(r2);
                LOGGER.atDebug().log(() -> DoubleStream.of(apparent1, apparent2)
                    .mapToObj(x -> ValuePair.Name.RHO.of(x, 0.0).toString()).collect(Collectors.joining("; "))
                );
            double hStep = Metrics.Length.MILLI.toSI(0.01);
            return new Model.Layer3RelativeDh(
                new Model.Layer3Relative(K.PLUS_ONE, K.MINUS_ONE, hStep, new Model.P(200, 250)),
                new Model.P(3, 6)
                );
              }
          )
          .build().solve(1.0, 0.1, 0.01);
      Assertions.assertThat(solution).hasSize(3).doesNotContainNull().doesNotHaveDuplicates();
    }
  }

  @Nested
  class FourDiffTest {
    @Disabled("""
        2022-05-13 E-7672 ak 6 мм
        148.390, 222.283, 213.328, 243.550,  0.108, 0.582, 0.358, 1.014, 0.210
        146.571, 216.301, 208.746, 236.736, -0.043, 0.139, 0.046, 0.074, 0.210
        """)
    @ParameterizedTest
    @CsvSource(delimiter = ',', textBlock = """
        148.390, 222.283, 213.328, 243.550,  0.108, 0.582, 0.358, 1.014, 0.210
        146.571, 216.301, 208.746, 236.736, -0.043, 0.139, 0.046, 0.074, 0.210
        """)
    void hDiffMaxLayer3(double r1, double r2, double r3, double r4,
                        double dr1, double dr2, double dr3, double dr4, double hDiffMax) {
      Collection<Solver.Solution> solution = Solver.<TetrapolarMeasurement.MaxDiff>of(6.0, Metrics.Length.MILLI)
          .system1x3(m -> m.ohms(r1).thenOhms(r1 + dr1).hDiffMax(hDiffMax, Metrics.Length.MILLI))
          .system5x3(m -> m.ohms(r2).thenOhms(r2 + dr2).hDiffMax(hDiffMax, Metrics.Length.MILLI))
          .system2x4(m -> m.ohms(r3).thenOhms(r3 + dr3).hDiffMax(hDiffMax, Metrics.Length.MILLI))
          .system6x4(m -> m.ohms(r4).thenOhms(r4 + dr4).hDiffMax(hDiffMax, Metrics.Length.MILLI))
          .origin(_ -> {
            double hStep = Metrics.Length.MILLI.toSI(0.01);
            int dpMax = Numbers.toInt(Metrics.Length.MILLI.toSI(hDiffMax) / hStep);
            return new Model.Layer3RelativeDh(
                new Model.Layer3Relative(K.PLUS_ONE, K.MINUS_ONE, hStep,
                    new Model.P(
                        Metrics.Length.MILLI.toSI(2.0) / hStep,
                        Metrics.Length.MILLI.toSI(3.0) / hStep
                    )
                ),
                new Model.P(dpMax, dpMax)
            );
              }
          )
          .build().solve(10.0, 0.1, 0.001);
      Assertions.assertThat(solution).hasSize(3).doesNotContainNull().doesNotHaveDuplicates();
    }
  }
}
