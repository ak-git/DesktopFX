package com.ak.math;

import java.math.BigDecimal;
import java.math.MathContext;
import java.nio.file.Paths;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

import org.apache.commons.math3.analysis.UnivariateFunction;
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

import static java.lang.StrictMath.pow;

public final class OptimumRatioBtoA {

  private static final double SQRT_2 = 1.4142135623730951;

  private static class InequalityRho implements UnivariateFunction {
    final double dl2L;
    final double dRL2rho;

    private InequalityRho(double dl2L, double dRL2rho) {
      this.dl2L = dl2L;
      this.dRL2rho = dRL2rho;
    }

    @Override
    public double value(double ba) {
      return lG(ba) * dl2L + mG(ba) * dRL2rho;
    }

    static double lG(double x) {
      return 2.0 * (1.0 + x) / (x * (1.0 - x));
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
    public double value(double ba) {
      if (ba > 1.0) {
        ba = 1.0;
      }
      return lG(ba) * dl2L + 2.0 * mG(ba) * dRL2rho;
    }
  }

  private static PointValuePair solve(UnivariateFunction inequality) {
    SimplexOptimizer optimizer = new SimplexOptimizer(-1, 1.0e-6);
    return optimizer.optimize(new MaxEval(100), new ObjectiveFunction(
            ba -> inequality.value(ba[0])), GoalType.MINIMIZE,
        new NelderMeadSimplex(1, 0.01),
        new InitialGuess(new double[] {SQRT_2 - 1})
    );
  }

  @DataProvider(name = "dl2L-dRL2rho-Rho")
  public static Object[][] inequalityRhoProvider() {
    return new Object[][] {
        //dl / L, dR * L / rho, b/a, relative error Rho
        {1.0e-3, 1.0e-4, SQRT_2 - 1, 0.012},
        {1.0e-3, 1.0e-5, SQRT_2 - 1, 0.012},
        {1.0e-2, 1.0e-4, SQRT_2 - 1, 0.12},
        {1.0e-2, 1.0e-5, SQRT_2 - 1, 0.12},
    };
  }

  @DataProvider(name = "dl2L-dRL2dRho-DeltaRho")
  public static Object[][] inequalityDeltaRhoProvider() {
    return new Object[][] {
        //dl / L, dR * L / dRho, b/a, relative error Delta-Rho
        {1.0e-3, 1.0e-1, 0.89, 0.075},
        {1.0e-3, 1.0e-2, 0.72, 0.028},
        {1.0e-2, 1.0e-1, 0.72, 0.28},
        {1.0e-2, 1.0e-2, 0.50, 0.14},
    };
  }

  @Test(dataProvider = "dl2L-dRL2rho-Rho")
  public void testRho(double dl2L, double dRL2rho, double ba, double relError) {
    PointValuePair pair = solve(new InequalityRho(dl2L, dRL2rho));
    Assert.assertEquals(pair.getKey()[0], ba, 0.01);
    Assert.assertEquals(pair.getValue(), relError, 0.01);
  }

  @Test(dataProvider = "dl2L-dRL2dRho-DeltaRho")
  public void testDeltaRho(double dl2L, double dRL2rho, double ba, double relError) {
    PointValuePair pair = solve(new InequalityDeltaRho(dl2L, dRL2rho));
    Assert.assertEquals(pair.getKey()[0], ba, 0.01);
    Assert.assertEquals(pair.getValue(), relError, 0.01);
  }

  @DataProvider(name = "dl2L-dRL2rho")
  public static Object[][] dl2dRL2rho() {
    Supplier<DoubleStream> xVar = () -> doubleRange(1.0e-4, 1.0e-1);
    xVar.get().mapToObj(value -> String.format("%.4f", value)).collect(
        new LineFileCollector<>(Paths.get("x(dl|L).txt"), LineFileCollector.Direction.HORIZONTAL));

    Supplier<DoubleStream> yVar = () -> doubleRange(1.0e-6, 1.0e-3);
    yVar.get().mapToObj(value -> String.format("%.6f", value)).collect(
        new LineFileCollector<>(Paths.get("y(dR*L|rho).txt"), LineFileCollector.Direction.VERTICAL));
    return new Object[][] {{xVar, yVar}};
  }

  @Test(dataProvider = "dl2L-dRL2rho", enabled = false)
  public void testOptimalBtoAforRho(Supplier<DoubleStream> xVar, Supplier<DoubleStream> yVar) {
    yVar.get().mapToObj(dRL2rho -> xVar.get().map(dL2L -> solve(new InequalityRho(dL2L, dRL2rho)).getKey()[0])).
        map(stream -> stream.mapToObj(value -> String.format("%.6f", value)).collect(Collectors.joining("\t"))).
        collect(new LineFileCollector<>(Paths.get("OptimumBtoA_Rho.txt"), LineFileCollector.Direction.VERTICAL));

    yVar.get().mapToObj(dRL2rho -> xVar.get().map(dL2L -> solve(new InequalityRho(dL2L, dRL2rho)).getValue())).
        map(stream -> stream.mapToObj(value -> String.format("%.6f", value)).collect(Collectors.joining("\t"))).
        collect(new LineFileCollector<>(Paths.get("ErrorsAtOptimumBtoA_Rho.txt"), LineFileCollector.Direction.VERTICAL));
  }

  @DataProvider(name = "dl2L-dRL2dRho")
  public static Object[][] dl2dRL2dRho() {
    Supplier<DoubleStream> xVar = () -> doubleRange(1.0e-4, 1.0e-1);
    xVar.get().mapToObj(value -> String.format("%.4f", value)).collect(
        new LineFileCollector<>(Paths.get("x(dl|L).txt"), LineFileCollector.Direction.HORIZONTAL));

    Supplier<DoubleStream> yVar = () -> doubleRange(1.0e-3, 1.0);
    yVar.get().mapToObj(value -> String.format("%.6f", value)).collect(
        new LineFileCollector<>(Paths.get("y(dR*L|deltaRho).txt"), LineFileCollector.Direction.VERTICAL));
    return new Object[][] {{xVar, yVar}};
  }

  @Test(dataProvider = "dl2L-dRL2dRho", enabled = false)
  public void testOptimalBtoAforDeltaRho(Supplier<DoubleStream> xVar, Supplier<DoubleStream> yVar) {
    yVar.get().mapToObj(dRL2rho -> xVar.get().map(dL2L -> solve(new InequalityDeltaRho(dL2L, dRL2rho)).getKey()[0])).
        map(stream -> stream.mapToObj(value -> String.format("%.6f", value)).collect(Collectors.joining("\t"))).
        collect(new LineFileCollector<>(Paths.get("OptimumBtoA_DeltaRho.txt"), LineFileCollector.Direction.VERTICAL));

    yVar.get().mapToObj(dRL2rho -> xVar.get().map(dL2L -> solve(new InequalityDeltaRho(dL2L, dRL2rho)).getValue())).
        map(stream -> stream.mapToObj(value -> String.format("%.6f", value)).collect(Collectors.joining("\t"))).
        collect(new LineFileCollector<>(Paths.get("ErrorsAtOptimumBtoA_DeltaRho.txt"), LineFileCollector.Direction.VERTICAL));
  }

  @Test(dataProvider = "dl2L-dRL2dRho", enabled = false)
  public void testRhoAndDeltaRhoForBtoA(Supplier<DoubleStream> xVar, Supplier<DoubleStream> yVar) {
    DoubleStream.of(1.0 / 3.0, 0.5, 2.0 / 3.0).forEachOrdered(ba -> {
      yVar.get().mapToObj(dRL2rho -> xVar.get().map(dL2L -> new InequalityRho(dL2L, dRL2rho).value(ba))).
          map(stream -> stream.mapToObj(value -> String.format("%.6f", value)).collect(Collectors.joining("\t"))).
          collect(new LineFileCollector<>(Paths.get(String.format("ErrorsAt%.2f_Rho.txt", ba)),
              LineFileCollector.Direction.VERTICAL));
      yVar.get().mapToObj(dRL2rho -> xVar.get().map(dL2L -> new InequalityDeltaRho(dL2L, dRL2rho).value(ba))).
          map(stream -> stream.mapToObj(value -> String.format("%.6f", value)).collect(Collectors.joining("\t"))).
          collect(new LineFileCollector<>(Paths.get(String.format("ErrorsAt%.2f_DeltaRho.txt", ba)),
              LineFileCollector.Direction.VERTICAL));
    });
  }

  private static DoubleStream doubleRange(double step, double end) {
    return DoubleStream.iterate(step, dl2L -> dl2L + step).
        limit(BigDecimal.valueOf(end / step).round(MathContext.UNLIMITED).intValue()).sequential();
  }
}
