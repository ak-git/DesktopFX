package com.ak.rsm.resistance;

import javax.annotation.Nonnegative;

public sealed interface Resistance extends Resistivity permits com.ak.rsm.measurement.Measurement, DerivativeResistance, TetrapolarResistance {
  @Nonnegative
  double ohms();
}
