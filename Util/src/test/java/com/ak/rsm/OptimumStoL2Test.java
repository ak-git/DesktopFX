package com.ak.rsm;

import java.util.Arrays;
import java.util.Comparator;
import java.util.logging.Logger;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import com.ak.inverse.Inequality;
import com.ak.util.LineFileBuilder;
import org.apache.commons.math3.analysis.TrivariateFunction;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.NelderMeadSimplex;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizer;
import org.testng.annotations.Test;

import static tec.uom.se.unit.Units.METRE;

public class OptimumStoL2Test {
  private static final double[] EMPTY = {};
  private static final double LCC_SI = 1.0;
  private static final double RHO1_SI = 1.0;
  private static final double RHO2_SI = Double.POSITIVE_INFINITY;
  private static final double DS = 0.001;
  private static final double PRECISION_S = 0.05;

  private OptimumStoL2Test() {
  }

  @Test(enabled = false)
  public static void testOptimalS1S2() {
    DoubleStream.iterate(PRECISION_S * 2, operand -> operand + PRECISION_S).limit(20).
        forEach(h -> {
          SimplexOptimizer optimizer = new SimplexOptimizer(-1, 1.0e-5);
          optimizer.optimize(new MaxEval(30000), new ObjectiveFunction(s1s2 -> getRho1HErrors2(s1s2[0], s1s2[1], h)[0]),
              GoalType.MINIMIZE, new NelderMeadSimplex(2, PRECISION_S),
              new InitialGuess(new double[] {0.5, PRECISION_S}));
          PointValuePair optimize = optimizer.optimize();
          Logger.getLogger(OptimumStoL2Test.class.getName()).
              info(String.format("h = %.2f; [%.3f - %.3f] err = %.6f", h,
                  optimize.getPoint()[0], optimize.getPoint()[1], optimize.getValue()));
        });
  }

  @Test(enabled = false)
  public static void testRho1AndHErrors() {
    LineFileBuilder.<double[]>of("%.2f %.2f %.6f").
        xRange(PRECISION_S * 2.0, 1.0 - PRECISION_S, PRECISION_S).
        yRange(PRECISION_S * 2.0, 1.0 - PRECISION_S, PRECISION_S).
        add("zErrorRho1.txt", value -> value[0]).add("zErrorH.txt", value -> value[1]).
        generate((sPU1, sPU2) -> getRho1HErrors2(sPU1, sPU2, 0.5));
  }

  private static double[] getRho1HErrors2(double sPU1, double sPU2, double hSI) {
    if (sPU2 > sPU1 - PRECISION_S) {
      return new double[] {Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY};
    }
    return getRho1HErrors(sPU1, sPU2, hSI);
  }

  private static double[] getRho1HErrors(double sPU1, double sPU2, double hSI) {
    TrivariateFunction model1 = new ResistanceTwoLayer(new TetrapolarSystem(sPU1, LCC_SI, METRE));
    TrivariateFunction model2 = new ResistanceTwoLayer(new TetrapolarSystem(sPU2, LCC_SI, METRE));
    double[] PREDICTED = {RHO1_SI, hSI};

    PointValuePair pointValuePair = Stream.of(EMPTY).
        flatMap(d -> scalarMultiply(d, LCC_SI - DS, LCC_SI + DS)).
        flatMap(d -> scalarMultiply(d, sPU2 - DS, sPU2 + DS)).
        flatMap(d -> scalarMultiply(d, sPU1 - DS, sPU1 + DS)).
        map((double[] doubles) -> {
          double lCC = doubles[0];
          double s2 = doubles[1];
          double s1 = doubles[2];

          double real1 = new ResistanceTwoLayer(
              new TetrapolarSystem(s1, lCC, METRE)).value(RHO1_SI, RHO2_SI, hSI);
          double real2 = new ResistanceTwoLayer(
              new TetrapolarSystem(s2, lCC, METRE)).value(RHO1_SI, RHO2_SI, hSI);

          SimplexOptimizer optimizer = new SimplexOptimizer(-1, 1.0e-14);
          return optimizer.optimize(new MaxEval(30000), new ObjectiveFunction(rho1AndH -> {
                Inequality inequality = Inequality.logDifference();
                inequality.applyAsDouble(model1.value(rho1AndH[0], RHO2_SI, rho1AndH[1]), real1);
                inequality.applyAsDouble(model2.value(rho1AndH[0], RHO2_SI, rho1AndH[1]), real2);
                return inequality.getAsDouble();
              }),
              GoalType.MINIMIZE, new NelderMeadSimplex(2, 0.001), new InitialGuess(PREDICTED)
          );
        }).
        max(Comparator.comparingDouble(optimum -> {
          Inequality inequality = Inequality.logDifference();
          for (int i = 0; i < PREDICTED.length; i++) {
            inequality.applyAsDouble(optimum.getPoint()[i], PREDICTED[i]);
          }
          return inequality.getAsDouble();
        })).orElseThrow(IllegalStateException::new);

    double[] result = new double[PREDICTED.length];
    for (int i = 0; i < result.length; i++) {
      result[i] = Inequality.proportional().applyAsDouble(pointValuePair.getPoint()[i], PREDICTED[i]);
    }
    return result;
  }

  private static Stream<? extends double[]> scalarMultiply(double[] destination, double... values) {
    double[] pairs = Arrays.copyOf(destination, destination.length + 1);
    return DoubleStream.of(values).mapToObj(value -> new double[] {value}).flatMap(toAdd -> {
      System.arraycopy(toAdd, 0, pairs, pairs.length - toAdd.length, toAdd.length);
      return Stream.of(pairs);
    });
  }
}
