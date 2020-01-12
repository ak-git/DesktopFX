package com.ak.rsm;

import java.util.function.DoubleUnaryOperator;
import java.util.stream.DoubleStream;

import com.ak.util.LineFileBuilder;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.NelderMeadSimplex;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizer;
import org.apache.commons.math3.util.Pair;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static java.lang.StrictMath.pow;

public class OptimumStoLTest {
  private static final double SQRT_2 = 1.4142135623730951;

  private OptimumStoLTest() {
  }

  private static class InequalityRho implements DoubleUnaryOperator {
    final double dl2L;
    final double dRL2rho;

    private InequalityRho(double dl2L, double dRL2rho) {
      this.dl2L = dl2L;
      this.dRL2rho = dRL2rho;
    }

    @Override
    public double applyAsDouble(double sToL) {
      return lG(sToL) * dl2L + mG(sToL) * dRL2rho;
    }

    static double lG(double x) {
      return (1.0 + x) / (x * (1.0 - x));
    }

    static double mG(double x) {
      return Math.PI * 0.25 * (1.0 - pow(x, 2.0)) / x;
    }
  }

  private static class InequalityDeltaRho extends InequalityRho {
    private InequalityDeltaRho(double dl2L, double dRL2rho) {
      super(dl2L, dRL2rho);
    }

    @Override
    public double applyAsDouble(double sToL) {
      if (sToL > 1.0) {
        sToL = 1.0;
      }
      return lG(sToL) * dl2L + 2.0 * mG(sToL) * dRL2rho;
    }
  }

  private static PointValuePair solve(DoubleUnaryOperator inequality) {
    SimplexOptimizer optimizer = new SimplexOptimizer(-1, 1.0e-6);
    return optimizer.optimize(new MaxEval(100), new ObjectiveFunction(
            sToL -> inequality.applyAsDouble(sToL[0])), GoalType.MINIMIZE,
        new NelderMeadSimplex(1, 0.01),
        new InitialGuess(new double[] {SQRT_2 - 1})
    );
  }

  @DataProvider(name = "dl2L-dRL2rho-Rho")
  public static Object[][] inequalityRhoProvider() {
    return new Object[][] {
        //dl / L, dR * L / rho, s/l, relative error Rho
        {1.0e-3, 1.0e-4, SQRT_2 - 1 + 0.01, 0.006},
        {1.0e-3, 1.0e-5, SQRT_2 - 1, 0.006},
        {1.0e-2, 1.0e-4, SQRT_2 - 1, 0.06},
        {1.0e-2, 1.0e-5, SQRT_2 - 1, 0.06},
    };
  }

  @DataProvider(name = "dl2L-dRL2dRho-DeltaRho")
  public static Object[][] inequalityDeltaRhoProvider() {
    return new Object[][] {
        //dl / L, dR * L / dRho, s/l, relative error Delta-Rho
        {1.0e-3, 1.0e-1, 0.92, 0.05},
        {1.0e-3, 1.0e-2, 0.78, 0.02},
        {1.0e-2, 1.0e-1, 0.78, 0.18},
        {1.0e-2, 1.0e-2, 0.55, 0.08},
    };
  }

  @Test(dataProvider = "dl2L-dRL2rho-Rho")
  public static void testRho(double dl2L, double dRL2rho, double sToL, double relError) {
    PointValuePair pair = solve(new InequalityRho(dl2L, dRL2rho));
    Assert.assertEquals(pair.getKey()[0], sToL, 0.01);
    Assert.assertEquals(pair.getValue(), relError, 0.01);
  }

  @Test(dataProvider = "dl2L-dRL2dRho-DeltaRho")
  public static void testDeltaRho(double dl2L, double dRL2rho, double sToL, double relError) {
    PointValuePair pair = solve(new InequalityDeltaRho(dl2L, dRL2rho));
    Assert.assertEquals(pair.getKey()[0], sToL, 0.01);
    Assert.assertEquals(pair.getValue(), relError, 0.01);
  }

  @Test(enabled = false)
  public static void testRhoOptimalStoL() {
    LineFileBuilder.<PointValuePair>of("%.4f %.6f %.6f").
        xRange(1.0e-4, 1.0e-1, 1.0e-4).
        yRange(1.0e-6, 1.0e-3, 1.0e-6).
        add("Rho_Optimum_sToL.txt", value -> value.getKey()[0]).
        add("Rho_ErrorsAt_Optimum_sToL.txt", Pair::getValue).
        generate((dL2L, dRL2rho) -> solve(new InequalityRho(dL2L, dRL2rho)));
  }

  @Test(enabled = false)
  public static void testRhoErrorsAt() {
    DoubleStream.of(1.0 / 3.0, 0.5, 2.0 / 3.0).forEachOrdered(sToL -> LineFileBuilder.of("%.4f %.6f %.6f").
        xRange(1.0e-4, 1.0e-1, 1.0e-4).
        yRange(1.0e-6, 1.0e-3, 1.0e-6).
        generate(String.format("Rho_ErrorsAt_%.2f.txt", sToL),
            (dL2L, dRL2rho) -> new InequalityRho(dL2L, dRL2rho).applyAsDouble(sToL)));
  }

  @Test(enabled = false)
  public static void testDeltaRhoOptimalStoL() {
    LineFileBuilder.<PointValuePair>of("%.4f %.4f %.6f").
        xRange(1.0e-4, 1.0e-1, 1.0e-4).
        yRange(1.0e-4, 1.0e-1, 1.0e-4).
        add("DeltaRho_Optimum_sToL.txt", value -> value.getKey()[0]).
        add("DeltaRho_ErrorsAt_Optimum_sToL.txt", Pair::getValue).
        generate((dL2L, dRL2rho) -> solve(new InequalityDeltaRho(dL2L, dRL2rho)));
  }

  @Test(enabled = false)
  public static void testDeltaRhoErrorsAt() {
    DoubleStream.of(1.0 / 3.0, 0.5, 2.0 / 3.0).forEachOrdered(sToL -> LineFileBuilder.of("%.4f %.4f %.6f").
        xRange(1.0e-4, 1.0e-1, 1.0e-4).
        yRange(1.0e-4, 1.0e-1, 1.0e-4).
        generate(String.format("DeltaRho_ErrorsAt_%.2f.txt", sToL),
            (dL2L, dRL2rho) -> new InequalityDeltaRho(dL2L, dRL2rho).applyAsDouble(sToL)));
  }
}
