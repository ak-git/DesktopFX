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

  private static class InequalityRho implements UnivariateFunction {
    final double dl2L;
    final double dRL2ro;

    private InequalityRho(double dl2L, double dRL2ro) {
      this.dl2L = dl2L;
      this.dRL2ro = dRL2ro;
    }

    @Override
    public double value(double ba) {
      return lG(ba) * dl2L + mG(ba) * dRL2ro;
    }

    static double lG(double x) {
      return 2.0 * (1.0 + x) / (x * (1.0 - x));
    }

    static double mG(double x) {
      return Math.PI * 0.25 * (1.0 - pow(x, 2.0)) / x;
    }
  }

  private static class InequalityDeltaRho extends InequalityRho {
    private InequalityDeltaRho(double dl2L, double dRL2ro) {
      super(dl2L, dRL2ro);
    }

    @Override
    public double value(double ba) {
      if (ba > 1.0) {
        ba = 1.0;
      }
      return lG(ba) * dl2L + mG(ba) * dRL2ro * (2.0 / (lG(ba) * dl2L));
    }
  }

  private static PointValuePair getBtoA(UnivariateFunction inequality) {
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

    yVar.get().mapToObj(dRL2ro -> xVar.get().map(dL2L -> getBtoA(new InequalityRho(dL2L, dRL2ro)).getKey()[0])).
        map(stream -> stream.mapToObj(value -> String.format("%.6f", value)).collect(Collectors.joining("\t"))).
        collect(new LineFileCollector<>(Paths.get("OptimumBtoA_Rho.txt"), LineFileCollector.Direction.VERTICAL));

    yVar.get().mapToObj(dRL2ro -> xVar.get().map(dL2L -> getBtoA(new InequalityRho(dL2L, dRL2ro)).getValue())).
        map(stream -> stream.mapToObj(value -> String.format("%.6f", value)).collect(Collectors.joining("\t"))).
        collect(new LineFileCollector<>(Paths.get("ErrorsAtOptimumBtoA_Rho.txt"), LineFileCollector.Direction.VERTICAL));

    yVar.get().mapToObj(dRL2ro -> xVar.get().map(dL2L -> getBtoA(new InequalityDeltaRho(dL2L, dRL2ro)).getKey()[0])).
        map(stream -> stream.mapToObj(value -> String.format("%.6f", value)).collect(Collectors.joining("\t"))).
        collect(new LineFileCollector<>(Paths.get("OptimumBtoA_DeltaRho.txt"), LineFileCollector.Direction.VERTICAL));

    yVar.get().mapToObj(dRL2ro -> xVar.get().map(dL2L -> getBtoA(new InequalityDeltaRho(dL2L, dRL2ro)).getValue())).
        map(stream -> stream.mapToObj(value -> String.format("%.6f", value)).collect(Collectors.joining("\t"))).
        collect(new LineFileCollector<>(Paths.get("ErrorsAtOptimumBtoA_DeltaRho.txt"), LineFileCollector.Direction.VERTICAL));

  }

  private static DoubleStream doubleRange(double step, double end) {
    return DoubleStream.iterate(step, dl2L -> dl2L + step).
        limit(BigDecimal.valueOf(end / step).round(MathContext.UNLIMITED).intValue()).sequential();
  }
}
