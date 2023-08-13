package com.ak.rsm.inverse;

import com.ak.rsm.measurement.Measurement;

import javax.annotation.Nonnull;
import java.util.Collection;

final class StaticRelative extends AbstractRelative<Measurement> {
  StaticRelative(@Nonnull Collection<? extends Measurement> measurements) {
    super(measurements, new StaticInverse(measurements), Regularization.Interval.ZERO_MAX.of(0.0));
  }
}
