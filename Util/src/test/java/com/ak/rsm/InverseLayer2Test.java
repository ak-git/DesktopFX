package com.ak.rsm;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.math.ValuePair;
import com.ak.util.CSVLineFileCollector;
import com.ak.util.Metrics;
import com.ak.util.Strings;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import tec.uom.se.unit.MetricPrefix;
import tec.uom.se.unit.Units;

import static com.ak.rsm.InexactTetrapolarSystem.systems2;
import static com.ak.rsm.InexactTetrapolarSystem.toInexact;
import static com.ak.util.Strings.PLUS_MINUS;
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
    };
  }

  @Test(dataProvider = "layer2")
  @ParametersAreNonnullByDefault
  public void testInverseLayer2(InexactTetrapolarSystem[] systems, double[] rOhms, ValuePair[] expected) {
    Random random = new SecureRandom();
    MediumLayers<ValuePair> medium = Inverse.inverseStatic(
        TetrapolarMeasurement.of(systems,
            Arrays.stream(rOhms).map(x -> x + random.nextGaussian() / x / 10.0).toArray()
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

  @Test(enabled = false)
  public void testInverseDynamicLayerFile() {
    String T = "TIME, s";
    String R1 = "R1, mΩ";
    String R2 = "R2, mΩ";
    String POS = "POSITION, µm";

    String RHO_1 = Strings.rho(null, 1);
    String RHO_2 = Strings.rho(null, 2);
    String H = "h, %s".formatted(MetricPrefix.MILLI(Units.METRE));

    InexactTetrapolarSystem[] systems = systems2(0.1, 7.0);

    Path path = Paths.get("2021-04-12 20-08-08.csv");
    try (CSVParser parser = CSVParser.parse(new BufferedReader(new FileReader(path.toFile())),
        CSVFormat.DEFAULT.withHeader(T, R1, R2, "CCR, Ω", POS))) {
      List<CSVRecord> records = parser.getRecords();
      CSVLineFileCollector collector = new CSVLineFileCollector(Paths.get("inverse " + path), T, POS,
          RHO_1, PLUS_MINUS + RHO_1,
          RHO_2, PLUS_MINUS + RHO_2,
          "k", PLUS_MINUS + "k",
          H, PLUS_MINUS + H,
          "L" + low(2)
      );
      for (int i = 1; i < records.size() - 1; i++) {
        CSVRecord record1 = records.get(i);
        CSVRecord record2 = records.get(i + 1);
        double[] rOhms = {Double.parseDouble(record1.get(R1)) / 1000.0, Double.parseDouble(record1.get(R2)) / 1000.0};
        double[] rOhmsAfter = {Double.parseDouble(record2.get(R1)) / 1000.0, Double.parseDouble(record2.get(R2)) / 1000.0};
        double dh = Metrics.fromMilli((Double.parseDouble(record2.get(POS)) - Double.parseDouble(record1.get(POS))) / 1000.0);
        var medium = Inverse.inverseDynamic(TetrapolarDerivativeMeasurement.of(systems, rOhms, rOhmsAfter, dh));
        LOGGER.info(() -> "%.2f sec; %s µm; %s".formatted(Double.parseDouble(record1.get(T)), record1.get(POS), medium));
        if (medium.getL2() < 1.0) {
          collector.accept(new Object[] {record1.get(T), record1.get(POS),
              medium.rho1().getValue(), medium.rho1().getAbsError(),
              medium.rho2().getValue(), medium.rho2().getAbsError(),
              medium.k12().getValue(), medium.k12().getAbsError(),
              Metrics.toMilli(medium.h().getValue()), Metrics.toMilli(medium.h().getAbsError()),
              medium.getL2()
          });
        }
      }
      collector.close();
    }
    catch (IOException ex) {
      Logger.getLogger(getClass().getName()).log(Level.WARNING, path.toAbsolutePath().toString(), ex);
    }
  }
}