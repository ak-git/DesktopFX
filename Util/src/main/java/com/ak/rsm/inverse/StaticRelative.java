package com.ak.rsm.inverse;

import com.ak.rsm.measurement.Measurement;
import com.ak.rsm.system.InexactTetrapolarSystem;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.function.Function;

final class StaticRelative extends AbstractRelative<Measurement> {
  @ParametersAreNonnullByDefault
  StaticRelative(Collection<? extends Measurement> measurements,
                 Function<Collection<InexactTetrapolarSystem>, Regularization> regularizationFunction) {
    super(measurements, new StaticInverse(measurements), regularizationFunction,
        new StaticErrors(Measurement.inexact(measurements)));
  }
}
