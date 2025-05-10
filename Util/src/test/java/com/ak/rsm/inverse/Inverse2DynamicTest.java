package com.ak.rsm.inverse;

import com.ak.math.ValuePair;
import com.ak.rsm.apparent.Apparent2Rho;
import com.ak.rsm.measurement.DerivativeMeasurement;
import com.ak.rsm.measurement.TetrapolarDerivativeMeasurement;
import com.ak.rsm.medium.Layer2Medium;
import com.ak.rsm.relative.RelativeMediumLayers;
import com.ak.rsm.resistance.DeltaH;
import com.ak.rsm.resistance.Resistance;
import com.ak.rsm.resistance.TetrapolarResistance;
import com.ak.rsm.system.InexactTetrapolarSystem;
import com.ak.rsm.system.Layers;
import com.ak.rsm.system.RelativeTetrapolarSystem;
import com.ak.util.Extension;
import com.ak.util.Metrics;
import com.ak.util.Strings;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.NelderMeadSimplex;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizer;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnegative;
import javax.measure.MetricPrefix;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.*;
import java.util.function.Function;
import java.util.function.ObjDoubleConsumer;
import java.util.function.ToDoubleFunction;
import java.util.stream.*;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static tech.units.indriya.unit.Units.METRE;

class Inverse2DynamicTest {
  private static final Logger LOGGER = LoggerFactory.getLogger(Inverse2DynamicTest.class);

  static Stream<Arguments> relative() {
    double absErrorMilli = 0.001;
    DeltaH dhMilli = DeltaH.H1.apply(0.000001);
    double hmm = 15.0 / 2.0;
    double rho1 = 1.0;
    double k = 0.6;
    double rho2 = rho1 / Layers.getRho1ToRho2(k);
    return Stream.of(
        arguments(
            TetrapolarDerivativeMeasurement.milli(absErrorMilli).dh(dhMilli).system2(10.0).rho1(rho1).rho2(rho2).h(hmm),
            new RelativeMediumLayers(
                ValuePair.Name.K12.of(k, 0.00011),
                ValuePair.Name.H_L.of(0.25, 0.000035)
            )
        ),
        arguments(
            TetrapolarDerivativeMeasurement.milli(-absErrorMilli).dh(dhMilli).withShiftError().system2(10.0).ofOhms(
                Stream.concat(
                    TetrapolarResistance.milli().system2(10.0).rho1(rho1).rho2(rho2).h(hmm).stream(),
                    TetrapolarResistance.milli().system2(10.0).rho1(rho1).rho2(rho2).h(hmm + dhMilli.value()).stream()
                ).mapToDouble(Resistance::ohms).toArray()
            ),
            new RelativeMediumLayers(
                ValuePair.Name.K12.of(k + 0.00011, 0.00011),
                ValuePair.Name.H_L.of(0.25 + 0.000035, 0.000035)
            )
        )
    );
  }

  @ParameterizedTest
  @MethodSource("relative")
  void testInverseRelative(Collection<? extends DerivativeMeasurement> measurements, RelativeMediumLayers expected) {
    var regularizationFunction = Regularization.Interval.ZERO_MAX.of(0.0);
    var medium = Relative.Dynamic.solve(measurements, regularizationFunction);

    assertAll(medium.toString(),
        () -> assertThat(medium.k().value()).isCloseTo(expected.k().value(), byLessThan(expected.k().absError())),
        () -> assertThat(medium.k().absError()).isCloseTo(expected.k().absError(), withinPercentage(10.0)),
        () -> assertThat(medium.hToL().value()).isCloseTo(expected.hToL().value(), byLessThan(expected.hToL().absError())),
        () -> assertThat(medium.hToL().absError()).isCloseTo(expected.hToL().absError(), withinPercentage(10.0))
    );
  }

  static Stream<Arguments> absolute() {
    double absErrorMilli = 0.001;
    DeltaH dHmm = DeltaH.H1.apply(0.21);
    double hmm = 15.0 / 2;
    return Stream.of(
        // system 4 gets fewer errors
        arguments(
            TetrapolarDerivativeMeasurement.milli(absErrorMilli).dh(dHmm).system4(10.0).rho1(1.0).rho2(4.0).h(hmm),
            new ValuePair[] {
                ValuePair.Name.RHO.of(1.6267, 0.00012),
                ValuePair.Name.RHO_1.of(0.99789, 0.000063),
                ValuePair.Name.RHO_2.of(3.992, 0.0011),
                ValuePair.Name.H.of(Metrics.Length.MILLI.to(hmm, METRE), Metrics.Length.MILLI.to(0.00085, METRE))
            }
        ),
        // system 2 gets more errors
        arguments(
            TetrapolarDerivativeMeasurement.milli(absErrorMilli).dh(DeltaH.NULL).system2(10.0)
                .rho(1.4441429093546185, 1.6676102911913226, -3.0215753166196184, -3.49269170918376),
            new ValuePair[] {
                ValuePair.Name.RHO.of(1.5845, 0.00018),
                ValuePair.Name.RHO_1.of(1.0, 0.00010),
                ValuePair.Name.RHO_2.of(4.0, 0.0018),
                ValuePair.Name.H.of(Metrics.Length.MILLI.to(hmm, METRE), Metrics.Length.MILLI.to(0.0011, METRE))
            }
        )
    );
  }

  @ParameterizedTest
  @MethodSource("absolute")
  void testInverseAbsolute(Collection<? extends DerivativeMeasurement> measurements, ValuePair[] expected) {
    var medium = DynamicAbsolute.ofLayer2(measurements, Regularization.Interval.ZERO_MAX.of(0.0));
    assertAll(medium.toString(),
        () -> assertThat(medium.rho()).isEqualTo(expected[0]),
        () -> assertThat(medium.rho1()).isEqualTo(expected[1]),
        () -> assertThat(medium.rho2()).isEqualTo(expected[2]),
        () -> assertThat(medium.h()).isEqualTo(expected[3])
    );
  }

  static Stream<Arguments> theoryParameters() {
    DeltaH dHmm = DeltaH.H1.apply(-0.001);
    double hmm = 5.0;
    double alpha = 0.0;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.ofMilli(0.1).dh(dHmm)
                    .system(10.0, 20.0).rho1(1.0).rho2(9.0).h(hmm)
            ),
            alpha,
            new double[] {
                1.53,
                Apparent2Rho.newApparentDivRho1(new RelativeTetrapolarSystem(0.5))
                    .applyAsDouble(new RelativeMediumLayers(0.8, hmm / 20.0)),
                Apparent2Rho.newApparentDivRho1(new RelativeTetrapolarSystem(0.5))
                    .applyAsDouble(new RelativeMediumLayers(0.8, hmm / 20.0)),
                Double.NaN
            }
        ),
        arguments(
            TetrapolarDerivativeMeasurement.milli(0.1).dh(DeltaH.H1.apply(0.0)).system2(10.0)
                .rho1(1.0).rho2(Double.POSITIVE_INFINITY).h(hmm),
            alpha,
            new double[] {3.3, 1.0, Double.POSITIVE_INFINITY, Metrics.Length.MILLI.to(hmm, METRE)}
        ),
        arguments(
            TetrapolarDerivativeMeasurement.milli(0.1).dh(DeltaH.H1.apply(0.0)).system2(10.0)
                .rho1(10.0).rho2(Double.POSITIVE_INFINITY).h(hmm),
            alpha,
            new double[] {33.0, 1.0, Double.POSITIVE_INFINITY, Metrics.Length.MILLI.to(hmm / 10.0, METRE)}
        ),
        arguments(
            TetrapolarDerivativeMeasurement.milli(0.1).dh(dHmm).system2(10.0).ofOhms(
                Stream.concat(
                    TetrapolarResistance.milli().system2(10.0).rho1(1.0).rho2(Double.POSITIVE_INFINITY).h(hmm).stream(),
                    TetrapolarResistance.milli().system2(10.0).rho1(1.0).rho2(Double.POSITIVE_INFINITY).h(hmm + dHmm.value()).stream()
                ).mapToDouble(Resistance::ohms).toArray()
            ),
            alpha,
            new double[] {3.3, 1.0, Double.POSITIVE_INFINITY, Metrics.Length.MILLI.to(hmm, METRE)}
        ),
        arguments(
            TetrapolarDerivativeMeasurement.milli(0.1).dh(dHmm).system2(10.0).rho1(4.0).rho2(1.0).h(hmm),
            alpha,
            new double[] {1.75, 4.0, 1.0, Metrics.Length.MILLI.to(hmm, METRE)}
        ),
        arguments(
            TetrapolarDerivativeMeasurement.milli(0.1).dh(dHmm).system4(10.0).rho1(1.0).rho2(4.0).h(hmm),
            alpha,
            new double[] {2.02, 1.0, 4.0, Metrics.Length.MILLI.to(hmm, METRE)}
        ),
        arguments(
            TetrapolarDerivativeMeasurement.milli(0.01).dh(dHmm).system2(10.0)
                .ofOhms(100.0, 150.0, 90.0, 160.0),
            alpha,
            new double[] {6.283, Double.NaN, Double.NaN, Double.NaN}
        )
    );
  }

  static Stream<Arguments> waterParameters() {
    DeltaH dHmm = DeltaH.H1.apply(-1.0 / 20.0);
    double alpha = 2.0;
    return Stream.of(
        // h = 5 mm, rho1 = 0.7, rho2 = Inf
        arguments(
            TetrapolarDerivativeMeasurement.milli(0.1).dh(dHmm).system2(10.0)
                .ofOhms(29.47, 65.68, 29.75, 66.35),
            alpha,
            new double[] {2.31, 0.701, Double.POSITIVE_INFINITY, Metrics.Length.MILLI.to(5.06, METRE)}
        ),
        // h = 10 mm, rho1 = 0.7, rho2 = Inf
        arguments(
            TetrapolarDerivativeMeasurement.milli(0.1).dh(dHmm).system2(10.0)
                .ofOhms(16.761, 32.246, 16.821, 32.383),
            alpha,
            new double[] {1.23, 0.701, Double.POSITIVE_INFINITY, Metrics.Length.MILLI.to(10.0, METRE)}
        ),
        // h = 15 mm, rho1 = 0.7, rho2 = Inf
        arguments(
            TetrapolarDerivativeMeasurement.milli(0.1).dh(dHmm).system2(10.0)
                .ofOhms(13.338, 23.903, 13.357, 23.953),
            alpha,
            new double[] {0.94, 0.697, Double.POSITIVE_INFINITY, Metrics.Length.MILLI.to(14.8, METRE)}
        ),
        // h = 20 mm, rho1 = 0.7, rho2 = Inf
        arguments(
            TetrapolarDerivativeMeasurement.milli(0.1).dh(dHmm).system2(10.0)
                .ofOhms(12.187, 20.567, 12.194, 20.589),
            alpha,
            new double[] {0.827, 0.7, Double.POSITIVE_INFINITY, Metrics.Length.MILLI.to(20.0, METRE)}
        ),
        // h = 25 mm, rho1 = 0.7, rho2 = Inf
        arguments(
            TetrapolarDerivativeMeasurement.milli(0.1).dh(dHmm).system2(10.0)
                .ofOhms(11.710, 18.986, 11.714, 18.998),
            alpha,
            new double[] {0.775, 0.694, Double.POSITIVE_INFINITY, Metrics.Length.MILLI.to(23.8, METRE)}
        ),
        // h = 30 mm, rho1 = 0.7, rho2 = Inf
        arguments(
            TetrapolarDerivativeMeasurement.milli(0.1).dh(dHmm).system2(10.0)
                .ofOhms(11.482, 18.152, 11.484, 18.158),
            alpha,
            new double[] {0.747, 0.698, Double.POSITIVE_INFINITY, Metrics.Length.MILLI.to(29.0, METRE)}
        ),
        // h = 35 mm, rho1 = 0.7, rho2 = Inf
        arguments(
            TetrapolarDerivativeMeasurement.milli(0.1).dh(dHmm).system2(10.0)
                .ofOhms(11.361, 17.674, 11.362, 17.678),
            alpha,
            new double[] {0.732, 0.699, Double.POSITIVE_INFINITY, Metrics.Length.MILLI.to(34.0, METRE)}
        )
    );
  }

  @ParameterizedTest
  @MethodSource({"theoryParameters", "waterParameters"})
  void testInverse(Collection<? extends DerivativeMeasurement> measurements, @Nonnegative double alpha, double[] expected) {
    var medium = DynamicAbsolute.ofLayer2(measurements, Regularization.Interval.MAX_K.of(alpha));

    ObjDoubleConsumer<ValuePair> checker = (valuePair, expectedValue) -> {
      if (Double.isNaN(expectedValue)) {
        assertThat(valuePair.value()).isNaN();
      }
      else {
        assertThat(valuePair.value() > 700.0 ? Double.POSITIVE_INFINITY : valuePair.value())
            .isCloseTo(expectedValue, byLessThan(valuePair.absError()));
      }
    };
    assertAll(medium.toString(),
        () -> checker.accept(medium.rho(), expected[0]),
        () -> checker.accept(medium.rho1(), expected[1]),
        () -> checker.accept(medium.rho2(), expected[2]),
        () -> checker.accept(medium.h(), expected[3])
    );
  }

  private static List<Arguments> cvsFiles() throws IOException {
    try (DirectoryStream<Path> p = Files.newDirectoryStream(Paths.get(Strings.EMPTY), Extension.CSV.attachTo("*mm"))) {
      return StreamSupport.stream(p.spliterator(), false)
          .map(Path::toString)
          .flatMap(file -> DoubleStream.of(1.0).mapToObj(alpha -> arguments(file, alpha)))
          .toList();
    }
  }

  private sealed interface Fields {
    String name();

    enum InputFields implements Fields {
      TIME, POSITION, R1_START, R2_START, R1_DIFF, R2_DIFF, DH_MM
    }

    enum OutputFields implements Fields, ToDoubleFunction<Layer2Medium> {
      RHO {
        @Override
        public double applyAsDouble(Layer2Medium m) {
          return m.rho().value();
        }
      },
      RHO_1 {
        @Override
        public double applyAsDouble(Layer2Medium m) {
          return m.rho1().value();
        }
      },
      RHO_2 {
        @Override
        public double applyAsDouble(Layer2Medium m) {
          return m.rho2().value();
        }
      },
      H_MM {
        @Override
        public double applyAsDouble(Layer2Medium m) {
          return Metrics.Length.METRE.to(m.h().value(), MetricPrefix.MILLI(METRE));
        }
      }
    }
  }

  @ParameterizedTest
  @MethodSource("cvsFiles")
  @Disabled("ignored com.ak.rsm.inverse.Inverse2DynamicTest.inverseFileResistivity")
  void inverseFileResistivity(String fileName, @Nonnegative double alpha) {
    double targetRho2 = 4.657;
    Function<Collection<InexactTetrapolarSystem>, Regularization> regularizationFunction = Regularization.Interval.ZERO_MAX_LOG1P.of(alpha);
    LOGGER.atInfo().addKeyValue("target", Strings.rho(2, targetRho2)).log("{}", regularizationFunction);

    String[] mm = fileName.split(Strings.SPACE);
    int sBase = Integer.parseInt(mm[mm.length - 2]);
    Path path = Paths.get(Extension.CSV.attachTo(fileName));
    try (BufferedReader reader = new BufferedReader(new FileReader(path.toFile()))) {
      String header = reader.readLine();
      LOGGER.atInfo().addKeyValue("s, mm", sBase).log(header);
      assertThat(Arrays.stream(header.split("\\|")).filter(s -> !s.isBlank()).map(String::strip).toList())
          .containsExactlyElementsOf(EnumSet.allOf(Fields.InputFields.class).stream().map(Fields.InputFields::name).toList());

      Path outPath = Path.of(
          Extension.CSV.attachTo(
              String.format(Locale.ROOT, "%s inverse - %.1f", Extension.CSV.clean(fileName), alpha)
          )
      );
      boolean exists = Files.isReadable(outPath);
      try (BufferedWriter writer = Files.newBufferedWriter(outPath, Charset.defaultCharset(),
          StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND)) {

        Collector<CharSequence, ?, String> joining = Collectors.joining(" | ", "| ", " |");
        if (!exists) {
          writer.append(
              Stream.concat(
                  EnumSet.allOf(Fields.InputFields.class).stream(),
                  EnumSet.allOf(Fields.OutputFields.class).stream()
              ).map(Enum::name).collect(joining)
          );
          writer.newLine();
          writer.flush();
        }

        for (Iterator<String> iterator = reader.lines().iterator(); iterator.hasNext(); ) {
          String line = iterator.next();
          if (line.isBlank()) {
            break;
          }
          List<String> inputValues = Arrays.stream(line.split("\\|"))
              .filter(s -> !s.isBlank()).map(String::strip).toList();
          double[] values = inputValues.stream().mapToDouble(s -> Double.parseDouble(s.strip())).toArray();
          double r1Before = values[Fields.InputFields.R1_START.ordinal()];
          double r2Before = values[Fields.InputFields.R2_START.ordinal()];
          double r1Diff = values[Fields.InputFields.R1_DIFF.ordinal()];
          double r2Diff = values[Fields.InputFields.R2_DIFF.ordinal()];
          double dHmm = values[Fields.InputFields.DH_MM.ordinal()];

          Layer2Medium layer2Medium;
          if (!Double.isFinite(dHmm)) {
            PointValuePair optimizeDH = new SimplexOptimizer(0.001, 0.001)
                .optimize(
                    new MaxEval(50),
                    new ObjectiveFunction(point -> {
                      double dH = point[0];
                      if (dH > 0.0 && dH < 0.1) {
                        LOGGER.atInfo()
                            .addKeyValue(Fields.InputFields.DH_MM.name(), String.format(Locale.ROOT, "%.3f", dH))
                            .log(Strings.EMPTY);
                        Collection<DerivativeMeasurement> dm = TetrapolarDerivativeMeasurement.milli(0.1)
                            .dh(DeltaH.H1.apply(dH)).system2(sBase)
                            .ofOhms(r1Before, r2Before, r1Before + r1Diff, r2Before + r2Diff);
                        Layer2Medium layer2 = DynamicAbsolute.ofLayer2(dm, regularizationFunction);
                        double rho2 = layer2.rho2().value();
                        double inequality = Math.abs(rho2 - targetRho2);
                        LOGGER.atInfo().addKeyValue("L1 inequality", inequality).log("{}{}", Strings.NEW_LINE, layer2);
                        return inequality;
                      }
                      else {
                        return Double.POSITIVE_INFINITY;
                      }
                    }), GoalType.MINIMIZE,
                    new NelderMeadSimplex(new double[] {0.01}), new InitialGuess(new double[] {0.01})
                );
            dHmm = optimizeDH.getPoint()[0];
            inputValues = new ArrayList<>(inputValues);
            inputValues.set(Fields.InputFields.DH_MM.ordinal(), String.format(Locale.ROOT, "%.4f", dHmm));
          }

          Collection<DerivativeMeasurement> dm = TetrapolarDerivativeMeasurement.milli(0.1)
              .dh(DeltaH.H1.apply(dHmm)).system2(sBase)
              .ofOhms(r1Before, r2Before, r1Before + r1Diff, r2Before + r2Diff);
          layer2Medium = DynamicAbsolute.ofLayer2(dm, regularizationFunction);
          writer.append(
              Stream.concat(
                  inputValues.stream(),
                  Arrays.stream(Fields.OutputFields.values()).mapToDouble(out -> out.applyAsDouble(layer2Medium))
                      .mapToObj(value -> String.format(Locale.ROOT, "%.3f", value))
              ).collect(joining));
          writer.newLine();
          writer.flush();
          LOGGER.atWarn()
              .addKeyValue(Fields.InputFields.TIME.name(), () -> values[Fields.InputFields.TIME.ordinal()])
              .addKeyValue(Fields.InputFields.POSITION.name(), () -> values[Fields.InputFields.POSITION.ordinal()])
              .addKeyValue(Fields.InputFields.DH_MM.name(), inputValues.get(Fields.InputFields.DH_MM.ordinal()))
              .log("{}{}", Strings.NEW_LINE, layer2Medium);
        }
      }
    }
    catch (IOException ex) {
      LOGGER.atWarn().addKeyValue("file", () -> path.toAbsolutePath().toString()).setCause(ex).log();
    }
  }
}
