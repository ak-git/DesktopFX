package com.ak.rsm;

import java.util.function.DoubleUnaryOperator;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.NelderMeadSimplex;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizer;

public final class SoundingDepthTest {
  private static class InequalityRbyRho2 implements DoubleUnaryOperator {
    private final UnivariateFunction dRbyRho2N;

    private InequalityRbyRho2(double k12, double sToL) {
      dRbyRho2N = new DerivativeRbyRho2Normalized(k12, sToL);
    }

    @Override
    public double applyAsDouble(double hToL) {
      return dRbyRho2N.value(hToL) - 0.5;
    }
  }

  private static PointValuePair solve(DoubleUnaryOperator inequality) {
    SimplexOptimizer optimizer = new SimplexOptimizer(-1, 1.0e-6);
    return optimizer.optimize(new MaxEval(100), new ObjectiveFunction(
            sToL -> inequality.applyAsDouble(sToL[0])), GoalType.MINIMIZE,
        new NelderMeadSimplex(1, 0.01),
        new InitialGuess(new double[] {1.0 / 3.0})
    );
  }
}
