package com.ak.rsm.prediction;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.DoubleStream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.ak.inverse.Inequality;
import com.ak.rsm.apparent.Apparent2Rho;
import com.ak.rsm.relative.RelativeMediumLayers;
import com.ak.rsm.system.InexactTetrapolarSystem;
import com.ak.util.Strings;

public class TetrapolarDerivativePrediction extends AbstractPrediction {
  @Nonnull
  private final Prediction prediction;

  @ParametersAreNonnullByDefault
  private TetrapolarDerivativePrediction(@Nonnegative double resistivityPredicted, double[] inequalityL2, Prediction prediction) {
    super(resistivityPredicted, inequalityL2);
    this.prediction = prediction;
  }

  @ParametersAreNonnullByDefault
  public static Prediction of(InexactTetrapolarSystem inexact, RelativeMediumLayers layers, @Nonnegative double rho1,
                              double[] measured) {
    Prediction prediction = TetrapolarPrediction.of(inexact, layers, rho1, measured[0]);
    double diffResistivityPredicted = Apparent2Rho.newDerivativeApparentByPhi2Rho(inexact.system().relativeSystem()).applyAsDouble(layers) * rho1;
    double[] inequalityL2 = DoubleStream.concat(
        Arrays.stream(prediction.getInequalityL2()),
        DoubleStream.of(Inequality.proportional().applyAsDouble(measured[1], diffResistivityPredicted))
    ).toArray();
    return new TetrapolarDerivativePrediction(diffResistivityPredicted, inequalityL2, prediction);
  }

  @Override
  public double[] getHorizons() {
    return prediction.getHorizons();
  }

  @Override
  public String toString() {
    return "%s, %s".formatted(prediction, Strings.dRhoByPhi(getPredicted()));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || !getClass().equals(o.getClass())) {
      return false;
    }
    TetrapolarDerivativePrediction that = (TetrapolarDerivativePrediction) o;
    return super.equals(o) && prediction.equals(that.prediction);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), prediction.hashCode());
  }
}
