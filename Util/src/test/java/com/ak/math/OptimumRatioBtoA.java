package com.ak.math;

import java.math.BigDecimal;
import java.math.MathContext;
import java.nio.file.Paths;
import java.util.function.Supplier;
import java.util.stream.DoubleStream;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.NelderMeadSimplex;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizer;

import static java.lang.StrictMath.pow;

public class OptimumRatioBtoA {
  private OptimumRatioBtoA() {
    throw new AssertionError();
  }

  private static class Inequality implements UnivariateFunction {
    private final double dl2L;
    private final double dRL2ro;

    private Inequality(double dl2L, double dRL2ro) {
      this.dl2L = dl2L;
      this.dRL2ro = dRL2ro;
    }

    @Override
    public double value(double ba) {
      return lG(ba) * dl2L + mG(ba) * dRL2ro;
    }

    private static double lG(double x) {
      return 2.0 * (1.0 + x) / (x * (1.0 - x));
    }

    private static double mG(double x) {
      return Math.PI * 0.25 * (1.0 - pow(x, 2.0)) / x;
    }
  }

  private static double getBtoA(double dl2L, double dRL2ro) {
    UnivariateFunction inequality = new Inequality(dl2L, dRL2ro);
    SimplexOptimizer optimizer = new SimplexOptimizer(-1, 1.0e-6);
    PointValuePair optimum = optimizer.optimize(new MaxEval(100), new ObjectiveFunction(
            ba -> inequality.value(ba[0])), GoalType.MINIMIZE,
        new NelderMeadSimplex(1, 0.01),
        new InitialGuess(new double[] {1.4142135623730951 - 1})
    );
    return optimum.getKey()[0];
  }

  public static void main(String[] args) {
    Supplier<DoubleStream> xVar = () -> doubleRange(1.0e-4, 0.1);
    xVar.get().mapToObj(value -> String.format("%.4f", value)).collect(
        new TabFileCollector(Paths.get("x(dl|L).txt"), TabFileCollector.Direction.HORIZONTAL));

    Supplier<DoubleStream> yVar = () -> doubleRange(1.0e-6, 1.0e-3);
    yVar.get().mapToObj(value -> String.format("%.6f", value)).collect(
        new TabFileCollector(Paths.get("y(dR*L|ro).txt"), TabFileCollector.Direction.VERTICAL));
  }

  private static DoubleStream doubleRange(double step, double end) {
    return DoubleStream.iterate(0.0, dl2L -> dl2L + step).
        limit(BigDecimal.valueOf(end / step).round(MathContext.UNLIMITED).intValue() + 1).sequential();
  }
}
