package com.ak.rsm.measurement;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.ak.rsm.prediction.Prediction;
import com.ak.rsm.relative.RelativeMediumLayers;
import com.ak.rsm.resistance.Resistivity;
import com.ak.rsm.system.InexactTetrapolarSystem;

public interface Measurement extends Resistivity<InexactTetrapolarSystem> {
  @Nonnull
  Measurement merge(@Nonnull Measurement that);

  @Nonnull
  Prediction toPrediction(@Nonnull RelativeMediumLayers kw, @Nonnegative double rho1);
}
