package com.ak.rsm;

import java.util.Arrays;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.inverse.Inequality;
import com.ak.util.Metrics;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.NelderMeadSimplex;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizer;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static tec.uom.se.unit.MetricPrefix.MILLI;
import static tec.uom.se.unit.Units.METRE;

public class ResistanceTwoLayerTest {
  private ResistanceTwoLayerTest() {
  }

  @DataProvider(name = "layer-model", parallel = true)
  public static Object[][] twoLayerParameters() {
    return new Object[][] {
        {new Double[] {1.0, 1.0}, 0.0, 20.0, 40.0, 21.221},
        {new Double[] {2.0, 2.0}, 0.0, 20.0, 40.0, 21.221 * 2.0},
        {new Double[] {1.0, 1.0}, 0.0, 40.0, 80.0, 10.610},
        {new Double[] {0.5, 0.5}, 0.0, 40.0, 80.0, 10.610 / 2.0},

        {new Double[] {3.5, 1.35}, 10.0, 20.0, 40.0, 59.108},
        {new Double[] {5.0, 2.0}, 15.0, 20.0, 40.0, 95.908},
        {new Double[] {7.0, 1.0}, 20.0, 40.0, 80.0, 50.132},
        {new Double[] {9.5, 0.5}, 30.0, 40.0, 80.0, 81.831},

        {new Double[] {20.0, 1.0}, 1.0, 40.0, 80.0, 10.649}
    };
  }

  @Test(dataProvider = "layer-model")
  public static void testLayer(Double[] rho, double hmm, double smm, double lmm, double rOhm) {
    TetrapolarSystem system = new TetrapolarSystem(smm, lmm, MILLI(METRE));
    Assert.assertEquals(new ResistanceTwoLayer(system).value(rho[0], rho[1], Metrics.fromMilli(hmm)), rOhm, 0.001);
  }

  @Test
  public static void testRho1ToRho2() {
    Assert.assertEquals(ResistanceTwoLayer.getRho1ToRho2(1.0), 0.0, Float.MIN_NORMAL);
    Assert.assertEquals(ResistanceTwoLayer.getRho1ToRho2(0.0), 1.0, Float.MIN_NORMAL);
    Assert.assertEquals(ResistanceTwoLayer.getRho1ToRho2(-1.0), Double.POSITIVE_INFINITY, Float.MIN_NORMAL);
    Assert.assertEquals(ResistanceTwoLayer.getRho1ToRho2(0.1), ResistanceTwoLayer.getK12(0.1, 1.0), Float.MIN_NORMAL);
    Assert.assertEquals(ResistanceTwoLayer.getRho1ToRho2(10.0), ResistanceTwoLayer.getK12(10.0, 1.0), Float.MIN_NORMAL);
    Assert.assertEquals(ResistanceTwoLayer.getK12(10.0, Double.POSITIVE_INFINITY), 1.0, Float.MIN_NORMAL);
  }

  @Test(expectedExceptions = CloneNotSupportedException.class)
  public static void testNotClone() throws CloneNotSupportedException {
    new ResistanceTwoLayer(new TetrapolarSystem(1, 2, MILLI(METRE))).clone();
  }

  private static final Logger LOGGER = Logger.getLogger(ResistanceTwoLayerTest.class.getName());

  @DataProvider(name = "two-measures")
  public static Object[][] twoMeasuresParameters() {
    return new Object[][] {
        {
            new double[] {10.0, 1.0}, Metrics.fromMilli(15.0),
            new TetrapolarSystem[] {
                new TetrapolarSystem(30.0, 50.0, MILLI(METRE)),
                new TetrapolarSystem(10.0, 50.0, MILLI(METRE))
            },
        },
        {
            new double[] {10.1504, 1.0018}, Metrics.fromMilli(15.1),
            new TetrapolarSystem[] {
                new TetrapolarSystem(30.0, 50.0, MILLI(METRE)),
                new TetrapolarSystem(10.0, 50.0, MILLI(METRE))
            },
        },
    };
  }

  @Test(dataProvider = "two-measures", enabled = false)
  public static void testTwoMeasures(@Nonnull double[] rho, @Nonnegative double hSI, @Nonnull TetrapolarSystem[] systems) {
    DoubleStream.of(0.1, -0.1).forEach(eLmm -> {
      double dH = Metrics.fromMilli(0.01);

      double[] rOhms = DoubleStream.of(hSI, hSI + dH).flatMap(h ->
          Arrays.stream(Arrays.stream(systems).mapToDouble(s ->
              new ResistanceTwoLayer(s.newWithError(eLmm, MILLI(METRE))).value(rho[0], rho[1], h)).toArray())
      ).toArray();
      LOGGER.info(DoubleStream.of(rOhms).mapToObj(value ->
          String.format("%.3f", value)).collect(Collectors.joining(", ", "[", "]"))
      );

      PointValuePair optimize = find(systems, dH, rOhms, DoubleStream.concat(Arrays.stream(rho), DoubleStream.of(hSI)).toArray());
      LOGGER.info(DoubleStream.of(optimize.getPoint()).mapToObj(value ->
          String.format("%.4f", value)).collect(Collectors.joining(", ", "[", String.format("] %.6f", optimize.getValue()))));
    });
  }

  @DataProvider(name = "two-measures-inverse")
  public static Object[][] twoMeasuresInverseParameters() {
    return new Object[][] {
        {
            new TetrapolarSystem[] {
                new TetrapolarSystem(30.0, 50.0, MILLI(METRE)),
                new TetrapolarSystem(10.0, 50.0, MILLI(METRE))
            },
            Metrics.fromMilli(0.01),
            new double[] {186.857, 34.420, 186.917, 34.441},
        }
    };
  }

  @Test(dataProvider = "two-measures-inverse", enabled = false)
  public static void testTwoMeasuresInverse(@Nonnull TetrapolarSystem[] systems, @Nonnegative double dH,
                                            @Nonnull double[] rOhms) {
    double rho1InitialGuess = systems[0].getApparent(rOhms[0]);
    double rho2InitialGuess = systems[1].getApparent(rOhms[1]);

    PointValuePair optimize = find(systems, dH, rOhms, new double[] {rho1InitialGuess, rho2InitialGuess, 0.0});
    LOGGER.info(DoubleStream.of(optimize.getPoint()).mapToObj(value ->
        String.format("%.4f", value)).collect(Collectors.joining(", ", "[", String.format("] %.6f", optimize.getValue()))));
  }

  private static PointValuePair find(@Nonnull TetrapolarSystem[] systems, @Nonnegative double dHSI,
                                     @Nonnull double[] rOhms, @Nonnull double[] rho1rho2hInitial) {
    return new SimplexOptimizer(-1, 1.0e-16).optimize(new MaxEval(30000), new ObjectiveFunction(rho1rho2h -> {
          double[] rOhmsPredicted = DoubleStream.of(0, dHSI)
              .flatMap(dH -> Arrays.stream(Arrays.stream(systems).mapToDouble(s ->
                  new ResistanceTwoLayer(s).value(rho1rho2h[0], rho1rho2h[1], rho1rho2h[2] + dH)).toArray())
              ).toArray();
          LOGGER.config(DoubleStream.of(rOhmsPredicted).mapToObj(value ->
              String.format("%.4f", value)).collect(Collectors.joining(", ", "[", "]")));
          return Inequality.logDifference().applyAsDouble(rOhms, rOhmsPredicted);
        }),
        GoalType.MINIMIZE, new NelderMeadSimplex(3, 0.00001), new InitialGuess(rho1rho2hInitial)
    );
  }

  @DataProvider(name = "two-measures-h-fixed-inverse")
  public static Object[][] twoMeasuresHFixedInverseParameters() {
    return new Object[][] {
        {
            new TetrapolarSystem[] {
                new TetrapolarSystem(30.0, 50.0, MILLI(METRE)),
                new TetrapolarSystem(10.0, 50.0, MILLI(METRE))
            },
            Metrics.fromMilli(15.0),
            new double[] {186.857, 34.420},
        },
        {
            new TetrapolarSystem[] {
                new TetrapolarSystem(30.0, 50.0, MILLI(METRE)),
                new TetrapolarSystem(10.0, 50.0, MILLI(METRE))
            },
            Metrics.fromMilli(15.0),
            new double[] {31.550, 8.039},
        },
    };
  }

  @Test(dataProvider = "two-measures-h-fixed-inverse", enabled = false)
  public static void testTwoMeasuresHFixedInverse(@Nonnull TetrapolarSystem[] systems, @Nonnegative double h,
                                                  @Nonnull double[] rOhms) {
    PointValuePair optimize = find(systems, h, rOhms);
    LOGGER.info(DoubleStream.of(optimize.getPoint()).mapToObj(value ->
        String.format("%.4f", value)).collect(Collectors.joining(", ", "[", String.format("] %.6f", optimize.getValue()))));
  }

  private static PointValuePair find(@Nonnull TetrapolarSystem[] systems, @Nonnegative double hSI, @Nonnull double[] rOhms) {
    double[] rho1rho2Initial = {0, systems[0].getApparent(rOhms[0])};

    return new SimplexOptimizer(-1, 1.0e-16).optimize(new MaxEval(30000), new ObjectiveFunction(rho1rho2 -> {
          double[] rOhmsPredicted = Arrays.stream(systems).mapToDouble(s ->
              new ResistanceTwoLayer(s).value(rho1rho2[0], rho1rho2[1], hSI)).toArray();
          LOGGER.config(DoubleStream.of(rOhmsPredicted).mapToObj(value ->
              String.format("%.4f", value)).collect(Collectors.joining(", ", "[", "]")));
          return Inequality.absolute().applyAsDouble(rOhms, rOhmsPredicted);
        }),
        GoalType.MINIMIZE, new NelderMeadSimplex(2, 0.0001), new InitialGuess(rho1rho2Initial)
    );
  }
}