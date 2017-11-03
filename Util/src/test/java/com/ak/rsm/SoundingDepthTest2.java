package com.ak.rsm;

import com.ak.inverse.Inequality;
import com.ak.util.LineFileBuilder;
import org.apache.commons.math3.analysis.TrivariateFunction;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.NelderMeadSimplex;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizer;
import org.testng.annotations.Test;
import tec.uom.se.quantity.Quantities;
import tec.uom.se.unit.Units;

public class SoundingDepthTest2 {
  private static final double PRECISION = 1.0e-2;

  private SoundingDepthTest2() {
  }

  @Test(enabled = false)
  public static void testSimple() {
    double rho1 = 1.0;
    LineFileBuilder.<double[]>of("%.2f %.2f %.6f").
        xRange(0.01, 100, 0.01).
        yRange(0.5, 1.5, PRECISION).
        add("z.txt", value -> value[0]).
        generate(
            (rho1Rho2, hL) -> {
              TetrapolarSystem system = new TetrapolarSystem(0.5, 1.0, Units.METRE);
              TrivariateFunction twoLayerR = new ResistanceTwoLayer(system);
              double r = twoLayerR.value(rho1, 1.0 / rho1Rho2, hL);
              double apparentRho = system.getApparent(Quantities.getQuantity(r, Units.OHM));
              return new double[] {(apparentRho - rho1) / rho1};
            }
        );
  }

  @Test(enabled = false)
  public static void testOptimalH() {
    double rho1 = 1.0;
    LineFileBuilder.<double[]>of("%.2f %.3f %.6f").
        xRange(0.1, 0.9, PRECISION).
        yLog10Range(0.01, 100.0).
        add("z.txt", value -> value[0]).
        generate(
            (sL, rho1Rho2) -> {
              double dRho2RhoPredicted = ((1 + sL) / (sL * (1 - sL))) * 0.1 / 100;

              TetrapolarSystem system = new TetrapolarSystem(sL, 1.0, Units.METRE);
              TrivariateFunction twoLayerR = new ResistanceTwoLayer(system);

              SimplexOptimizer optimizer = new SimplexOptimizer(-1, 1.0e-14);
              return optimizer.optimize(new MaxEval(30000), new ObjectiveFunction(hL -> {
                    double r = twoLayerR.value(rho1, 1.0 / rho1Rho2, hL[0]);
                    double apparentRho = system.getApparent(Quantities.getQuantity(r, Units.OHM));
                    double dRho2RhoMeans = Math.abs(apparentRho - rho1) / rho1;
                    Inequality inequality = Inequality.absolute();
                    inequality.applyAsDouble(dRho2RhoMeans, dRho2RhoPredicted);
                    return inequality.getAsDouble();
                  }),
                  GoalType.MINIMIZE, new NelderMeadSimplex(1, 0.001), new InitialGuess(new double[] {1.0})
              ).getPoint();
            }
        );
  }
}
