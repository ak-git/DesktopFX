package com.ak.rsm.inverse;

import com.ak.rsm.measurement.DerivativeMeasurement;
import com.ak.rsm.medium.Layer2Medium;
import com.ak.rsm.relative.RelativeMediumLayers;
import com.ak.rsm.system.InexactTetrapolarSystem;

import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Function;

enum DynamicAbsolute {
  ;

  public static final BiFunction<Collection<? extends DerivativeMeasurement>, Function<Collection<InexactTetrapolarSystem>, Regularization>, Layer2Medium>
      LAYER_2 = (measurements, regularizationFunction) -> new Layer2Medium(measurements,
      measurements.size() > 1 ? new DynamicRelative(measurements, regularizationFunction).get() : RelativeMediumLayers.SINGLE_LAYER);
}
