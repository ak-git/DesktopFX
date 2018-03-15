package com.ak.rsm;

import java.util.Arrays;
import java.util.Comparator;
import java.util.function.DoubleBinaryOperator;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import com.ak.inverse.Inequality;
import com.ak.util.LineFileBuilder;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.NelderMeadSimplex;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizer;
import org.testng.annotations.Test;

import static com.ak.rsm.DerivativeRBySLNormalizedByRho1.DerivateBy.L;
import static com.ak.rsm.DerivativeRBySLNormalizedByRho1.DerivateBy.S;
import static tec.uom.se.unit.Units.METRE;

public class OptimumStoL2Test {
  private static final double[] EMPTY = {};
  private static final double LCC_SI = 1.0;
  private static final double RHO1_SI = 1.0;
  private static final double STEP_S = 0.05;

  private OptimumStoL2Test() {
  }

  @Test(enabled = false)
  public static void testOptimalS1S2() {
    LineFileBuilder.<double[]>of("%.2f %.2f %.6f").
        xRange(STEP_S * 2, 1.0 - STEP_S, STEP_S).
        yLog10Range(0.1, 10.0).
        add("zS1.txt", value -> value[0]).
        add("zS2.txt", value -> value[1]).
        generate((hToL, rho1rho2) -> {
          double[] optimalS1S2 = getOptimalS1S2(RHO1_SI / rho1rho2, hToL);
          Logger.getLogger(OptimumStoL2Test.class.getName()).info(String.format("rho1 / rho2 = %.2f; h = %.2f; s1, s2 = [%s]",
              rho1rho2, hToL, Arrays.stream(optimalS1S2).mapToObj(value -> String.format("%.3f", value)).collect(Collectors.joining(", "))));
          return optimalS1S2;
        });
  }


  @Test(enabled = false)
  public static void testOptimalS1S2FixedK() {
    DoubleStream.iterate(STEP_S * 2, operand -> operand + STEP_S).takeWhile(hToL -> hToL < LCC_SI + STEP_S).
        forEach(h -> {
          double rho2 = 0.1;
          double[] s1s2Optimal = getOptimalS1S2(rho2, h);
          double[] rho1rho2Errors = getRho1Rho2Errors2(s1s2Optimal[0], s1s2Optimal[1], rho2, h);
          Logger.getLogger(OptimumStoL2Test.class.getName()).info(
              String.format("h = %.2f; [%.3f - %.3f] errRho1 = %.6f errRho2 = %.6f", h,
                  s1s2Optimal[0], s1s2Optimal[1], rho1rho2Errors[0], rho1rho2Errors[1]));
        });
  }

  private static double[] getOptimalS1S2(double rho2, double hSI) {
    return new SimplexOptimizer(-1, 1.0e-10).optimize(new MaxEval(30000),
        new ObjectiveFunction(s1s2 ->
            Inequality.absolute().applyAsDouble(getRho1Rho2Errors2(s1s2[0], s1s2[1], rho2, hSI), new double[] {0, 0})
        ),
        GoalType.MINIMIZE, new NelderMeadSimplex(2, STEP_S),
        new InitialGuess(new double[] {0.5, STEP_S})).getPoint();
  }

  @Test(enabled = false)
  public static void testRho1Rho2ErrorsFixedKAndH() {
    LineFileBuilder.<double[]>of("%.2f %.2f %.6f").
        xRange(STEP_S * 2.0, 1.0 - STEP_S, STEP_S).
        yRange(STEP_S * 2.0, 1.0 - STEP_S, STEP_S).
        add("zErrorRho1.txt", value -> value[0]).add("zErrorRho2.txt", value -> value[1]).
        generate((sPU1, sPU2) -> getRho1Rho2Errors2(sPU1, sPU2, 0.1, 0.2));
  }

  private static double[] getRho1Rho2Errors2(double sPU1, double sPU2, double rho2, double hSI) {
    if (sPU2 > sPU1 - STEP_S) {
      return new double[] {Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY};
    }
    return getRho1Rho2Errors(sPU1, sPU2, rho2, hSI);
  }

  private static double[] getRho1Rho2Errors(double sPU1, double sPU2, double rho2, double hToL) {
    double k12 = ResistanceTwoLayer.getK12(RHO1_SI, rho2);
    return Stream.of(EMPTY)
        .flatMap(signDL -> scalarMultiply(signDL, -1, 1))
        .flatMap(signDS1 -> scalarMultiply(signDS1, -1, 1))
        .flatMap(signDS2 -> scalarMultiply(signDS2, -1, 1))
        .map((double[] doubles) -> {
          double signDL = doubles[0];
          double signDS1 = doubles[1];
          double signDS2 = doubles[2];

          DoubleBinaryOperator rightPart = (sToL, signS) ->
              new DerivativeRBySLNormalizedByRho1(k12, sToL, LCC_SI, L).value(hToL) * RHO1_SI * signDL +
                  new DerivativeRBySLNormalizedByRho1(k12, sToL, LCC_SI, S).value(hToL) * RHO1_SI * signS;
          double[] B = {rightPart.applyAsDouble(sPU1, signDS1), rightPart.applyAsDouble(sPU2, signDS2)};

          double[][] A = DoubleStream.of(sPU1, sPU2).mapToObj(sToL -> {
            double resistanceTwoLayer = new ResistanceTwoLayer(new TetrapolarSystem(sToL, 1.0, METRE)).value(RHO1_SI, rho2, hToL);
            double dRByRho2 = new DerivativeRbyRho2Normalized(k12, sToL).value(hToL) *
                (resistanceTwoLayer / rho2);
            double dRByRho1 = (resistanceTwoLayer - dRByRho2 * rho2) / RHO1_SI;
            return new double[] {dRByRho1, dRByRho2};
          }).toArray(value -> new double[value][2]);

          double[] array = new LUDecomposition(new Array2DRowRealMatrix(A)).getSolver().solve(new ArrayRealVector(B)).toArray();
          array[0] = Math.abs(array[0] * (LCC_SI / RHO1_SI));
          array[1] = Math.abs(array[1] * (LCC_SI / rho2));
          return array;
        })
        .max(Comparator.comparingDouble(optimum -> Inequality.proportional().applyAsDouble(optimum, new double[] {RHO1_SI, rho2})))
        .orElseThrow(IllegalStateException::new);
  }

  private static Stream<? extends double[]> scalarMultiply(double[] destination, double... values) {
    double[] pairs = Arrays.copyOf(destination, destination.length + 1);
    return DoubleStream.of(values).mapToObj(value -> new double[] {value}).flatMap(toAdd -> {
      System.arraycopy(toAdd, 0, pairs, pairs.length - toAdd.length, toAdd.length);
      return Stream.of(pairs);
    });
  }
}
