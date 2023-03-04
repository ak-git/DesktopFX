package com.ak.rsm.resistance;

public sealed interface DerivativeResistivity extends Resistivity
    permits com.ak.rsm.measurement.DerivativeMeasurement, DerivativeResistance {
  double derivativeResistivity();

  double dh();
}
