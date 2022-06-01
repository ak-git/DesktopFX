package com.ak.rsm.measurement;

import com.ak.rsm.resistance.DerivativeResistivity;

public interface DerivativeMeasurement extends Measurement, DerivativeResistivity {
  double dh();

  double dOhms();
}
