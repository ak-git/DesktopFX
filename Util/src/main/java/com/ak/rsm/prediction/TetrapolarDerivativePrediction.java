package com.ak.rsm.prediction;

import com.ak.inverse.Inequality;
import com.ak.rsm.apparent.Apparent2Rho;
import com.ak.rsm.relative.RelativeMediumLayers;
import com.ak.rsm.resistance.DerivativeResistivity;
import com.ak.util.Strings;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.DoubleStream;

public final class TetrapolarDerivativePrediction extends AbstractPrediction {
  @Nonnull
  private final Prediction prediction;

  @ParametersAreNonnullByDefault
  private TetrapolarDerivativePrediction(@Nonnegative double resistivityPredicted, double[] inequalityL2, Prediction prediction) {
    super(resistivityPredicted, inequalityL2);
    this.prediction = prediction;
  }

  @ParametersAreNonnullByDefault
  public static Prediction of(DerivativeResistivity resistivityMeasured, RelativeMediumLayers layers, @Nonnegative double rho1) {
    Prediction prediction = TetrapolarPrediction.of(resistivityMeasured, layers, rho1);
    double diffResistivityPredicted = Apparent2Rho.newDerivativeApparentByPhiDivRho1(resistivityMeasured.system(), resistivityMeasured.dh())
        .applyAsDouble(layers) * rho1;
    double[] inequalityL2 = DoubleStream.concat(
        Arrays.stream(prediction.getInequalityL2()),
        DoubleStream.of(Inequality.proportional().applyAsDouble(resistivityMeasured.derivativeResistivity(), diffResistivityPredicted))
    ).toArray();
    return new TetrapolarDerivativePrediction(diffResistivityPredicted, inequalityL2, prediction);
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
