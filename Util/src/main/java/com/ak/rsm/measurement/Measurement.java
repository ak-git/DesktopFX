package com.ak.rsm.measurement;

import com.ak.rsm.resistance.Resistance;
import com.ak.rsm.system.InexactTetrapolarSystem;
import com.ak.rsm.system.TetrapolarSystem;

import javax.annotation.Nonnull;
import java.util.Collection;

public sealed interface Measurement extends Resistance permits DerivativeMeasurement, TetrapolarMeasurement {
  @Nonnull
  InexactTetrapolarSystem inexact();

  @Nonnull
  @Override
  default TetrapolarSystem system() {
    return inexact().system();
  }

  @Nonnull
  Measurement merge(@Nonnull Measurement that);

  @Nonnull
  static Collection<InexactTetrapolarSystem> inexact(@Nonnull Collection<? extends Measurement> measurements) {
    return measurements.stream().map(Measurement::inexact).toList();
  }
}
