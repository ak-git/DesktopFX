package com.ak.rsm.inverse;

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

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
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
import java.util.function.ObjDoubleConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    return Stream.of(
        // h = 5 mm, rho1 = 0.7, rho2 = Inf
        arguments(
            TetrapolarDerivativeMeasurement.milli(0.1).dh(dh).system2(10.0)
                .ofOhms(29.47, 65.68, 29.75, 66.35),
            0.0,
            new double[] {0.701, Double.POSITIVE_INFINITY, Metrics.fromMilli(5.06)}
        ),
        // h = 10 mm, rho1 = 0.7, rho2 = Inf
        arguments(
            TetrapolarDerivativeMeasurement.milli(0.1).dh(dh).system2(10.0)
                .ofOhms(16.761, 32.246, 16.821, 32.383),
            0.0,
            new double[] {0.701, Double.POSITIVE_INFINITY, Metrics.fromMilli(10.0)}
        ),
        // h = 15 mm, rho1 = 0.7, rho2 = Inf
        arguments(
            TetrapolarDerivativeMeasurement.milli(0.1).dh(dh).system2(10.0)
                .ofOhms(13.338, 23.903, 13.357, 23.953),
            0.5,
            new double[] {0.697, Double.POSITIVE_INFINITY, Metrics.fromMilli(14.8)}
        ),
        // h = 20 mm, rho1 = 0.7, rho2 = Inf
        arguments(
            TetrapolarDerivativeMeasurement.milli(0.1).dh(dh).system2(10.0)
                .ofOhms(12.187, 20.567, 12.194, 20.589),
            0.1,
            new double[] {0.7, Double.POSITIVE_INFINITY, Metrics.fromMilli(20.0)}
        ),
        // h = 25 mm, rho1 = 0.7, rho2 = Inf
        arguments(
            TetrapolarDerivativeMeasurement.milli(0.1).dh(dh).system2(10.0)
                .ofOhms(11.710, 18.986, 11.714, 18.998),
            1.0,
            new double[] {0.694, Double.POSITIVE_INFINITY, Metrics.fromMilli(23.8)}
        ),
        // h = 30 mm, rho1 = 0.7, rho2 = Inf
        arguments(
            TetrapolarDerivativeMeasurement.milli(0.1).dh(dh).system2(10.0)
                .ofOhms(11.482, 18.152, 11.484, 18.158),
            1.0,
            new double[] {0.698, Double.POSITIVE_INFINITY, Metrics.fromMilli(29.0)}
        ),
        // h = 35 mm, rho1 = 0.7, rho2 = Inf
        arguments(
            TetrapolarDerivativeMeasurement.milli(0.1).dh(dh).system2(10.0)
                .ofOhms(11.361, 17.674, 11.362, 17.678),
            0.0,
            new double[] {0.699, Double.POSITIVE_INFINITY, Metrics.fromMilli(34.0)}
        )
    );
  }

  @ParameterizedTest
  @MethodSource({"theoryDynamicParameters2", "waterDynamicParameters2"})
  @ParametersAreNonnullByDefault
  void testInverseDynamicLayer2(Collection<? extends DerivativeMeasurement> measurements, double alpha, double[] expected) {
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

  private static List<String> cvsFiles() throws IOException {
    try (DirectoryStream<Path> p = Files.newDirectoryStream(Paths.get(Strings.EMPTY), Extension.CSV.attachTo("*mm"))) {
      return StreamSupport.stream(p.spliterator(), true).map(Path::toString).toList();
    }
  }

  @ParameterizedTest
  @MethodSource("cvsFiles")
  @Disabled("ignored com.ak.rsm.inverse.InverseDynamicTest.testInverseDynamicLayerFileResistivity")
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
          .filter(r -> (r.getRecordNumber() - 2) % 3 == 0)
          .<Map<String, Object>>mapMulti((r, consumer) -> {
            LOGGER.info(() -> "%.2f sec; %s mm".formatted(Double.parseDouble(r.get(T)), r.get(POSITION)));
            var medium = new DynamicAbsolute(TetrapolarDerivativeMeasurement.milli(0.1)
                .dh(Double.NaN).system2(Integer.parseInt(mm[mm.length - 2]))
                .rho(
                    Double.parseDouble(r.get(RHO_S1)), Double.parseDouble(r.get(RHO_S2)),
                    Double.parseDouble(r.get(RHO_S1_DIFF)), Double.parseDouble(r.get(RHO_S2_DIFF))
                ), Regularization.Interval.ZERO_MAX.of(0.0)).get();
            LOGGER.info(medium::toString);
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
