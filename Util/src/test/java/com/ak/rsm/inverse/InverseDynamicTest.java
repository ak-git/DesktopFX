package com.ak.rsm.inverse;

import com.ak.math.ValuePair;
import com.ak.rsm.apparent.Apparent2Rho;
import com.ak.rsm.measurement.DerivativeMeasurement;
import com.ak.rsm.measurement.TetrapolarDerivativeMeasurement;
import com.ak.rsm.medium.MediumLayers;
import com.ak.rsm.relative.Layer2RelativeMedium;
import com.ak.rsm.relative.RelativeMediumLayers;
import com.ak.rsm.resistance.Resistance;
import com.ak.rsm.resistance.TetrapolarResistance;
import com.ak.rsm.system.Layers;
import com.ak.rsm.system.RelativeTetrapolarSystem;
import com.ak.util.CSVLineFileCollector;
import com.ak.util.Extension;
import com.ak.util.Metrics;
import com.ak.util.Strings;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.ObjDoubleConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class InverseDynamicTest {
  private static final Logger LOGGER = Logger.getLogger(InverseDynamicTest.class.getName());

  static Stream<Arguments> relativeDynamicLayer2() {
    double absErrorMilli = 0.001;
    double dhMilli = 0.000001;
    double hmm = 15.0 / 2.0;
    double rho1 = 1.0;
    double k = 0.6;
    double rho2 = rho1 / Layers.getRho1ToRho2(k);
    return Stream.of(
        arguments(
            TetrapolarDerivativeMeasurement.milli(absErrorMilli).dh(dhMilli).system2(10.0).rho1(rho1).rho2(rho2).h(hmm),
            new Layer2RelativeMedium(
                ValuePair.Name.K12.of(k, 0.00011),
                ValuePair.Name.H_L.of(0.25, 0.000035)
            )
        ),
        arguments(
            TetrapolarDerivativeMeasurement.milli(-absErrorMilli).dh(dhMilli).withShiftError().system2(10.0).ofOhms(
                Stream.concat(
                    TetrapolarResistance.milli().system2(10.0).rho1(rho1).rho2(rho2).h(hmm).stream(),
                    TetrapolarResistance.milli().system2(10.0).rho1(rho1).rho2(rho2).h(hmm + dhMilli).stream()
                ).mapToDouble(Resistance::ohms).toArray()
            ),
            new Layer2RelativeMedium(
                ValuePair.Name.K12.of(k + 0.00011, 0.00011),
                ValuePair.Name.H_L.of(0.25 + 0.000035, 0.000035)
            )
        )
    );
  }

  @ParameterizedTest
  @MethodSource("relativeDynamicLayer2")
  @ParametersAreNonnullByDefault
  void testInverseRelativeDynamicLayer2Theory(Collection<? extends DerivativeMeasurement> measurements, RelativeMediumLayers expected) {
    var regularizationFunction = Regularization.Interval.ZERO_MAX.of(0.0);
    var medium = new DynamicRelative(measurements, regularizationFunction).get();

    assertAll(medium.toString(),
        () -> assertThat(medium.k12()).isCloseTo(expected.k12(), byLessThan(expected.k12AbsError())),
        () -> assertThat(medium.k12AbsError()).isCloseTo(expected.k12AbsError(), withinPercentage(10.0)),
        () -> assertThat(medium.hToL()).isCloseTo(expected.hToL(), byLessThan(expected.hToLAbsError())),
        () -> assertThat(medium.hToLAbsError()).isCloseTo(expected.hToLAbsError(), withinPercentage(10.0)),
        () -> assertThat(medium).isEqualTo(new DynamicAbsolute(measurements, regularizationFunction).apply(medium))
    );
    LOGGER.info(medium::toString);
  }

  static Stream<Arguments> absoluteDynamicLayer2() {
    double absErrorMilli = 0.001;
    double dhMilli = 0.21;
    double hmm = 15.0 / 2;
    return Stream.of(
        // system 4 gets fewer errors
        arguments(
            TetrapolarDerivativeMeasurement.milli(absErrorMilli).dh(dhMilli).system4(10.0).rho1(1.0).rho2(4.0).h(hmm),
            new ValuePair[] {
                ValuePair.Name.RHO_1.of(0.99789, 0.000063),
                ValuePair.Name.RHO_2.of(3.992, 0.0011),
                ValuePair.Name.H.of(Metrics.fromMilli(hmm), Metrics.fromMilli(0.00085))
            }
        ),
        // system 2 gets more errors
        arguments(
            TetrapolarDerivativeMeasurement.milli(absErrorMilli).dh(Double.NaN).system2(10.0)
                .rho(1.4441429093546185, 1.6676102911913226, -3.0215753166196184, -3.49269170918376),
            new ValuePair[] {
                ValuePair.Name.RHO_1.of(1.0, 0.00010),
                ValuePair.Name.RHO_2.of(4.0, 0.0018),
                ValuePair.Name.H.of(Metrics.fromMilli(hmm), Metrics.fromMilli(0.0011))
            }
        )
    );
  }

  @ParameterizedTest
  @MethodSource("absoluteDynamicLayer2")
  @ParametersAreNonnullByDefault
  void testInverseAbsoluteDynamicLayer2(Collection<? extends DerivativeMeasurement> measurements, ValuePair[] expected) {
    var medium = new DynamicAbsolute(measurements, Regularization.Interval.ZERO_MAX.of(0.0)).get();
    assertAll(medium.toString(),
        () -> assertThat(medium.rho()).isEqualTo(expected[0]),
        () -> assertThat(medium.rho1()).isEqualTo(expected[0]),
        () -> assertThat(medium.rho2()).isEqualTo(expected[1]),
        () -> assertThat(medium.h1()).isEqualTo(expected[2])
    );
    LOGGER.info(medium::toString);
  }

  static Stream<Arguments> theoryDynamicParameters2() {
    double dhMilli = -0.001;
    double hmm = 5.0;
    return Stream.of(
        arguments(
            List.of(
                TetrapolarDerivativeMeasurement.ofMilli(0.1).dh(dhMilli)
                    .system(10.0, 20.0).rho1(1.0).rho2(9.0).h(hmm)
            ),
            0.0,
            new double[] {
                Apparent2Rho.newApparentDivRho1(new RelativeTetrapolarSystem(0.5))
                    .applyAsDouble(new Layer2RelativeMedium(0.8, hmm / 20.0)),
                Apparent2Rho.newApparentDivRho1(new RelativeTetrapolarSystem(0.5))
                    .applyAsDouble(new Layer2RelativeMedium(0.8, hmm / 20.0)),
                Double.NaN}
        ),
        arguments(
            TetrapolarDerivativeMeasurement.milli(0.1).dh(0.0).system2(10.0)
                .rho1(1.0).rho2(Double.POSITIVE_INFINITY).h(hmm),
            0.0,
            new double[] {1.0, Double.POSITIVE_INFINITY, Metrics.fromMilli(hmm)}
        ),
        arguments(
            TetrapolarDerivativeMeasurement.milli(0.1).dh(0.0).system2(10.0)
                .rho1(10.0).rho2(Double.POSITIVE_INFINITY).h(hmm),
            0.0,
            new double[] {1.0, Double.POSITIVE_INFINITY, Metrics.fromMilli(hmm / 10.0)}
        ),
        arguments(
            TetrapolarDerivativeMeasurement.milli(0.1).dh(dhMilli).system2(10.0).ofOhms(
                Stream.concat(
                    TetrapolarResistance.milli().system2(10.0).rho1(1.0).rho2(Double.POSITIVE_INFINITY).h(hmm).stream(),
                    TetrapolarResistance.milli().system2(10.0).rho1(1.0).rho2(Double.POSITIVE_INFINITY).h(hmm + dhMilli).stream()
                ).mapToDouble(Resistance::ohms).toArray()
            ),
            0.0,
            new double[] {1.0, Double.POSITIVE_INFINITY, Metrics.fromMilli(hmm)}
        ),
        arguments(
            TetrapolarDerivativeMeasurement.milli(0.1).dh(dhMilli).system2(10.0).rho1(4.0).rho2(1.0).h(hmm),
            0.0,
            new double[] {4.0, 1.0, Metrics.fromMilli(hmm)}
        ),
        arguments(
            TetrapolarDerivativeMeasurement.milli(0.1).dh(dhMilli).system4(10.0).rho1(1.0).rho2(4.0).h(hmm),
            0.0,
            new double[] {1.0, 4.0, Metrics.fromMilli(hmm)}
        ),
        arguments(
            TetrapolarDerivativeMeasurement.milli(0.01).dh(dhMilli).system2(10.0)
                .ofOhms(100.0, 150.0, 90.0, 160.0),
            0.0,
            new double[] {Double.NaN, Double.NaN, Double.NaN}
        )
    );
  }

  static Stream<Arguments> waterDynamicParameters2() {
    double dh = -10.0 / 200.0;
    double alpha = 5.0;
    return Stream.of(
        // h = 5 mm, rho1 = 0.7, rho2 = Inf
        arguments(
            TetrapolarDerivativeMeasurement.milli(0.1).dh(dh).system2(10.0)
                .ofOhms(29.47, 65.68, 29.75, 66.35),
            alpha,
            new double[] {0.701, Double.POSITIVE_INFINITY, Metrics.fromMilli(5.06)}
        ),
        // h = 10 mm, rho1 = 0.7, rho2 = Inf
        arguments(
            TetrapolarDerivativeMeasurement.milli(0.1).dh(dh).system2(10.0)
                .ofOhms(16.761, 32.246, 16.821, 32.383),
            alpha,
            new double[] {0.701, Double.POSITIVE_INFINITY, Metrics.fromMilli(10.0)}
        ),
        // h = 15 mm, rho1 = 0.7, rho2 = Inf
        arguments(
            TetrapolarDerivativeMeasurement.milli(0.1).dh(dh).system2(10.0)
                .ofOhms(13.338, 23.903, 13.357, 23.953),
            alpha,
            new double[] {0.697, Double.POSITIVE_INFINITY, Metrics.fromMilli(14.8)}
        ),
        // h = 20 mm, rho1 = 0.7, rho2 = Inf
        arguments(
            TetrapolarDerivativeMeasurement.milli(0.1).dh(dh).system2(10.0)
                .ofOhms(12.187, 20.567, 12.194, 20.589),
            alpha,
            new double[] {0.7, Double.POSITIVE_INFINITY, Metrics.fromMilli(20.0)}
        ),
        // h = 25 mm, rho1 = 0.7, rho2 = Inf
        arguments(
            TetrapolarDerivativeMeasurement.milli(0.1).dh(dh).system2(10.0)
                .ofOhms(11.710, 18.986, 11.714, 18.998),
            alpha,
            new double[] {0.694, Double.POSITIVE_INFINITY, Metrics.fromMilli(23.8)}
        ),
        // h = 30 mm, rho1 = 0.7, rho2 = Inf
        arguments(
            TetrapolarDerivativeMeasurement.milli(0.1).dh(dh).system2(10.0)
                .ofOhms(11.482, 18.152, 11.484, 18.158),
            alpha,
            new double[] {0.698, Double.POSITIVE_INFINITY, Metrics.fromMilli(29.0)}
        ),
        // h = 35 mm, rho1 = 0.7, rho2 = Inf
        arguments(
            TetrapolarDerivativeMeasurement.milli(0.1).dh(dh).system2(10.0)
                .ofOhms(11.361, 17.674, 11.362, 17.678),
            alpha,
            new double[] {0.699, Double.POSITIVE_INFINITY, Metrics.fromMilli(34.0)}
        )
    );
  }

  @ParameterizedTest
  @MethodSource({"theoryDynamicParameters2", "waterDynamicParameters2"})
  @ParametersAreNonnullByDefault
  void testInverseDynamicLayer2(Collection<? extends DerivativeMeasurement> measurements,
                                @Nonnegative double alpha, double[] expected) {
    var medium = new DynamicAbsolute(measurements, Regularization.Interval.MAX_K.of(alpha)).get();

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
        () -> checker.accept(medium.rho1(), expected[0]),
        () -> checker.accept(medium.rho2(), expected[1]),
        () -> checker.accept(medium.h1(), expected[2])
    );
    LOGGER.info(medium::toString);
  }

  private static List<Arguments> cvsFiles() throws IOException {
    try (DirectoryStream<Path> p = Files.newDirectoryStream(Paths.get(Strings.EMPTY), Extension.CSV.attachTo("*mm"))) {
      return StreamSupport.stream(p.spliterator(), false)
          .map(Path::toString)
          .flatMap(file -> DoubleStream.of(0.2, 0.5, 1.0).mapToObj(alpha -> arguments(file, alpha)))
          .toList();
    }
  }

  @ParameterizedTest
  @MethodSource({"cvsFiles"})
  @Disabled("ignored com.ak.rsm.inverse.InverseDynamicTest.testInverseDynamicLayerFileResistivity")
  void testInverseDynamicLayerFileResistivity(@Nonnull String fileName, @Nonnegative double alpha) {
    String T = "TIME";
    String POSITION = "POSITION";
    String RHO_S1 = "A1";
    String RHO_S1_DIFF = "DA1";
    String RHO_S2 = "A2";
    String RHO_S2_DIFF = "DA2";

    String[] mm = fileName.split(Strings.SPACE);
    int sBase = Integer.parseInt(mm[mm.length - 2]);

    Map<String, Function<MediumLayers, Object>> outputMap = new LinkedHashMap<>();
    outputMap.put("rho1", medium -> medium.rho1().value());
    outputMap.put("rho1AbsError", medium -> medium.rho1().absError());
    outputMap.put("rho2", medium -> medium.rho2().value());
    outputMap.put("rho2AbsError", medium -> medium.rho2().absError());
    outputMap.put("h", medium -> Metrics.toMilli(medium.h1().value()));
    outputMap.put("hAbsError", medium -> Metrics.toMilli(medium.h1().absError()));
    outputMap.put("RMS_BASE", medium -> medium.getRMS()[0]);
    outputMap.put("RMS_DIFF", medium -> medium.getRMS()[1]);

    String PREDICTED_R1 = "PREDICTED_R1";
    String PREDICTED_R2 = "PREDICTED_R2";
    String PREDICTED_DIFF_R1 = "PREDICTED_DIFF_R1";
    String PREDICTED_DIFF_R2 = "PREDICTED_DIFF_R2";
    String CONTRIBUTION_RHO1_1 = "CONTRIBUTION_rho1_1";
    String CONTRIBUTION_RHO1_2 = "CONTRIBUTION_rho1_2";
    String CONTRIBUTION_RHO2_1 = "CONTRIBUTION_rho2_1";
    String CONTRIBUTION_RHO2_2 = "CONTRIBUTION_rho2_2";
    String CONTRIBUTION_H_1 = "CONTRIBUTION_h_1";
    String CONTRIBUTION_H_2 = "CONTRIBUTION_h_2";

    Map<String, Object> predictedMap = new LinkedHashMap<>();
    predictedMap.put(PREDICTED_R1, Double.NaN);
    predictedMap.put(PREDICTED_DIFF_R1, Double.NaN);
    predictedMap.put(CONTRIBUTION_RHO1_1, Double.NaN);
    predictedMap.put(CONTRIBUTION_RHO2_1, Double.NaN);
    predictedMap.put(CONTRIBUTION_H_1, Double.NaN);

    predictedMap.put(PREDICTED_R2, Double.NaN);
    predictedMap.put(PREDICTED_DIFF_R2, Double.NaN);
    predictedMap.put(CONTRIBUTION_RHO1_2, Double.NaN);
    predictedMap.put(CONTRIBUTION_RHO2_2, Double.NaN);
    predictedMap.put(CONTRIBUTION_H_2, Double.NaN);

    Path path = Paths.get(Extension.CSV.attachTo(fileName));
    try (BufferedReader reader = new BufferedReader(new FileReader(path.toFile()))) {
      reader.mark(8192);
      CSVFormat csvFormat = CSVFormat.Builder.create().build();
      Collection<String> inputHeaders = Arrays.stream(reader.readLine().split(csvFormat.getDelimiterString()))
          .map(s -> s.replace(csvFormat.getQuoteCharacter(), ' ').strip()).toList();
      reader.reset();

      try (CSVParser parser = CSVParser.parse(reader,
          CSVFormat.Builder.create().setHeader(inputHeaders.toArray(String[]::new)).setSkipHeaderRecord(true).build());
           CSVLineFileCollector collector = new CSVLineFileCollector(
               Paths.get(Extension.CSV.attachTo(
                   String.format(Locale.ROOT, "%s inverse - %.1f", Extension.CSV.clean(fileName), alpha))),
               Stream.of(inputHeaders.stream(), outputMap.keySet().stream(), predictedMap.keySet().stream())
                   .flatMap(Function.identity()).toArray(String[]::new)
           )
      ) {
        AtomicReference<MediumLayers> prevMediumRC = new AtomicReference<>(null);
        assertTrue(StreamSupport.stream(parser.spliterator(), false)
            .map(r -> {
              LOGGER.info(() -> "%.2f sec; %s mm".formatted(Double.parseDouble(r.get(T)), r.get(POSITION)));
              MediumLayers medium = new DynamicAbsolute(TetrapolarDerivativeMeasurement.milli(0.1)
                  .dh(Double.NaN).system2(sBase)
                  .rho(
                      Double.parseDouble(r.get(RHO_S1)), Double.parseDouble(r.get(RHO_S2)),
                      Double.parseDouble(r.get(RHO_S1_DIFF)), Double.parseDouble(r.get(RHO_S2_DIFF))
                  ), Regularization.Interval.ZERO_MAX.of(alpha)).get();
              LOGGER.info(medium::toString);

              double[] ohms = TetrapolarResistance.milli().system2(sBase)
                  .rho1(medium.rho1().value()).rho2(medium.rho2().value()).h(medium.h1().value())
                  .stream().mapToDouble(Resistance::ohms).toArray();
              predictedMap.keySet().forEach(s -> predictedMap.put(s, Double.NaN));
              predictedMap.put(PREDICTED_R1, ohms[0]);
              predictedMap.put(PREDICTED_R2, ohms[1]);

              MediumLayers prevMedium = prevMediumRC.getAndSet(medium);
              if (prevMedium != null && Double.isFinite(prevMedium.rho().value()) && Double.isFinite(medium.rho().value())) {
                double[] baseOhms = TetrapolarResistance.milli().system2(sBase)
                    .rho1(prevMedium.rho1().value()).rho2(prevMedium.rho2().value()).h(prevMedium.h1().value()).stream()
                    .mapToDouble(Resistance::ohms).toArray();

                double[] dRho1Ohms = TetrapolarResistance.milli().system2(sBase)
                    .rho1(medium.rho1().value()).rho2(prevMedium.rho2().value()).h(prevMedium.h1().value()).stream()
                    .mapToDouble(Resistance::ohms).toArray();
                double[] dRho2Ohms = TetrapolarResistance.milli().system2(sBase)
                    .rho1(prevMedium.rho1().value()).rho2(medium.rho2().value()).h(prevMedium.h1().value()).stream()
                    .mapToDouble(Resistance::ohms).toArray();
                double[] dHOhms = TetrapolarResistance.milli().system2(sBase)
                    .rho1(prevMedium.rho1().value()).rho2(prevMedium.rho2().value()).h(medium.h1().value()).stream()
                    .mapToDouble(Resistance::ohms).toArray();

                predictedMap.put(PREDICTED_DIFF_R1, ohms[0] - baseOhms[0]);
                predictedMap.put(CONTRIBUTION_RHO1_1, dRho1Ohms[0] - baseOhms[0]);
                predictedMap.put(CONTRIBUTION_RHO2_1, dRho2Ohms[0] - baseOhms[0]);
                predictedMap.put(CONTRIBUTION_H_1, dHOhms[0] - baseOhms[0]);

                predictedMap.put(PREDICTED_DIFF_R2, ohms[1] - baseOhms[1]);
                predictedMap.put(CONTRIBUTION_RHO1_2, dRho1Ohms[1] - baseOhms[1]);
                predictedMap.put(CONTRIBUTION_RHO2_2, dRho2Ohms[1] - baseOhms[1]);
                predictedMap.put(CONTRIBUTION_H_2, dHOhms[1] - baseOhms[1]);
              }

              return Stream.of(
                  inputHeaders.stream().map(r::get),
                  outputMap.values().stream().map(f -> f.apply(medium)),
                  predictedMap.values().stream()
              ).flatMap(Function.identity()).toArray();
            })
            .collect(collector));
      }
    }
    catch (IOException ex) {
      Logger.getLogger(getClass().getName()).log(Level.WARNING, path.toAbsolutePath().toString(), ex);
    }
  }
}
