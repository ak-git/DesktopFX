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
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class InverseDynamicTest {
  private static final Logger LOGGER = Logger.getLogger(InverseDynamicTest.class.getName());

  @DataProvider(name = "relativeDynamicLayer2")
  public static Object[][] relativeDynamicLayer2() {
    double absErrorMilli = 0.001;
    double dhMilli = 0.000001;
    double hmm = 15.0 / 2.0;
    double rho1 = 1.0;
    double k = 0.6;
    double rho2 = rho1 / Layers.getRho1ToRho2(k);
    return new Object[][] {
        {
            TetrapolarDerivativeMeasurement.milli(absErrorMilli).dh(dhMilli).system2(10.0).rho1(rho1).rho2(rho2).h(hmm),
            new Layer2RelativeMedium(
                ValuePair.Name.K12.of(k, 0.00011),
                ValuePair.Name.H_L.of(0.25, 0.000035)
            )
        },
        {
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
        },
    };
  }

  @Test(dataProvider = "relativeDynamicLayer2")
  @ParametersAreNonnullByDefault
  public void testInverseRelativeDynamicLayer2Theory(Collection<? extends DerivativeMeasurement> measurements, RelativeMediumLayers expected) {
    var medium = InverseDynamic.INSTANCE.inverseRelative(measurements);
    Assert.assertEquals(medium.k12(), expected.k12(), expected.k12AbsError(), medium.toString());
    Assert.assertEquals(medium.k12AbsError(), expected.k12AbsError(), expected.k12AbsError() * 0.1, medium.toString());
    Assert.assertEquals(medium.hToL(), expected.hToL(), expected.hToLAbsError(), medium.toString());
    Assert.assertEquals(medium.hToLAbsError(), expected.hToLAbsError(), expected.hToLAbsError() * 0.1, medium.toString());
    LOGGER.info(medium::toString);
  }

  @DataProvider(name = "absoluteDynamicLayer2")
  public static Object[][] absoluteDynamicLayer2() {
    double absErrorMilli = 0.001;
    double dhMilli = 0.000001;
    double hmm = 15.0 / 2;
    return new Object[][] {
        {
            TetrapolarDerivativeMeasurement.milli(absErrorMilli).dh(dhMilli).system2(10.0).rho1(1.0).rho2(4.0).h(hmm),
            new ValuePair[] {
                ValuePair.Name.RHO_1.of(1.0, 0.00011),
                ValuePair.Name.RHO_2.of(4.0, 0.0018),
                ValuePair.Name.H.of(Metrics.fromMilli(hmm), Metrics.fromMilli(0.0011))
            }
        },
    };
  }

  @Test(dataProvider = "absoluteDynamicLayer2")
  @ParametersAreNonnullByDefault
  public void testInverseAbsoluteDynamicLayer2(Collection<? extends DerivativeMeasurement> measurements, ValuePair[] expected) {
    var medium = InverseDynamic.INSTANCE.inverse(measurements);
    Assert.assertEquals(medium.rho1(), expected[0], medium.toString());
    Assert.assertEquals(medium.rho2(), expected[1], medium.toString());
    Assert.assertEquals(medium.h1(), expected[2], medium.toString());
    LOGGER.info(medium::toString);
  }

  @DataProvider(name = "theoryDynamicParameters2")
  public static Object[][] theoryDynamicParameters2() {
    double dhMilli = -0.001;
    double hmm = 5.0;
    return new Object[][] {
        {
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
        },
        {
            TetrapolarDerivativeMeasurement.milli(0.1).dh(0.0).system2(10.0)
                .rho1(1.0).rho2(Double.POSITIVE_INFINITY).h(hmm),
            new double[] {1.0, Double.POSITIVE_INFINITY, Metrics.fromMilli(hmm)}
        },
        {
            TetrapolarDerivativeMeasurement.milli(0.1).dh(0.0).system2(10.0)
                .rho1(10.0).rho2(Double.POSITIVE_INFINITY).h(hmm),
            new double[] {1.0, Double.POSITIVE_INFINITY, Metrics.fromMilli(hmm / 10.0)}
        },
        {
            TetrapolarDerivativeMeasurement.milli(0.1).dh(dhMilli).system2(10.0).ofOhms(
                Stream.concat(
                    TetrapolarResistance.milli().system2(10.0).rho1(1.0).rho2(Double.POSITIVE_INFINITY).h(hmm).stream(),
                    TetrapolarResistance.milli().system2(10.0).rho1(1.0).rho2(Double.POSITIVE_INFINITY).h(hmm + dhMilli).stream()
                ).mapToDouble(Resistance::ohms).toArray()
            ),
            new double[] {1.0, Double.POSITIVE_INFINITY, Metrics.fromMilli(hmm)}
        },
        {
            TetrapolarDerivativeMeasurement.milli(0.1).dh(dhMilli).system2(10.0).rho1(4.0).rho2(1.0).h(hmm),
            new double[] {4.0, 1.0, Metrics.fromMilli(hmm)}
        },
        {
            TetrapolarDerivativeMeasurement.milli(0.1).dh(dhMilli).system2(10.0).rho1(1.0).rho2(4.0).h(hmm),
            new double[] {1.0, 4.0, Metrics.fromMilli(hmm)}
        },
        {
            TetrapolarDerivativeMeasurement.milli(0.01).dh(dhMilli).system2(10.0)
                .ofOhms(100.0, 150.0, 90.0, 160.0),
            new double[] {Double.NaN, Double.NaN, Double.NaN}
        },
    };
  }

  @DataProvider(name = "dynamicParameters2")
  public static Object[][] dynamicParameters2() {
    return new Object[][] {
        {
            TetrapolarDerivativeMeasurement.milli(0.1)
                .dh(0.15).system2(7.0)
                .ofOhms(113.341, 167.385, 113.341 + 0.091, 167.385 + 0.273),
            new double[] {5.211, 1.584, Metrics.fromMilli(15.28)}
        },
        {
            TetrapolarDerivativeMeasurement.milli(0.1).dh(0.12).system2(8.0).ofOhms(93.4, 162.65, 93.5, 162.85),
            new double[] {5.118, 4.235, Metrics.fromMilli(7.89)}
        },
        {
            TetrapolarDerivativeMeasurement.milli(0.1)
                .dh(0.15).system2(7.0).ofOhms(136.5, 207.05, 136.65, 207.4),
            new double[] {6.332, 4.180, Metrics.fromMilli(10.43)}
        },
    };
  }

  @DataProvider(name = "waterDynamicParameters2-E5731")
  public static Object[][] waterDynamicParameters2() {
    double dh = -10.0 / 200.0;
    return new Object[][] {
        // h = 5 mm, rho1 = 0.7, rho2 = Inf
        {
            TetrapolarDerivativeMeasurement.milli(0.1).dh(dh).system2(10.0)
                .ofOhms(29.47, 65.68, 29.75, 66.35),
            new double[] {0.694, Double.POSITIVE_INFINITY, Metrics.fromMilli(5.01)}
        },
        // h = 10 mm, rho1 = 0.7, rho2 = Inf
        {
            TetrapolarDerivativeMeasurement.milli(0.1).dh(dh).system2(10.0)
                .ofOhms(16.761, 32.246, 16.821, 32.383),
            new double[] {0.7, Double.POSITIVE_INFINITY, Metrics.fromMilli(9.98)}
        },
        // h = 15 mm, rho1 = 0.7, rho2 = Inf
        {
            TetrapolarDerivativeMeasurement.milli(0.1).dh(dh).system2(10.0)
                .ofOhms(13.338, 23.903, 13.357, 23.953),
            new double[] {0.698, 33.428, Metrics.fromMilli(14.48)}
        },
        // h = 20 mm, rho1 = 0.7, rho2 = Inf
        {
            TetrapolarDerivativeMeasurement.milli(0.1).dh(dh).system2(10.0)
                .ofOhms(12.187, 20.567, 12.194, 20.589),
            new double[] {0.7, 383.867, Metrics.fromMilli(19.95)}
        },
        // h = 25 mm, rho1 = 0.7, rho2 = Inf
        {
            TetrapolarDerivativeMeasurement.milli(0.1).dh(dh).system2(10.0)
                .ofOhms(11.710, 18.986, 11.714, 18.998),
            new double[] {0.7, 3.0, Metrics.fromMilli(19.81)}
        },
        // h = 30 mm, rho1 = 0.7, rho2 = Inf
        {
            TetrapolarDerivativeMeasurement.milli(0.1).dh(dh).system2(10.0)
                .ofOhms(11.482, 18.152, 11.484, 18.158),
            new double[] {0.7, 1.5, Metrics.fromMilli(20.4)}
        },
        // h = 35 mm, rho1 = 0.7, rho2 = Inf
        {
            TetrapolarDerivativeMeasurement.milli(0.1).dh(dh).system2(10.0)
                .ofOhms(11.361, 17.674, 11.362, 17.678),
            new double[] {0.698, Double.POSITIVE_INFINITY, Metrics.fromMilli(34.06)}
        },
    };
  }

  @DataProvider(name = "allDynamicParameters2")
  public static Object[][] allDynamicParameters2() {
    return Stream.concat(Arrays.stream(theoryDynamicParameters2()), Arrays.stream(dynamicParameters2())).toArray(Object[][]::new);
  }

  @Test(dataProvider = "allDynamicParameters2")
  @ParametersAreNonnullByDefault
  public void testInverseDynamicLayer2(Collection<? extends DerivativeMeasurement> measurements, double[] expected) {
    var medium = InverseDynamic.INSTANCE.inverse(measurements);
    Assert.assertEquals(medium.rho1().getValue(), expected[0], 0.1, medium.toString());
    Assert.assertEquals(medium.rho2().getValue() > 1000 ? Double.POSITIVE_INFINITY : medium.rho2().getValue(), expected[1], 0.1, medium.toString());
    Assert.assertEquals(Metrics.toMilli(medium.h1().getValue()), Metrics.toMilli(expected[2]), 0.01, medium.toString());
    LOGGER.info(medium::toString);
  }

  @DataProvider(name = "cvsFiles")
  public static Object[][] cvsFiles() throws IOException {
    Object[][] paths;
    try (DirectoryStream<Path> p = Files.newDirectoryStream(Paths.get(Strings.EMPTY), Extension.CSV.attachTo("*mm"))) {
      Object[] csv = StreamSupport.stream(p.spliterator(), true).map(Path::toString).toArray();
      paths = new Object[csv.length][1];
      for (int i = 0; i < csv.length; i++) {
        paths[i][0] = csv[i];
      }
    }
    return paths;
  }

  @Test(enabled = false, dataProvider = "cvsFiles")
  public void testInverseDynamicLayerFileResistivity(@Nonnull String fileName) {
    String T = "TIME";
    String POSITION = "POSITION";
    String RHO_S1 = "RHO_S1";
    String RHO_S1_DIFF = "RHO_S1_DIFF";
    String RHO_S2 = "RHO_S2";
    String RHO_S2_DIFF = "RHO_S2_DIFF";

    String RHO_1 = "rho1";
    String RHO_2 = "rho2";
    String H = "h";
    String RMS_BASE = "RMS_BASE";
    String RMS_DIFF = "RMS_DIFF";

    String[] mm = fileName.split(Strings.SPACE);

    Path path = Paths.get(Extension.CSV.attachTo(fileName));
    String[] HEADERS = {T, POSITION, RHO_1, RHO_2, H, RMS_BASE, RMS_DIFF};
    try (CSVParser parser = CSVParser.parse(
        new BufferedReader(new FileReader(path.toFile())),
        CSVFormat.Builder.create().setHeader(T, POSITION, RHO_S1, RHO_S1_DIFF, RHO_S2, RHO_S2_DIFF).build());
         CSVLineFileCollector collector = new CSVLineFileCollector(
             Paths.get(Extension.CSV.attachTo("%s inverse".formatted(Extension.CSV.clean(fileName)))),
             HEADERS
         )
    ) {
      Assert.assertTrue(StreamSupport.stream(parser.spliterator(), false)
          .filter(r -> r.getRecordNumber() > 1)
          .<Map<String, Object>>mapMulti((r, consumer) -> {
            var medium = InverseDynamic.INSTANCE.inverse(TetrapolarDerivativeMeasurement.milli(0.1)
                .dh(Double.NaN).system2(Integer.parseInt(mm[mm.length - 2]))
                .rho(
                    Double.parseDouble(r.get(RHO_S1)), Double.parseDouble(r.get(RHO_S2)),
                    Double.parseDouble(r.get(RHO_S1_DIFF)), Double.parseDouble(r.get(RHO_S2_DIFF))
                ));
            LOGGER.info(() -> "%.2f sec; %s mm; %s".formatted(Double.parseDouble(r.get(T)), r.get(POSITION), medium));
            consumer.accept(
                Map.ofEntries(
                    Map.entry(T, r.get(T)),
                    Map.entry(POSITION, r.get(POSITION)),
                    Map.entry(RHO_1, medium.rho1().getValue()),
                    Map.entry(RHO_2, medium.rho2().getValue()),
                    Map.entry(H, Metrics.toMilli(medium.h1().getValue())),
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
