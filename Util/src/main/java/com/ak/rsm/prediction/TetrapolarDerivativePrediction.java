package com.ak.rsm.prediction;

import com.ak.util.Strings;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

final class TetrapolarDerivativePrediction extends AbstractPrediction {
  @Nonnull
  private final Prediction prediction;

  @ParametersAreNonnullByDefault
  TetrapolarDerivativePrediction(@Nonnegative double resistivityPredicted, double[] inequalityL2, Prediction prediction) {
    super(resistivityPredicted, inequalityL2);
    this.prediction = prediction;
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
