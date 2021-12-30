package com.ak.rsm.prediction;

import java.util.Arrays;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.inverse.Inequality;
import com.ak.rsm.apparent.Apparent2Rho;
import com.ak.rsm.relative.RelativeMediumLayers;
import com.ak.rsm.system.InexactTetrapolarSystem;
import com.ak.util.Strings;

public final class TetrapolarPrediction extends AbstractPrediction {
  @Nonnull
  private final double[] horizons;

  private TetrapolarPrediction(@Nonnegative double resistivityPredicted, @Nonnull double[] horizons,
                               @Nonnegative double inequalityL2) {
    super(resistivityPredicted, new double[] {inequalityL2});
    this.horizons = Arrays.copyOf(horizons, horizons.length);
  }

  @ParametersAreNonnullByDefault
  public static Prediction of(InexactTetrapolarSystem inexact, RelativeMediumLayers layers,
                              @Nonnegative double rho1, @Nonnegative double resistivityMeasured) {
    double resistivityPredicted;
    if (Double.compare(layers.k12(), 0.0) == 0) {
      resistivityPredicted = rho1;
    }
    else {
      resistivityPredicted = Apparent2Rho.newNormalizedApparent2Rho(inexact.system().relativeSystem()).applyAsDouble(layers) * rho1;
    }
    double[] horizons = {inexact.getHMin(layers.k12()), inexact.getHMax(layers.k12())};
    double inequalityL2 = Inequality.proportional().applyAsDouble(resistivityMeasured, resistivityPredicted);
    return new TetrapolarPrediction(resistivityPredicted, horizons, inequalityL2);
  }

  @Override
  public double[] getHorizons() {
    return Arrays.copyOf(horizons, horizons.length);
  }

  @Override
  public String toString() {
    return "predicted %s; %s".formatted(Strings.rho(getPredicted()), Prediction.toStringHorizons(horizons));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || !getClass().equals(o.getClass())) {
      return false;
    }
    TetrapolarPrediction that = (TetrapolarPrediction) o;
    return super.equals(o) && Arrays.equals(horizons, that.horizons);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + Arrays.hashCode(horizons);
    return result;
  }
}
