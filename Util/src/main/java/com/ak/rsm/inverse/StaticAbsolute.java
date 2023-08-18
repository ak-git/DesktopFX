package com.ak.rsm.inverse;

import com.ak.rsm.measurement.Measurement;
import com.ak.rsm.medium.Layer1Medium;
import com.ak.rsm.medium.Layer2Medium;
import com.ak.rsm.relative.RelativeMediumLayers;

import java.util.Collection;
import java.util.function.Function;

enum StaticAbsolute {
  ;

  public static final Function<Collection<? extends Measurement>, Layer1Medium> LAYER_1 = Layer1Medium::new;
  public static final Function<Collection<? extends Measurement>, Layer2Medium> LAYER_2 =
      measurements -> {
        if (measurements.size() == 1) {
          return new Layer2Medium(measurements, RelativeMediumLayers.SINGLE_LAYER);
        }
        return new Layer2Medium(measurements, Relative.Static.solve(measurements));
      };
}
