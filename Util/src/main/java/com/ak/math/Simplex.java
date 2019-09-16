package com.ak.math;

import java.time.Duration;
import java.time.LocalTime;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

import com.ak.util.Strings;
import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleBounds;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.CMAESOptimizer;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.NelderMeadSimplex;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizer;
import org.apache.commons.math3.random.MersenneTwister;
import tec.uom.se.AbstractUnit;

public class Simplex {
  private Simplex() {
  }

  public static PointValuePair optimizeCMAES(@Nonnull MultivariateFunction function, @Nonnull SimpleBounds bounds,
                                             @Nonnull double[] initialGuess, @Nonnull double[] initialSteps) {
    return new CMAESOptimizer(30000, 1.0e-6, true, 0,
        10, new MersenneTwister(), false, null)
        .optimize(
            new MaxEval(30000),
            new ObjectiveFunction(function),
            GoalType.MINIMIZE,
            new InitialGuess(initialGuess),
            bounds,
            new CMAESOptimizer.Sigma(initialSteps),
            new CMAESOptimizer.PopulationSize(2 * (4 + (int) (3.0 * StrictMath.log(initialGuess.length))))
        );
  }

  public static PointValuePair optimize(@Nonnull MultivariateFunction function, @Nonnull SimpleBounds bounds,
                                        @Nonnull double[] initialGuess, @Nonnull double[] initialSteps) {
    return optimize(new MultivariateFunction() {
      private LocalTime prev = LocalTime.now();

      @Override
      public double value(double[] point) {
        for (int i = 0; i < point.length; i++) {
          if (bounds.getLower()[i] > point[i] || bounds.getUpper()[i] < point[i]) {
            return Double.POSITIVE_INFINITY;
          }
        }
        double value = function.value(point);
        if (Duration.between(prev, LocalTime.now()).getSeconds() >= 10) {
          Logger.getLogger(Simplex.class.getName()).log(Level.INFO, String.format("%s; %.6f", Strings.toString("%.3f", point, AbstractUnit.ONE), value));
          prev = LocalTime.now();
        }
        return value;
      }
    }, initialGuess, initialSteps);
  }

  private static PointValuePair optimize(@Nonnull MultivariateFunction function,
                                         @Nonnull double[] initialGuess, @Nonnull double[] initialSteps) {
    return new SimplexOptimizer(1.0e-6, 1.0e-6).optimize(new MaxEval(30000), new ObjectiveFunction(function), GoalType.MINIMIZE,
        new NelderMeadSimplex(initialSteps), new InitialGuess(initialGuess));
  }
}