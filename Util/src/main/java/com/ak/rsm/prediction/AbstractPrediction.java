package com.ak.rsm.prediction;

import java.util.Arrays;
import java.util.Objects;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;

public abstract class AbstractPrediction implements Prediction {
  @Nonnegative
  private final double predicted;
  @Nonnull
  private final double[] inequalityL2;

  protected AbstractPrediction(@Nonnegative double predicted, @Nonnull double[] inequalityL2) {
    this.predicted = predicted;
    this.inequalityL2 = inequalityL2.clone();
  }

  @Override
  public final double getPredicted() {
    return predicted;
  }

  @Override
  public final double[] getInequalityL2() {
    return inequalityL2.clone();
  }

  @Override
  @OverridingMethodsMustInvokeSuper
  public boolean equals(Object o) {
    if (!(o instanceof AbstractPrediction that)) {
      return false;
    }
    return Double.compare(that.predicted, predicted) == 0 && Arrays.equals(inequalityL2, that.inequalityL2);
  }

  @Override
  @OverridingMethodsMustInvokeSuper
  public int hashCode() {
    int result = Objects.hash(predicted);
    result = 31 * result + Arrays.hashCode(inequalityL2);
    return result;
  }
}
