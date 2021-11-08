package com.ak.rsm;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.math.ValuePair;
import com.ak.util.CSVLineFileCollector;
import com.ak.util.Extension;
import com.ak.util.Metrics;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static com.ak.rsm.TetrapolarSystem.milli;
import static com.ak.rsm.TetrapolarSystem.systems2;
import static com.ak.rsm.TetrapolarSystem.systems4;

public class InverseLayer2Test {
  private static final Logger LOGGER = Logger.getLogger(InverseLayer2Test.class.getName());

  @DataProvider(name = "layer2")
  public static Object[][] layer2() {
    double absErrorMilli = 0.001;
    TetrapolarSystem[] systems4 = systems4(absErrorMilli, 10.0);
    double h = Metrics.fromMilli(15.0 / 2.0);
    return new Object[][] {
        {
            TetrapolarMeasurement.of(systems4,
                s -> new Resistance2Layer(s).value(1.0, 4.0, h)),
            new ValuePair[] {
                ValuePair.Name.RHO_1.of(1.0, 0.0022),
                ValuePair.Name.RHO_2.of(4.0, 0.015),
                ValuePair.Name.H.of(h, Metrics.fromMilli(0.050))
            }
        },
        {
            TetrapolarMeasurement.of(systems4,
                s -> new Resistance2Layer(s).value(4.0, 1.0, h)),
            new ValuePair[] {
                ValuePair.Name.RHO_1.of(4.0, 0.0055),
                ValuePair.Name.RHO_2.of(1.0, 0.0021),
                ValuePair.Name.H.of(h, Metrics.fromMilli(0.019))
            }
        },
    };
  }

  @Test(dataProvider = "layer2")
  @ParametersAreNonnullByDefault
  public void testInverseLayer2(Collection<? extends Measurement> measurements, ValuePair[] expected) {
    var medium = InverseStatic.INSTANCE.inverse(measurements);
    Assert.assertEquals(medium.rho1(), expected[0], medium.toString());
    Assert.assertEquals(medium.rho2(), expected[1], medium.toString());
    Assert.assertEquals(medium.h1(), expected[2], medium.toString());
    LOGGER.info(medium::toString);
  }

  @DataProvider(name = "relativeStaticLayer2RiseErrors")
  public static Object[][] relativeStaticLayer2RiseErrors() {
    double absErrorMilli = 0.001;
    TetrapolarSystem[] systems2 = systems2(absErrorMilli, 10.0);
    TetrapolarSystem[] systems2Err = {
        milli(absErrorMilli).s(10.0 + absErrorMilli).l(10.0 * 3.0 - absErrorMilli),
        milli(absErrorMilli).s(10.0 * 5.0 + absErrorMilli).l(10.0 * 3.0 - absErrorMilli)
    };
    double h = Metrics.fromMilli(15.0);
    return new Object[][] {
        {
            TetrapolarMeasurement.of(systems2, s -> new Resistance2Layer(s).value(1.0, Double.POSITIVE_INFINITY, h)),
            new double[] {136.7, 31.0}
        },
        {
            TetrapolarMeasurement.of(
                systems2,
                Arrays.stream(systems2Err).
                    mapToDouble(s -> new Resistance2Layer(s).value(1.0, Double.POSITIVE_INFINITY, h)).toArray()
            ),
            new double[] {138.7, 31.2}
        },
        {
            TetrapolarMeasurement.of(
                systems2Err,
                Arrays.stream(systems2).
                    mapToDouble(s -> new Resistance2Layer(s).value(1.0, Double.POSITIVE_INFINITY, h)).toArray()
            ),
            new double[] {136.7, 31.0}
        },
    };
  }

  @Test(dataProvider = "relativeStaticLayer2RiseErrors")
  @ParametersAreNonnullByDefault
  public void testInverseRelativeStaticLayer2RiseErrors(Collection<? extends Measurement> measurements, double[] riseErrors) {
    double absError = measurements.stream().mapToDouble(m -> m.getSystem().getAbsError()).average().orElseThrow();
    double L = Measurements.getBaseL(measurements);
    double dim = measurements.stream().mapToDouble(m -> Math.max(m.getSystem().getL(), m.getSystem().getS())).max().orElseThrow();

    var medium = InverseStatic.INSTANCE.inverseRelative(measurements);
    Assert.assertEquals(medium.k12AbsError() / (absError / dim), riseErrors[0], 0.1, medium.toString());
    Assert.assertEquals(medium.hToLAbsError() / (absError / L), riseErrors[1], 0.1, medium.toString());
    Assert.assertEquals(medium, InverseStatic.INSTANCE.errors(measurements.stream().map(Measurement::getSystem).toList(), medium));
    LOGGER.info(medium::toString);
  }

  @DataProvider(name = "relativeStaticLayer2")
  public static Object[][] relativeStaticLayer2() {
    double absErrorMilli = 0.001;
    TetrapolarSystem[] systems2 = systems2(absErrorMilli, 10.0);
    TetrapolarSystem[] systems2Err = {
        milli(absErrorMilli).s(10.0 + absErrorMilli).l(10.0 * 3.0 - absErrorMilli),
        milli(absErrorMilli).s(10.0 * 5.0 + absErrorMilli).l(10.0 * 3.0 - absErrorMilli)
    };
    double h = Metrics.fromMilli(15.0 / 2.0);
    double rho1 = 1.0;
    double k = 0.6;
    double rho2 = rho1 / Layers.getRho1ToRho2(k);
    return new Object[][] {
        {
            TetrapolarMeasurement.of(systems2, s -> new Resistance2Layer(s).value(1.0, rho2, h)),
            new Layer2RelativeMedium(
                ValuePair.Name.K12.of(0.6, 0.0010),
                ValuePair.Name.H_L.of(0.25, 0.00039)
            )
        },
        {
            TetrapolarMeasurement.of(systems2,
                Arrays.stream(systems2Err).mapToDouble(s -> new Resistance2Layer(s).value(1.0, rho2, h)).toArray()
            ),
            new Layer2RelativeMedium(
                ValuePair.Name.K12.of(0.599, 0.0010),
                ValuePair.Name.H_L.of(0.2496, 0.00039)
            )
        },
        {
            TetrapolarMeasurement.of(systems2Err,
                Arrays.stream(systems2).mapToDouble(s -> new Resistance2Layer(s).value(1.0, rho2, h)).toArray()
            ),
            new Layer2RelativeMedium(
                ValuePair.Name.K12.of(0.601, 0.0010),
                ValuePair.Name.H_L.of(0.2504, 0.00039)
            )
        },
    };
  }

  @Test(dataProvider = "relativeStaticLayer2")
  @ParametersAreNonnullByDefault
  public void testInverseRelativeStaticLayer2(Collection<? extends Measurement> measurements, RelativeMediumLayers expected) {
    var medium = InverseStatic.INSTANCE.inverseRelative(measurements);
    Assert.assertEquals(medium.k12(), expected.k12(), expected.k12AbsError(), medium.toString());
    Assert.assertEquals(medium.k12AbsError(), expected.k12AbsError(), expected.k12AbsError() * 0.1, medium.toString());
    Assert.assertEquals(medium.hToL(), expected.hToL(), expected.hToLAbsError(), medium.toString());
    Assert.assertEquals(medium.hToLAbsError(), expected.hToLAbsError(), expected.hToLAbsError() * 0.1, medium.toString());
    LOGGER.info(medium::toString);
  }

  @DataProvider(name = "relativeDynamicLayer2")
  public static Object[][] relativeDynamicLayer2() {
    double absErrorMilli = 0.001;
    TetrapolarSystem[] systems2 = systems2(absErrorMilli, 10.0);
    TetrapolarSystem[] systems2Err = {
        milli(absErrorMilli).s(10.0 + absErrorMilli).l(10.0 * 3.0 - absErrorMilli),
        milli(absErrorMilli).s(10.0 * 5.0 + absErrorMilli).l(10.0 * 3.0 - absErrorMilli)
    };
    double dh = Metrics.fromMilli(0.000001);
    double h = Metrics.fromMilli(15.0 / 2);
    double rho1 = 1.0;
    double k = 0.6;
    double rho2 = rho1 / Layers.getRho1ToRho2(k);
    return new Object[][] {
        {
            TetrapolarDerivativeMeasurement.of(
                systems2,
                s -> new Resistance2Layer(s).value(rho1, rho2, h),
                s -> new Resistance2Layer(s).value(rho1, rho2, h + dh),
                dh
            ),
            new Layer2RelativeMedium(
                ValuePair.Name.K12.of(k, 0.00011),
                ValuePair.Name.H_L.of(0.25, 0.000035)
            )
        },
        {
            TetrapolarDerivativeMeasurement.of(
                systems2,
                Arrays.stream(systems2Err).
                    mapToDouble(s -> new Resistance2Layer(s).value(rho1, rho2, h)).toArray(),
                Arrays.stream(systems2Err).
                    mapToDouble(s -> new Resistance2Layer(s).value(rho1, rho2, h + dh)).toArray(),
                dh
            ),
            new Layer2RelativeMedium(
                ValuePair.Name.K12.of(k + 0.00011, 0.00011),
                ValuePair.Name.H_L.of(0.25 + 0.000035, 0.000035)
            )
        },
        {
            TetrapolarDerivativeMeasurement.of(
                systems2Err,
                Arrays.stream(systems2).
                    mapToDouble(s -> new Resistance2Layer(s).value(rho1, rho2, h)).toArray(),
                Arrays.stream(systems2).
                    mapToDouble(s -> new Resistance2Layer(s).value(rho1, rho2, h + dh)).toArray(),
                dh
            ),
            new Layer2RelativeMedium(
                ValuePair.Name.K12.of(k - 0.00011, 0.00011),
                ValuePair.Name.H_L.of(0.25 - 0.000035, 0.000035)
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
    TetrapolarSystem[] systems2 = systems2(absErrorMilli, 10.0);
    double dh = Metrics.fromMilli(0.000001);
    double h = Metrics.fromMilli(15.0 / 2);
    return new Object[][] {
        {
            TetrapolarDerivativeMeasurement.of(
                systems2,
                s -> new Resistance2Layer(s).value(1.0, 4.0, h),
                s -> new Resistance2Layer(s).value(1.0, 4.0, h + dh),
                dh
            ),
            new ValuePair[] {
                ValuePair.Name.RHO_1.of(1.0, 0.00011),
                ValuePair.Name.RHO_2.of(4.0, 0.0018),
                ValuePair.Name.H.of(h, Metrics.fromMilli(0.0011))
            }
        },
        {
            TetrapolarDerivativeMeasurement.of(
                systems2,
                s -> new Resistance2Layer(s).value(4.0, 1.0, h),
                s -> new Resistance2Layer(s).value(4.0, 1.0, h + dh),
                dh
            ),
            new ValuePair[] {
                ValuePair.Name.RHO_1.of(4.0, 0.00097),
                ValuePair.Name.RHO_2.of(1.0, 0.00040),
                ValuePair.Name.H.of(h, Metrics.fromMilli(0.0018))
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
    TetrapolarSystem[] systems1 = {
        milli(0.1).s(10.0).l(20.0)
    };
    TetrapolarSystem[] systems2 = systems2(0.1, 10.0);
    double dh = Metrics.fromMilli(-0.001);
    double h = Metrics.fromMilli(5.0);
    return new Object[][] {
        {
            TetrapolarDerivativeMeasurement.of(
                systems1,
                s -> new Resistance2Layer(s).value(1.0, 9.0, h),
                s -> new Resistance2Layer(s).value(1.0, 9.0, h + dh),
                dh
            ),
            new double[] {
                Apparent2Rho.newNormalizedApparent2Rho(systems1[0].toRelative()).applyAsDouble(new Layer2RelativeMedium(0.8, h / systems1[0].getL())),
                Apparent2Rho.newNormalizedApparent2Rho(systems1[0].toRelative()).applyAsDouble(new Layer2RelativeMedium(0.8, h / systems1[0].getL())),
                Double.NaN}
        },
        {
            TetrapolarDerivativeMeasurement.of(
                systems2,
                s -> new Resistance2Layer(s).value(1.0, Double.POSITIVE_INFINITY, h),
                s -> new Resistance2Layer(s).value(1.0, Double.POSITIVE_INFINITY, h),
                dh
            ),
            new double[] {1.0, Double.POSITIVE_INFINITY, h}
        },
        {
            TetrapolarDerivativeMeasurement.of(
                systems2,
                s -> new Resistance2Layer(s).value(10.0, Double.POSITIVE_INFINITY, h),
                s -> new Resistance2Layer(s).value(10.0, Double.POSITIVE_INFINITY, h),
                dh
            ),
            new double[] {1.0, Double.POSITIVE_INFINITY, h / 10.0}
        },
        {
            TetrapolarDerivativeMeasurement.of(
                systems2,
                s -> new Resistance2Layer(s).value(1.0, Double.POSITIVE_INFINITY, h),
                s -> new Resistance2Layer(s).value(1.0, Double.POSITIVE_INFINITY, h + dh),
                dh
            ),
            new double[] {1.0, Double.POSITIVE_INFINITY, h}
        },
        {
            TetrapolarDerivativeMeasurement.of(
                systems2,
                s -> new Resistance2Layer(s).value(4.0, 1.0, h),
                s -> new Resistance2Layer(s).value(4.0, 1.0, h + dh),
                dh
            ),
            new double[] {4.0, 1.0, h}
        },
        {
            TetrapolarDerivativeMeasurement.of(
                systems2,
                s -> new Resistance2Layer(s).value(1.0, 4.0, h),
                s -> new Resistance2Layer(s).value(1.0, 4.0, h + dh),
                dh
            ),
            new double[] {1.0, 4.0, h}
        },
        {
            TetrapolarDerivativeMeasurement.of(systems2, new double[] {100.0, 150.0}, new double[] {90.0, 160.0}, dh),
            new double[] {Double.NaN, Double.NaN, Double.NaN}
        },
    };
  }

  @DataProvider(name = "dynamicParameters2")
  public static Object[][] dynamicParameters2() {
    return new Object[][] {
        {
            TetrapolarDerivativeMeasurement.of(
                systems2(0.1, 7.0),
                new double[] {113.341, 167.385},
                new double[] {113.341 + 0.091, 167.385 + 0.273},
                Metrics.fromMilli(0.15)
            ),
            new double[] {5.211, 1.584, Metrics.fromMilli(15.28)}
        },
        {
            TetrapolarDerivativeMeasurement.of(
                systems2(0.1, 8.0),
                new double[] {93.4, 162.65},
                new double[] {93.5, 162.85},
                Metrics.fromMilli(0.12)
            ),
            new double[] {5.118, 4.235, Metrics.fromMilli(7.89)}
        },
        {
            TetrapolarDerivativeMeasurement.of(
                systems2(0.1, 7.0),
                new double[] {136.5, 207.05},
                new double[] {136.65, 207.4},
                Metrics.fromMilli(0.15)
            ),
            new double[] {6.332, 4.180, Metrics.fromMilli(10.43)}
        },
    };
  }

  @DataProvider(name = "waterDynamicParameters2-E5731")
  public static Object[][] waterDynamicParameters2() {
    double dh = -Metrics.fromMilli(10.0 / 200.0);
    return new Object[][] {
        // h = 5 mm, rho1 = 0.7, rho2 = Inf
        {
            TetrapolarDerivativeMeasurement.of(
                systems2(0.1, 10.0),
                new double[] {29.47, 65.68},
                new double[] {29.75, 66.35},
                dh
            ),
            new double[] {0.694, 1.0, Metrics.fromMilli(4.96)}
        },
        // h = 10 mm, rho1 = 0.7, rho2 = Inf
        {
            TetrapolarDerivativeMeasurement.of(
                systems2(0.1, 10.0),
                new double[] {16.761, 32.246},
                new double[] {16.821, 32.383},
                dh
            ),
            new double[] {0.699, 1.0, Metrics.fromMilli(9.98)}
        },
        // h = 15 mm, rho1 = 0.7, rho2 = Inf
        {
            TetrapolarDerivativeMeasurement.of(
                systems2(0.1, 10.0),
                new double[] {13.338, 23.903},
                new double[] {13.357, 23.953},
                dh
            ),
            new double[] {0.698, 1.0, Metrics.fromMilli(14.48)}
        },
        // h = 20 mm, rho1 = 0.7, rho2 = Inf
        {
            TetrapolarDerivativeMeasurement.of(
                systems2(0.1, 10.0),
                new double[] {12.187, 20.567},
                new double[] {12.194, 20.589},
                dh
            ),
            new double[] {0.7, 1.0, Metrics.fromMilli(19.95)}
        },
        // h = 25 mm, rho1 = 0.7, rho2 = Inf
        {
            TetrapolarDerivativeMeasurement.of(
                systems2(0.1, 10.0),
                new double[] {11.710, 18.986},
                new double[] {11.714, 18.998},
                dh
            ),
            new double[] {0.7, 1.0, Metrics.fromMilli(25.0)}
        },
        // h = 30 mm, rho1 = 0.7, rho2 = Inf
        {
            TetrapolarDerivativeMeasurement.of(
                systems2(0.1, 10.0),
                new double[] {11.482, 18.152},
                new double[] {11.484, 18.158},
                dh
            ),
            new double[] {0.7, 1.0, Metrics.fromMilli(30.0)}
        },
        // h = 35 mm, rho1 = 0.7, rho2 = Inf
        {
            TetrapolarDerivativeMeasurement.of(
                systems2(0.1, 10.0),
                new double[] {11.361, 17.674},
                new double[] {11.362, 17.678},
                dh
            ),
            new double[] {0.698, 1.0, Metrics.fromMilli(34.06)}
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

  @Test(enabled = false)
  public void testInverseDynamicLayerFile() {
    String T = "TIME";
    String R1_BEFORE = "R1";
    String R1_AFTER = "R1`";
    String R2_BEFORE = "R2";
    String R2_AFTER = "R2`";
    String POSITION = "POSITION";
    String DH = "dH";

    String RHO_1 = "rho1";
    String RHO_2 = "rho2";
    String H = "h";
    String RMS_BASE = "RMS_BASE";
    String RMS_DIFF = "RMS_DIFF";

    TetrapolarSystem[] systems = systems2(0.1, 7.0);

    String fileName = "2021-10-25 17-23-43";
    Path path = Paths.get(Extension.CSV.attachTo(fileName));
    String[] HEADERS = {T, POSITION, DH, RHO_1, RHO_2, H, RMS_BASE, RMS_DIFF};
    try (CSVParser parser = CSVParser.parse(
        new BufferedReader(new FileReader(path.toFile())),
        CSVFormat.Builder.create().setHeader(T, R1_BEFORE, R1_AFTER, R2_BEFORE, R2_AFTER, POSITION, DH).build());
         CSVLineFileCollector collector = new CSVLineFileCollector(
             Paths.get(Extension.CSV.attachTo("%s inverse".formatted(fileName))),
             HEADERS
         )
    ) {
      Assert.assertTrue(StreamSupport.stream(parser.spliterator(), false)
          .filter(r -> r.getRecordNumber() > 1)
          .map(r -> {
            double[] rOhms = {Double.parseDouble(r.get(R1_BEFORE)), Double.parseDouble(r.get(R2_BEFORE))};
            double[] rOhmsAfter = {Double.parseDouble(r.get(R1_AFTER)), Double.parseDouble(r.get(R2_AFTER))};
            double dh = Metrics.fromMilli(Double.parseDouble(r.get(DH)));
            var medium = InverseDynamic.INSTANCE.inverse(TetrapolarDerivativeMeasurement.of(systems, rOhms, rOhmsAfter, dh));
            LOGGER.info(() -> "%.2f sec; %s mm; %s µm; %s".formatted(Double.parseDouble(r.get(T)), r.get(POSITION), r.get(DH), medium));
            return Map.ofEntries(
                Map.entry(T, r.get(T)),
                Map.entry(POSITION, r.get(POSITION)),
                Map.entry(DH, r.get(DH)),
                Map.entry(RHO_1, medium.rho1().getValue()),
                Map.entry(RHO_2, medium.rho2().getValue()),
                Map.entry(H, Metrics.toMilli(medium.h1().getValue())),
                Map.entry(RMS_BASE, medium.getRMS()[0]),
                Map.entry(RMS_DIFF, medium.getRMS()[1])
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