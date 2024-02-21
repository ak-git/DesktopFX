package com.ak.rsm.measurement;

import com.ak.rsm.resistance.Resistance;
import com.ak.rsm.system.InexactTetrapolarSystem;
import com.ak.rsm.system.TetrapolarSystem;

import java.util.Collection;

public sealed interface Measurement extends Resistance permits DerivativeMeasurement, TetrapolarMeasurement {
  InexactTetrapolarSystem inexact();

  @Override
  default TetrapolarSystem system() {
    return inexact().system();
  }

  Measurement merge(Measurement that);

  static Measurement average(Collection<? extends Measurement> measurements) {
    return measurements.stream().map(Measurement.class::cast).reduce(Measurement::merge).orElseThrow();
  }

  static Collection<InexactTetrapolarSystem> inexact(Collection<? extends Measurement> measurements) {
    return measurements.stream().map(Measurement::inexact).toList();
  }
}
