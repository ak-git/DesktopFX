package com.ak.rsm.inverse;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.math.ValuePair;
import com.ak.rsm.apparent.Apparent2Rho;
import com.ak.rsm.measurement.DerivativeMeasurement;
import com.ak.rsm.measurement.TetrapolarDerivativeMeasurement;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.byLessThan;
import static org.assertj.core.api.Assertions.withinPercentage;
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
    var medium = new DynamicRelative(measurements).get();

    assertAll(medium.toString(),
        () -> assertThat(medium.k12()).isCloseTo(expected.k12(), byLessThan(expected.k12AbsError())),
        () -> assertThat(medium.k12AbsError()).isCloseTo(expected.k12AbsError(), withinPercentage(10.0)),
        () -> assertThat(medium.hToL()).isCloseTo(expected.hToL(), byLessThan(expected.hToLAbsError())),
        () -> assertThat(medium.hToLAbsError()).isCloseTo(expected.hToLAbsError(), withinPercentage(10.0)),
        () -> assertThat(medium).isEqualTo(new DynamicAbsolute(measurements).apply(medium))
    );
    LOGGER.info(medium::toString);
  }

  static Stream<Arguments> absoluteDynamicLayer2() {
    double absErrorMilli = 0.001;
    double dhMilli = 0.21;
    double hmm = 15.0 / 2;
    return Stream.of(
        arguments(
            TetrapolarDerivativeMeasurement.milli(absErrorMilli).dh(dhMilli).system2(10.0).rho1(1.0).rho2(4.0).h(hmm),
            new ValuePair[] {
                ValuePair.Name.RHO_1.of(1.0, 0.00011),
                ValuePair.Name.RHO_2.of(4.0, 0.0018),
                ValuePair.Name.H.of(Metrics.fromMilli(hmm), Metrics.fromMilli(0.0011))
            }
        ),
        arguments(
            TetrapolarDerivativeMeasurement.milli(absErrorMilli).dh(Double.NaN).system2(10.0)
                .rho(1.4441429093546185, 1.6676102911913226, -3.0215753166196184, -3.49269170918376),
            new ValuePair[] {
                ValuePair.Name.RHO_1.of(1.0, 0.00011),
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
    var medium = new DynamicAbsolute(measurements).get();
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
            new double[] {
                Apparent2Rho.newNormalizedApparent2Rho(new RelativeTetrapolarSystem(0.5))
                    .applyAsDouble(new Layer2RelativeMedium(0.8, hmm / 20.0)),
                Apparent2Rho.newNormalizedApparent2Rho(new RelativeTetrapolarSystem(0.5))
                    .applyAsDouble(new Layer2RelativeMedium(0.8, hmm / 20.0)),
                Double.NaN}
        ),
        arguments(
            TetrapolarDerivativeMeasurement.milli(0.1).dh(0.0).system2(10.0)
                .rho1(1.0).rho2(Double.POSITIVE_INFINITY).h(hmm),
            new double[] {1.0, Double.POSITIVE_INFINITY, Metrics.fromMilli(hmm)}
        ),
        arguments(
            TetrapolarDerivativeMeasurement.milli(0.1).dh(0.0).system2(10.0)
                .rho1(10.0).rho2(Double.POSITIVE_INFINITY).h(hmm),
            new double[] {1.0, Double.POSITIVE_INFINITY, Metrics.fromMilli(hmm / 10.0)}
        ),
        arguments(
            TetrapolarDerivativeMeasurement.milli(0.1).dh(dhMilli).system2(10.0).ofOhms(
                Stream.concat(
                    TetrapolarResistance.milli().system2(10.0).rho1(1.0).rho2(Double.POSITIVE_INFINITY).h(hmm).stream(),
                    TetrapolarResistance.milli().system2(10.0).rho1(1.0).rho2(Double.POSITIVE_INFINITY).h(hmm + dhMilli).stream()
                ).mapToDouble(Resistance::ohms).toArray()
            ),
            new double[] {1.0, Double.POSITIVE_INFINITY, Metrics.fromMilli(hmm)}
        ),
        arguments(
            TetrapolarDerivativeMeasurement.milli(0.1).dh(dhMilli).system2(10.0).rho1(4.0).rho2(1.0).h(hmm),
            new double[] {4.0, 1.0, Metrics.fromMilli(hmm)}
        ),
        arguments(
            TetrapolarDerivativeMeasurement.milli(0.1).dh(dhMilli).system2(10.0).rho1(1.0).rho2(4.0).h(hmm),
            new double[] {1.0, 4.0, Metrics.fromMilli(hmm)}
        ),
        arguments(
            TetrapolarDerivativeMeasurement.milli(0.01).dh(dhMilli).system2(10.0)
                .ofOhms(100.0, 150.0, 90.0, 160.0),
            new double[] {Double.NaN, Double.NaN, Double.NaN}
        )
    );
  }

  static Stream<Arguments> dynamicParameters2() {
    return Stream.of(
        arguments(
            TetrapolarDerivativeMeasurement.milli(0.1)
                .dh(0.15).system2(7.0)
                .ofOhms(113.341, 167.385, 113.341 + 0.091, 167.385 + 0.273),
            new double[] {5.211, 1.584, Metrics.fromMilli(15.19)}
        ),
        arguments(
            TetrapolarDerivativeMeasurement.milli(0.1).dh(0.12).system2(8.0).ofOhms(93.4, 162.65, 93.5, 162.85),
            new double[] {5.118, 4.235, Metrics.fromMilli(7.82)}
        ),
        arguments(
            TetrapolarDerivativeMeasurement.milli(0.1)
                .dh(0.15).system2(7.0).ofOhms(136.5, 207.05, 136.65, 207.4),
            new double[] {6.332, 4.180, Metrics.fromMilli(10.35)}
        )
    );
  }

  static Stream<Arguments> waterDynamicParameters2() {
    double dh = -10.0 / 200.0;
    return Stream.of(
        // h = 5 mm, rho1 = 0.7, rho2 = Inf
        arguments(
            TetrapolarDerivativeMeasurement.milli(0.1).dh(dh).system2(10.0)
                .ofOhms(29.47, 65.68, 29.75, 66.35),
            new double[] {0.694, Double.POSITIVE_INFINITY, Metrics.fromMilli(5.01)}
        ),
        // h = 10 mm, rho1 = 0.7, rho2 = Inf
        arguments(
            TetrapolarDerivativeMeasurement.milli(0.1).dh(dh).system2(10.0)
                .ofOhms(16.761, 32.246, 16.821, 32.383),
            new double[] {0.7, Double.POSITIVE_INFINITY, Metrics.fromMilli(9.98)}
        ),
        // h = 15 mm, rho1 = 0.7, rho2 = Inf
        arguments(
            TetrapolarDerivativeMeasurement.milli(0.1).dh(dh).system2(10.0)
                .ofOhms(13.338, 23.903, 13.357, 23.953),
            new double[] {0.698, 33.428, Metrics.fromMilli(14.48)}
        ),
        // h = 20 mm, rho1 = 0.7, rho2 = Inf
        arguments(
            TetrapolarDerivativeMeasurement.milli(0.1).dh(dh).system2(10.0)
                .ofOhms(12.187, 20.567, 12.194, 20.589),
            new double[] {0.7, 383.867, Metrics.fromMilli(19.95)}
        ),
        // h = 25 mm, rho1 = 0.7, rho2 = Inf
        arguments(
            TetrapolarDerivativeMeasurement.milli(0.1).dh(dh).system2(10.0)
                .ofOhms(11.710, 18.986, 11.714, 18.998),
            new double[] {0.7, 3.0, Metrics.fromMilli(19.81)}
        ),
        // h = 30 mm, rho1 = 0.7, rho2 = Inf
        arguments(
            TetrapolarDerivativeMeasurement.milli(0.1).dh(dh).system2(10.0)
                .ofOhms(11.482, 18.152, 11.484, 18.158),
            new double[] {0.7, 1.5, Metrics.fromMilli(20.4)}
        ),
        // h = 35 mm, rho1 = 0.7, rho2 = Inf
        arguments(
            TetrapolarDerivativeMeasurement.milli(0.1).dh(dh).system2(10.0)
                .ofOhms(11.361, 17.674, 11.362, 17.678),
            new double[] {0.698, Double.POSITIVE_INFINITY, Metrics.fromMilli(34.06)}
        )
    );
  }

  static Stream<Arguments> allDynamicParameters2() {
    return Stream.concat(theoryDynamicParameters2(), dynamicParameters2());
  }

  @ParameterizedTest
  @MethodSource("allDynamicParameters2")
  @ParametersAreNonnullByDefault
  void testInverseDynamicLayer2(Collection<? extends DerivativeMeasurement> measurements, double[] expected) {
    var medium = new DynamicAbsolute(measurements).get();
    assertAll(medium.toString(),
        () -> assertThat(medium.rho().value()).isCloseTo(expected[0], byLessThan(0.1)),
        () -> assertThat(medium.rho1().value()).isCloseTo(expected[0], byLessThan(0.1)),
        () -> assertThat(medium.rho2().value() > 1000 ? Double.POSITIVE_INFINITY : medium.rho2().value())
            .isCloseTo(expected[1], byLessThan(0.1)),
        () -> assertThat(Metrics.toMilli(medium.h1().value())).isCloseTo(Metrics.toMilli(expected[2]), byLessThan(0.1))
    );
    LOGGER.info(medium::toString);
  }

  static Stream<String> cvsFiles() throws IOException {
    Stream<String> paths = Stream.empty();
    try (DirectoryStream<Path> p = Files.newDirectoryStream(Paths.get(Strings.EMPTY), Extension.CSV.attachTo("*mm"))) {
      paths = Stream.concat(paths, StreamSupport.stream(p.spliterator(), true).map(Path::toString));
    }
    return paths;
  }

  @ParameterizedTest
  @MethodSource("cvsFiles")
  @Disabled("ingored com.ak.rsm.inverse.InverseDynamicTest.testInverseDynamicLayerFileResistivity")
  void testInverseDynamicLayerFileResistivity(@Nonnull String fileName) {
    String T = "TIME";
    String POSITION = "POSITION";
    String RHO_S1 = "A1";
    String RHO_S1_DIFF = "DA1";
    String RHO_S2 = "A2";
    String RHO_S2_DIFF = "DA2";

    String RHO_1 = "rho1";
    String RHO_1_ABS_ERROR = "rho1AbsError";
    String RHO_2 = "rho2";
    String RHO_2_ABS_ERROR = "rho2AbsError";
    String H = "h";
    String H_ABS_ERROR = "hAbsError";
    String RMS_BASE = "RMS_BASE";
    String RMS_DIFF = "RMS_DIFF";

    String[] mm = fileName.split(Strings.SPACE);

    Path path = Paths.get(Extension.CSV.attachTo(fileName));
    String[] HEADERS = {T, POSITION, RHO_1, RHO_1_ABS_ERROR, RHO_2, RHO_2_ABS_ERROR, H, H_ABS_ERROR, RMS_BASE, RMS_DIFF};
    try (CSVParser parser = CSVParser.parse(
        new BufferedReader(new FileReader(path.toFile())),
        CSVFormat.Builder.create().setHeader(T, POSITION, RHO_S1, RHO_S2, RHO_S1_DIFF, RHO_S2_DIFF).build());
         CSVLineFileCollector collector = new CSVLineFileCollector(
             Paths.get(Extension.CSV.attachTo("%s inverse".formatted(Extension.CSV.clean(fileName)))),
             HEADERS
         )
    ) {
      assertTrue(StreamSupport.stream(parser.spliterator(), false)
          .filter(r -> r.getRecordNumber() > 1)
          .<Map<String, Object>>mapMulti((r, consumer) -> {
            var medium = new DynamicAbsolute(TetrapolarDerivativeMeasurement.milli(0.1)
                .dh(Double.NaN).system2(Integer.parseInt(mm[mm.length - 2]))
                .rho(
                    Double.parseDouble(r.get(RHO_S1)), Double.parseDouble(r.get(RHO_S2)),
                    Double.parseDouble(r.get(RHO_S1_DIFF)), Double.parseDouble(r.get(RHO_S2_DIFF))
                )).get();
            LOGGER.info(() -> "%.2f sec; %s mm; %s".formatted(Double.parseDouble(r.get(T)), r.get(POSITION), medium));
            consumer.accept(
                Map.ofEntries(
                    Map.entry(T, r.get(T)),
                    Map.entry(POSITION, r.get(POSITION)),
                    Map.entry(RHO_1, medium.rho1().value()),
                    Map.entry(RHO_1_ABS_ERROR, medium.rho1().absError()),
                    Map.entry(RHO_2, medium.rho2().value()),
                    Map.entry(RHO_2_ABS_ERROR, medium.rho2().absError()),
                    Map.entry(H, Metrics.toMilli(medium.h1().value())),
                    Map.entry(H_ABS_ERROR, Metrics.toMilli(medium.h1().absError())),
                    Map.entry(RMS_BASE, medium.getRMS()[0]),
                    Map.entry(RMS_DIFF, medium.getRMS()[1])
                )
            );
          })
          .map(stringMap -> Arrays.stream(HEADERS).map(stringMap::get).toArray())
          .collect(collector));
    }
    catch (IOException ex) {
      Logger.getLogger(getClass().getName()).log(Level.WARNING, path.toAbsolutePath().toString(), ex);
    }
  }
}
