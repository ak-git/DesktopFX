package com.ak.rsm;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

import com.ak.inverse.Inequality;
import com.ak.util.LineFileBuilder;
import com.ak.util.LineFileCollector;
import com.ak.util.Strings;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.NelderMeadSimplex;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizer;
import org.testng.Assert;
import org.testng.annotations.Test;

import static com.ak.rsm.DerivativeRBySLNormalizedByRho1.DerivateBy.L;
import static com.ak.rsm.DerivativeRBySLNormalizedByRho1.DerivateBy.S;
import static tec.uom.se.unit.Units.METRE;

public class OptimumStoL2Test {
  private static final String D_RHO_1 = Strings.DELTA + Strings.RHO + Strings.LOW_1;
  private static final String D_RHO_2 = Strings.DELTA + Strings.RHO + Strings.LOW_2;
  private static final String D_L = Strings.DELTA + "L";
  private static final double LCC_SI = 1.0;
  private static final double RHO1_SI = 1.0;
  private static final double STEP_S = 0.05;

  private OptimumStoL2Test() {
  }

  @Test(enabled = false)
  public static void testOptimalS1S2() {
    LineFileBuilder.<double[]>of("%.2f %.3f %.6f").
        xRange(STEP_S * 2, 1.0 - STEP_S, STEP_S).
        yLog10Range(0.01, 100.0).
        add("zS1.txt", value -> value[0]).
        add("zS2.txt", value -> value[1]).
        add("zRho1Error.txt", value -> value[2]).
        add("zRho2Error.txt", value -> value[3]).
        generate((hToL, rho1rho2) -> {
          double rho2 = RHO1_SI / rho1rho2;
          double[] optimalS1S2 = getOptimalS1S2(rho2, hToL);
          double[] rho1rho2Errors = getRho1Rho2Errors(optimalS1S2[0], optimalS1S2[1], rho2, hToL);

          Logger.getLogger(OptimumStoL2Test.class.getName()).info(
              String.format("%s / %s = %.2f; h / L = %.2f; [s1, s2] = [%s]; [%s, %s] = [%s] %s",
                  D_RHO_1, D_RHO_2, rho1rho2, hToL,
                  Arrays.stream(optimalS1S2).mapToObj(value -> String.format("%.3f", value)).collect(Collectors.joining(", ")),
                  D_RHO_1, D_RHO_2,
                  Arrays.stream(rho1rho2Errors).mapToObj(value -> String.format("%.3f", value)).collect(Collectors.joining(", ")),
                  D_L
              )
          );
          return DoubleStream.concat(Arrays.stream(optimalS1S2), Arrays.stream(rho1rho2Errors)).toArray();
        });
  }

  @Test(enabled = false)
  public static void testFixedS1S2() {
    LineFileBuilder.<double[]>of("%.2f %.3f %.6f").
        xRange(STEP_S * 2, 1.0 - STEP_S, STEP_S).
        yLog10Range(0.01, 100.0).
        add("zRho1Error.txt", value -> value[0]).
        add("zRho2Error.txt", value -> value[1]).
        generate((hToL, rho1rho2) -> Arrays.stream(getRho1Rho2Errors(1.0 / 3.0, 3.0 / 5.0, RHO1_SI / rho1rho2, hToL)).toArray());
  }


  @Test(enabled = false)
  public static void testOptimalS1S2FixedK() {
    DoubleStream.iterate(STEP_S * 2, operand -> operand + STEP_S).takeWhile(hToL -> hToL < LCC_SI + STEP_S).
        forEach(h -> {
          double rho2 = 0.1;
          double[] s1s2Optimal = getOptimalS1S2(rho2, h);
          double[] rho1rho2Errors = getRho1Rho2Errors2(s1s2Optimal[0], s1s2Optimal[1], rho2, h);
          Logger.getLogger(OptimumStoL2Test.class.getName()).info(
              String.format("h / L = %.2f; [%.3f - %.3f] %s = %.6f %s; %s = %.6f %s", h,
                  s1s2Optimal[0], s1s2Optimal[1],
                  D_RHO_1, rho1rho2Errors[0], D_L,
                  D_RHO_2, rho1rho2Errors[1], D_L)
          );
        });
  }

  private static double[] getOptimalS1S2(double rho2, double hSI) {
    return new SimplexOptimizer(-1, 1.0e-10).optimize(new MaxEval(30000),
        new ObjectiveFunction(s1s2 ->
            Inequality.absolute().applyAsDouble(getRho1Rho2Errors2(s1s2[0], s1s2[1], rho2, hSI), i -> 0.0)
        ),
        GoalType.MINIMIZE, new NelderMeadSimplex(2, STEP_S),
        new InitialGuess(new double[] {3.0 / 5.0, 1.0 / 3.0})).getPoint();
  }

  @Test(enabled = false)
  public static void testRho1Rho2ErrorsFixedKAndH() {
    LineFileBuilder.<double[]>of("%.2f %.2f %.6f").
        xRange(STEP_S * 2.0, 1.0 - STEP_S, STEP_S).
        yRange(STEP_S * 2.0, 1.0 - STEP_S, STEP_S).
        add("zErrorRho1.txt", value -> value[0]).add("zErrorRho2.txt", value -> value[1]).
        generate((sPU1, sPU2) -> getRho1Rho2Errors2(sPU1, sPU2, 1.0 / 5.0, 0.5));
  }

  @Test(enabled = false)
  public static void testRho1Rho2ErrorsIterateH() throws IOException {
    Assert.assertNotNull(DoubleStream.iterate(0.1, operand -> operand + 0.02).takeWhile(value -> value <= 1.0).mapToObj(hToL -> {
      double[] errors = getRho1Rho2Errors2(3.0 / 5.0, 1.0 / 3.0, 10, hToL);
      return String.format("h / L = %.2f; %s = %.3f %s; %s = %.3f %s", hToL,
          D_RHO_1, errors[0], D_L,
          D_RHO_2, errors[1], D_L);
    }).collect(new LineFileCollector(Paths.get("errors.txt"), LineFileCollector.Direction.VERTICAL)));
  }

  @Test(enabled = false)
  public static void testRho1Rho2ErrorsSingle() {
    double[] errors = getRho1Rho2Errors2(3.0 / 5.0, 1.0 / 3.0, 0.1, 0.2);
    Logger.getAnonymousLogger().info(
        String.format("%s = %.2f %s; %s = %.2f %s",
            D_RHO_1, Math.abs(errors[0]), D_L,
            D_RHO_2, Math.abs(errors[1]), D_L)
    );
  }

  private static double[] getRho1Rho2Errors2(double sPU1, double sPU2, double rho2, double hSI) {
    if (sPU2 > sPU1 - STEP_S || Math.max(sPU1, sPU2) >= LCC_SI) {
      return new double[] {Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY};
    }
    return getRho1Rho2Errors(sPU1, sPU2, rho2, hSI);
  }

  private static double[] getRho1Rho2Errors(double s1, double s2, double rho2, double hToL) {
    double sMax = Math.max(s1, s2);
    double sMin = Math.min(s1, s2);
    double k12 = ResistanceTwoLayer.getK12(RHO1_SI, rho2);
    double signDL = 1.0;
    double signDSMax = -1.0;
    double signDSMin = 1.0;

    DoubleUnaryOperator R = sToL -> new ResistanceTwoLayer(new TetrapolarSystem(sToL, 1.0, METRE)).value(RHO1_SI, rho2, hToL);

    DoubleBinaryOperator rightPart = (sToL, signS) ->
        (new DerivativeRBySLNormalizedByRho1(k12, sToL, LCC_SI, L).value(hToL) * RHO1_SI * signDL +
            new DerivativeRBySLNormalizedByRho1(k12, sToL, LCC_SI, S).value(hToL) * RHO1_SI * signS) * LCC_SI / R.applyAsDouble(sToL);
    double[] B = {rightPart.applyAsDouble(sMax, signDSMax), rightPart.applyAsDouble(sMin, signDSMin)};

    double[][] A = DoubleStream.of(sMax, sMin).mapToObj(sToL -> {
      double dRByRho2 = new DerivativeRbyRho2Normalized(k12, sToL).value(hToL);
      return new double[] {1.0 - dRByRho2, dRByRho2};
    }).toArray(value -> new double[value][2]);

    try {
      double[] solution = new LUDecomposition(new Array2DRowRealMatrix(A)).getSolver().solve(new ArrayRealVector(B)).toArray();
      return Arrays.stream(solution).map(Math::abs).toArray();
    }
    catch (RuntimeException e) {
      return new double[] {Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY};
    }
  }
}
