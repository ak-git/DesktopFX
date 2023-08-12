package com.ak.rsm.inverse;

import com.ak.rsm.measurement.Measurement;
import com.ak.rsm.medium.Layer1Medium;
import com.ak.rsm.medium.Layer2Medium;
import com.ak.rsm.system.InexactTetrapolarSystem;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.function.Function;

enum StaticAbsolute {
  ;

  static Layer1Medium solve(@Nonnull Collection<? extends Measurement> measurements) {
    return new Layer1Medium(measurements);
  }

  static Layer2Medium solve(Collection<? extends Measurement> measurements,
                            Function<Collection<InexactTetrapolarSystem>, Regularization> regularizationFunction) {
    return new Layer2Medium(measurements, new StaticRelative(measurements, regularizationFunction).get());
  }
}
