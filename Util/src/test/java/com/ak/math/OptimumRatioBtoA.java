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

  private static PointValuePair getBtoA(double dl2L, double dRL2ro) {
    UnivariateFunction inequality = new Inequality(dl2L, dRL2ro);
    SimplexOptimizer optimizer = new SimplexOptimizer(-1, 1.0e-6);
    return optimizer.optimize(new MaxEval(100), new ObjectiveFunction(
            ba -> inequality.value(ba[0])), GoalType.MINIMIZE,
        new NelderMeadSimplex(1, 0.01),
        new InitialGuess(new double[] {1.4142135623730951 - 1})
    );
  }

  public static void main(String[] args) {
    Supplier<DoubleStream> xVar = () -> doubleRange(1.0e-4, 1.0e-1);
    xVar.get().mapToObj(value -> String.format("%.4f", value)).collect(
        new LineFileCollector<>(Paths.get("x(dl|L).txt"), LineFileCollector.Direction.HORIZONTAL));

    Supplier<DoubleStream> yVar = () -> doubleRange(1.0e-6, 1.0e-3);
    yVar.get().mapToObj(value -> String.format("%.6f", value)).collect(
        new LineFileCollector<>(Paths.get("y(dR*L|ro).txt"), LineFileCollector.Direction.VERTICAL));

    yVar.get().mapToObj(dRL2ro -> xVar.get().map(dL2L -> getBtoA(dL2L, dRL2ro).getKey()[0])).
        map(stream -> stream.mapToObj(value -> String.format("%.6f", value)).collect(Collectors.joining("\t"))).
        collect(new LineFileCollector<>(Paths.get("OptimumRatioBtoA.txt"), LineFileCollector.Direction.VERTICAL));

    yVar.get().mapToObj(dRL2ro -> xVar.get().map(dL2L -> getBtoA(dL2L, dRL2ro).getValue())).
        map(stream -> stream.mapToObj(value -> String.format("%.6f", value)).collect(Collectors.joining("\t"))).
        collect(new LineFileCollector<>(Paths.get("ErrorsAtOptimumRatioBtoA.txt"), LineFileCollector.Direction.VERTICAL));
  }

  private static DoubleStream doubleRange(double step, double end) {
    return DoubleStream.iterate(step, dl2L -> dl2L + step).
        limit(BigDecimal.valueOf(end / step).round(MathContext.UNLIMITED).intValue()).sequential();
  }
}
