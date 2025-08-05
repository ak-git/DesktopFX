package com.ak.rsm.prediction;

import com.ak.inverse.Inequality;
import com.ak.rsm.apparent.Apparent2Rho;
import com.ak.rsm.relative.RelativeMediumLayers;
import com.ak.rsm.resistance.DerivativeResistivity;
import com.ak.rsm.resistance.Resistivity;

import java.util.Arrays;
import java.util.stream.DoubleStream;

public enum Predictions {
  ;

  public static Prediction of(Resistivity resistivityMeasured, RelativeMediumLayers layers, double rho1) {
    Prediction prediction = innerOf(resistivityMeasured, layers, rho1);

    if (resistivityMeasured instanceof DerivativeResistivity derivativeResistivity) {
      double diffResistivityPredicted = Apparent2Rho.newDerApparentByPhiDivRho1(derivativeResistivity.system(), derivativeResistivity.dh())
          .applyAsDouble(layers) * rho1;
      double[] inequalityL2 = DoubleStream.concat(
          Arrays.stream(prediction.getInequalityL2()),
          DoubleStream.of(Inequality.proportional().applyAsDouble(derivativeResistivity.derivativeResistivity(), diffResistivityPredicted))
      ).toArray();
      return new TetrapolarDerivativePrediction(diffResistivityPredicted, inequalityL2, prediction);
    }

    return prediction;
  }

  public static Prediction of(Resistivity resistivityMeasured, double resistivityPredicted) {
    double inequalityL2 = Inequality.proportional().applyAsDouble(resistivityMeasured.resistivity(), resistivityPredicted);
    return new TetrapolarPrediction(resistivityPredicted, inequalityL2);
  }

  private static Prediction innerOf(Resistivity resistivityMeasured, RelativeMediumLayers layers, double rho1) {
    double resistivityPredicted = Apparent2Rho.newApparentDivRho1(resistivityMeasured.system().relativeSystem()).applyAsDouble(layers) * rho1;
    return of(resistivityMeasured, resistivityPredicted);
  }
}
