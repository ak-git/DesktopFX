package com.ak.rsm.inverse;

import com.ak.rsm.measurement.DerivativeMeasurement;
import com.ak.rsm.medium.Layer2Medium;
import com.ak.rsm.relative.RelativeMediumLayers;
import com.ak.rsm.system.InexactTetrapolarSystem;

import java.util.Collection;
import java.util.function.Function;

enum DynamicAbsolute {
  ;

  static Layer2Medium ofLayer2(Collection<? extends DerivativeMeasurement> measurements, Function<Collection<InexactTetrapolarSystem>, Regularization> regularizationFunction) {
    return new Layer2Medium(
        measurements,
        measurements.size() > 1 ? Relative.Dynamic.solve(measurements, regularizationFunction) : RelativeMediumLayers.SINGLE_LAYER
    );
  }
}
