package com.ak.rsm.measurement;

import com.ak.rsm.resistance.DerivativeResistivity;

public sealed interface DerivativeMeasurement extends Measurement, DerivativeResistivity permits TetrapolarDerivativeMeasurement {
  double dOhms();
}
