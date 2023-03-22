package com.ak.rsm.prediction;

import com.ak.inverse.Inequality;
import com.ak.rsm.apparent.Apparent2Rho;
import com.ak.rsm.relative.RelativeMediumLayers;
import com.ak.rsm.resistance.Resistivity;
import com.ak.util.Strings;

import javax.annotation.Nonnegative;
import javax.annotation.ParametersAreNonnullByDefault;

public final class TetrapolarPrediction extends AbstractPrediction {
  private TetrapolarPrediction(@Nonnegative double resistivityPredicted, @Nonnegative double inequalityL2) {
    super(resistivityPredicted, new double[] {inequalityL2});
  }

  @ParametersAreNonnullByDefault
  public static Prediction of(Resistivity resistivityMeasured, RelativeMediumLayers layers, @Nonnegative double rho1) {
    double resistivityPredicted;
    if (Double.compare(layers.k12(), 0.0) == 0) {
      resistivityPredicted = rho1;
    }
    else {
      resistivityPredicted = Apparent2Rho.newApparentDivRho1(resistivityMeasured.system().relativeSystem()).applyAsDouble(layers) * rho1;
    }
    double inequalityL2 = Inequality.proportional().applyAsDouble(resistivityMeasured.resistivity(), resistivityPredicted);
    return new TetrapolarPrediction(resistivityPredicted, inequalityL2);
  }

  @Override
  public String toString() {
    return "predicted %s".formatted(Strings.rho(getPredicted()));
  }
}
