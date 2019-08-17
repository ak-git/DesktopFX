package com.ak.rsm;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.util.Strings;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SingularValueDecomposition;

/**
 * Calculates <b>full</b> resistance R<sub>m-n</sub> (in Ohm) between electrodes for <b>single-layer</b> model.
 */
final class Resistance1Layer extends AbstractResistanceLayer<Potential1Layer> implements UnivariateFunction {
  Resistance1Layer(@Nonnull TetrapolarSystem electrodeSystem) {
    super(electrodeSystem, Potential1Layer::new);
  }

  /**
   * Calculates <b>full</b> resistance R<sub>m-n</sub> (in Ohm)
   *
   * @param rho specific resistance of <b>single-layer</b> in Ohm-m
   * @return resistance R<sub>m-n</sub> (in Ohm)
   */
  @Override
  public double value(double rho) {
    return applyAsDouble(u -> u.value(rho));
  }

  /**
   * Gets <b>apparent</b> specific resistance which is correspond to 1-layer model.
   *
   * @param rOhms in Ohms.
   * @return <b>apparent</b> specific resistance in Ohm-m.
   */
  double getApparent(@Nonnegative double rOhms) {
    return rOhms / value(1.0);
  }

  public static class Medium {
    @Nonnegative
    private final double rho;

    private Medium(@Nonnegative double rho) {
      this.rho = rho;
    }

    @Override
    public String toString() {
      return Strings.rho1(rho);
    }

    @Nonnull
    public static Resistance1Layer.Medium inverse(@Nonnull TetrapolarSystem[] systems, @Nonnull double[] rOhms) {
      RealMatrix coefficients = new Array2DRowRealMatrix(systems.length, 1);
      for (int i = 0; i < systems.length; i++) {
        coefficients.setEntry(i, 0, new Resistance1Layer(systems[i]).value(1.0));
      }
      RealVector constants = new ArrayRealVector(rOhms, false);
      RealVector solution = new SingularValueDecomposition(coefficients).getSolver().solve(constants);
      return new Medium(solution.getEntry(0));
    }
  }
}
