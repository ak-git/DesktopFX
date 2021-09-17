package com.ak.rsm;

import java.util.Arrays;
import java.util.stream.DoubleStream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.inverse.Inequality;
import com.ak.util.Strings;

final class TetrapolarDerivativePrediction implements Prediction {
  @Nonnull
  private final Prediction prediction;
  private final double diffResistivityPredicted;
  @Nonnull
  private final double[] inequalityL2;

  @ParametersAreNonnullByDefault
  TetrapolarDerivativePrediction(TetrapolarSystem system, RelativeMediumLayers layers, @Nonnegative double rho1,
                                 double[] measured) {
    prediction = new TetrapolarPrediction(system, layers, rho1, measured[0]);
    diffResistivityPredicted = Apparent2Rho.newDerivativeApparentByPhi2Rho(system.toRelative()).applyAsDouble(layers) * rho1;
    inequalityL2 = DoubleStream.concat(
        Arrays.stream(prediction.getInequalityL2()),
        DoubleStream.of(Inequality.proportional().applyAsDouble(measured[1], diffResistivityPredicted))
    ).toArray();
  }

  @Override
  public double getResistivityPredicted() {
    return diffResistivityPredicted;
  }

  @Override
  public double[] getHorizons() {
    return prediction.getHorizons();
  }

  @Override
  public double[] getInequalityL2() {
    return Arrays.copyOf(inequalityL2, inequalityL2.length);
  }

  @Override
  public String toString() {
    return "%s, %s".formatted(String.valueOf(prediction), Strings.dRhoByPhi(diffResistivityPredicted));
  }
}
