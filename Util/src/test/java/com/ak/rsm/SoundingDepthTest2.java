package com.ak.rsm;

import java.util.function.DoubleUnaryOperator;

import javax.annotation.Nonnegative;

import com.ak.inverse.Inequality;
import com.ak.util.LineFileBuilder;
import org.apache.commons.math3.analysis.TrivariateFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.testng.annotations.Test;
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
              double apparentRho = system.getApparent(r);
              return new double[] {(apparentRho - rho1) / rho1};
            }
        );
  }


  /**
   * Finds <b>h/L</b> where <b>accuracy Apparent Rho1 == accuracy Rho</b>
   */
  private static class InequalityApparentRho implements DoubleUnaryOperator {
    private final double rho1Rho2;
    private final double dRho2RhoPredicted;
    private final TetrapolarSystem system;
    private final TrivariateFunction twoLayerR;

    private InequalityApparentRho(double sL, double rho1Rho2, double dLToL) {
      this.rho1Rho2 = rho1Rho2;
      dRho2RhoPredicted = ((1 + sL) / (sL * (1 - sL))) * dLToL;
      system = new TetrapolarSystem(sL, 1.0, Units.METRE);
      twoLayerR = new ResistanceTwoLayer(system);
    }

    @Override
    public double applyAsDouble(double hL) {
      double rho1 = 1.0;
      double r = twoLayerR.value(rho1, 1.0 / rho1Rho2, hL);
      double apparentRho = system.getApparent(r);
      double dRho2RhoMeans = Math.abs(apparentRho - rho1) / rho1;
      Inequality inequality = Inequality.absolute();
      inequality.applyAsDouble(dRho2RhoMeans, dRho2RhoPredicted);
      return inequality.getAsDouble();
    }
  }


  @Test(enabled = false)
  public static void testOptimalH() {
    LineFileBuilder.<double[]>of("%.2f %.3f %.6f").
        xRange(0.1, 0.9, PRECISION).yLog10Range(0.01, 100.0).add("z.txt", value -> value[0]).
        generate((sL, rho1Rho2) -> SoundingDepthTest.solve(hL -> new InequalityApparentRho(sL, rho1Rho2, 0.1 / 100.0).applyAsDouble(hL), GoalType.MINIMIZE, 1.0).getPoint());
  }

  /**
   * Finds <b>s/L = f(rho1/rho2)</b> where <b>sounding h/L</b> reaches maximum.
   */
  private static class OptimalSL implements DoubleUnaryOperator {
    @Nonnegative
    private final double rho1Rho2;
    @Nonnegative
    private final double dLToL;

    private OptimalSL(double rho1Rho2, double dLToL) {
      this.rho1Rho2 = rho1Rho2;
      this.dLToL = dLToL;
    }

    @Override
    public double applyAsDouble(double sL) {
      return SoundingDepthTest.solve(new InequalityApparentRho(sL, rho1Rho2, dLToL), GoalType.MINIMIZE, 1.0).getPoint()[0];
    }
  }

  @Test(enabled = false)
  public static void testOptimalSL() {
    LineFileBuilder.<double[]>of("%.3f %.5f %.2f").
        xLog10Range(0.01, 100.0).
        yLog10Range(0.1 / 100.0, 0.5 / 100.0).
        add("z.txt", value -> value[0]).
        generate((rho1Rho2, dLToL) ->
            SoundingDepthTest.solve(sL -> new OptimalSL(rho1Rho2, dLToL).applyAsDouble(sL), GoalType.MAXIMIZE, 1.0 / 3.0).getPoint());
  }
}
