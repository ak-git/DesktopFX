package com.ak.rsm;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.math.ValuePair;
import com.ak.util.CSVLineFileCollector;
import com.ak.util.Extension;
import com.ak.util.Metrics;
import com.ak.util.Strings;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import tec.uom.se.unit.MetricPrefix;
import tec.uom.se.unit.Units;

import static com.ak.rsm.InexactTetrapolarSystem.systems2;
import static com.ak.rsm.InexactTetrapolarSystem.toInexact;
import static com.ak.util.Strings.low;

public class InverseLayer2Test {
  private static final Logger LOGGER = Logger.getLogger(InverseLayer2Test.class.getName());

  @DataProvider(name = "layer2")
  public static Object[][] layer2() {
    InexactTetrapolarSystem[] systems2 = systems2(0.1, 10.0);
    InexactTetrapolarSystem[] systems3 = toInexact(Metrics.fromMilli(0.1), new TetrapolarSystem[] {
        TetrapolarSystem.milli().s(10.0).l(20.0),
        TetrapolarSystem.milli().s(20.0).l(30.0),
        TetrapolarSystem.milli().s(10.0).l(30.0),
    });
    return new Object[][] {
        {
            systems2,
            Arrays.stream(systems2)
                .mapToDouble(s -> new Resistance1Layer(s.toExact()).value(10.0)).toArray(),
            new ValuePair[] {new ValuePair(10.0, 0.11), new ValuePair(10.0, 0.11), new ValuePair(Double.NaN)}
        },
        {
            systems3,
            Arrays.stream(systems3)
                .mapToDouble(s -> new Resistance2Layer(s.toExact()).value(10.0, 1.0, Metrics.fromMilli(10.0))).toArray(),
            new ValuePair[] {
                new ValuePair(10.0, 3.3),
                new ValuePair(1.0, 1.0),
                new ValuePair(Metrics.fromMilli(10.0), Metrics.fromMilli(3.1))
            }
        },
        {
            systems3,
            Arrays.stream(systems3)
                .mapToDouble(s -> new Resistance2Layer(s.toExact()).value(1.0, 10.0, Metrics.fromMilli(10.0))).toArray(),
            new ValuePair[] {
                new ValuePair(1.0, 3.3),
                new ValuePair(10.0, 1.0),
                new ValuePair(Metrics.fromMilli(10.0), Metrics.fromMilli(3.1))
            }
        },
    };
  }

  @Test(dataProvider = "layer2")
  @ParametersAreNonnullByDefault
  public void testInverseLayer2(InexactTetrapolarSystem[] systems, double[] rOhms, ValuePair[] expected) {
    Random random = new SecureRandom();
    MediumLayers<ValuePair> medium = Inverse.inverseStatic(
        TetrapolarMeasurement.of(systems,
            Arrays.stream(rOhms).map(x -> x + random.nextGaussian() / x / 20.0).toArray()
        )
    );
    Assert.assertEquals(medium.rho1().getValue(), expected[0].getValue(), 0.1, medium.toString());
    Assert.assertEquals(medium.rho2().getValue(), expected[1].getValue(), 0.1, medium.toString());
    Assert.assertEquals(medium.h().getValue(), expected[2].getValue(), 0.1, medium.toString());
    LOGGER.info(medium::toString);
  }

  @DataProvider(name = "theoryDynamicParameters2")
  public static Object[][] theoryDynamicParameters2() {
    InexactTetrapolarSystem[] systems1 = {
        InexactTetrapolarSystem.milli(0.1).s(10.0).l(20.0)
    };
    InexactTetrapolarSystem[] systems2 = systems2(0.1, 10.0);
    double dh = Metrics.fromMilli(-0.001);
    double h = Metrics.fromMilli(5.0);
    return new Object[][] {
        {
            systems1,
            Arrays.stream(systems1)
                .mapToDouble(s -> new Resistance2Layer(s.toExact()).value(1.0, 9.0, h)).toArray(),
            Arrays.stream(systems1)
                .mapToDouble(s -> new Resistance2Layer(s.toExact()).value(1.0, 9.0, h + dh)).toArray(),
            dh,
            new double[] {new NormalizedApparent2Rho(systems1[0].toExact().toRelative()).value(0.8, h / systems1[0].toExact().getL()), 0.0, Double.NaN}
        },
        {
            systems2,
            Arrays.stream(systems2)
                .mapToDouble(s -> new Resistance2Layer(s.toExact()).value(1.0, Double.POSITIVE_INFINITY, h)).toArray(),
            Arrays.stream(systems2)
                .mapToDouble(s -> new Resistance2Layer(s.toExact()).value(1.0, Double.POSITIVE_INFINITY, h + dh)).toArray(),
            dh,
            new double[] {1.0, 1.0, h}
        },
        {
            systems2,
            Arrays.stream(systems2)
                .mapToDouble(s -> new Resistance2Layer(s.toExact()).value(10.0, 0.0, h)).toArray(),
            Arrays.stream(systems2)
                .mapToDouble(s -> new Resistance2Layer(s.toExact()).value(10.0, 0.0, h + dh)).toArray(),
            dh,
            new double[] {10.0, -1.0, h}
        },
        {
            systems2,
            new double[] {100.0, 150.0},
            new double[] {90.0, 160.0},
            dh,
            new double[] {Double.NaN, 0.0, Double.NaN}
        },
    };
  }

  @DataProvider(name = "dynamicParameters2")
  public static Object[][] dynamicParameters2() {
    return new Object[][] {
        {
            systems2(0.1, 7.0),
            new double[] {113.341, 167.385},
            new double[] {113.341 + 0.091, 167.385 + 0.273},
            Metrics.fromMilli(0.15),
            new double[] {5.211, -0.534, Metrics.fromMilli(15.28)}
        },
        {
            systems2(0.1, 8.0),
            new double[] {93.4, 162.65},
            new double[] {93.5, 162.85},
            Metrics.fromMilli(0.12),
            new double[] {5.302, -0.094, Metrics.fromMilli(7.89)}
        },
        {
            systems2(0.1, 7.0),
            new double[] {136.5, 207.05},
            new double[] {136.65, 207.4},
            Metrics.fromMilli(0.15),
            new double[] {6.332, -0.205, Metrics.fromMilli(10.43)}
        },
    };
  }

  @DataProvider(name = "waterDynamicParameters2-E5731")
  public static Object[][] waterDynamicParameters2() {
    double dh = -Metrics.fromMilli(10.0 / 200.0);
    return new Object[][] {
        // h = 5 mm, rho1 = 0.7, rho2 = Inf
        {
            systems2(0.1, 10.0),
            new double[] {29.47, 65.68},
            new double[] {29.75, 66.35},
            dh,
            new double[] {0.694, 1.0, Metrics.fromMilli(4.96)}
        },
        // h = 10 mm, rho1 = 0.7, rho2 = Inf
        {
            systems2(0.1, 10.0),
            new double[] {16.761, 32.246},
            new double[] {16.821, 32.383},
            dh,
            new double[] {0.699, 1.0, Metrics.fromMilli(9.98)}
        },
        // h = 15 mm, rho1 = 0.7, rho2 = Inf
        {
            systems2(0.1, 10.0),
            new double[] {13.338, 23.903},
            new double[] {13.357, 23.953},
            dh,
            new double[] {0.698, 1.0, Metrics.fromMilli(14.48)}
        },
        // h = 20 mm, rho1 = 0.7, rho2 = Inf
        {
            systems2(0.1, 10.0),
            new double[] {12.187, 20.567},
            new double[] {12.194, 20.589},
            dh,
            new double[] {0.7, 1.0, Metrics.fromMilli(19.95)}
        },
        // h = 25 mm, rho1 = 0.7, rho2 = Inf
        {
            systems2(0.1, 10.0),
            new double[] {11.710, 18.986},
            new double[] {11.714, 18.998},
            dh,
            new double[] {0.7, 1.0, Metrics.fromMilli(25.0)}
        },
        // h = 30 mm, rho1 = 0.7, rho2 = Inf
        {
            systems2(0.1, 10.0),
            new double[] {11.482, 18.152},
            new double[] {11.484, 18.158},
            dh,
            new double[] {0.7, 1.0, Metrics.fromMilli(30.0)}
        },
        // h = 35 mm, rho1 = 0.7, rho2 = Inf
        {
            systems2(0.1, 10.0),
            new double[] {11.361, 17.674},
            new double[] {11.362, 17.678},
            dh,
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
  public void testInverseDynamicLayer2(InexactTetrapolarSystem[] systems, double[] rOhms, double[] rOhmsAfter, double dh, double[] expected) {
    var medium = Inverse.inverseDynamic(TetrapolarDerivativeMeasurement.of(systems, rOhms, rOhmsAfter, dh));
    Assert.assertEquals(medium.rho1().getValue(), expected[0], 0.1, medium.toString());
    Assert.assertEquals(medium.k12().getValue(), expected[1], 0.1, medium.toString());
    Assert.assertEquals(Metrics.toMilli(medium.h().getValue()), Metrics.toMilli(expected[2]), 0.01, medium.toString());
    LOGGER.info(medium::toString);
  }

  @Test
  public void testInverseDynamicLayerFile() {
    String T = "TIME, s";
    String R1_BEFORE = "R1, Ω";
    String R1_AFTER = "R1`, Ω";
    String R2_BEFORE = "R2, Ω";
    String R2_AFTER = "R2`, Ω";
    String POS = "POSITION, µm";

    String RHO_1 = Strings.rho(1, null);
    String RHO_2 = Strings.rho(2, null);
    String H = "h, %s".formatted(MetricPrefix.MILLI(Units.METRE));
    String L2 = "L" + low(2);

    InexactTetrapolarSystem[] systems = systems2(0.1, 7.0);

    String fileName = "2021-05-12 19-03-11";
    Path path = Paths.get(Extension.CSV.attachTo(fileName));
    String[] HEADERS = {T, POS, RHO_1, RHO_2, H, L2};
    try (CSVParser parser = CSVParser.parse(
        new BufferedReader(new FileReader(path.toFile())),
        CSVFormat.DEFAULT.withHeader(T, R1_BEFORE, R1_AFTER, R2_BEFORE, R2_AFTER, POS));
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
            double dh = Metrics.fromMilli(Double.parseDouble(r.get(POS)) / 1000.0);
            var medium = Inverse.inverseDynamic(TetrapolarDerivativeMeasurement.of(systems, rOhms, rOhmsAfter, dh));
            LOGGER.info(() -> "%.2f sec; %s µm; %s".formatted(Double.parseDouble(r.get(T)), r.get(POS), medium));
            return Map.ofEntries(
                Map.entry(T, r.get(T)),
                Map.entry(POS, r.get(POS)),
                Map.entry(RHO_1, medium.rho1().getValue()),
                Map.entry(RHO_2, medium.rho2().getValue()),
                Map.entry(H, Metrics.toMilli(medium.h().getValue())),
                Map.entry(L2, medium.getInequalityL2())
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