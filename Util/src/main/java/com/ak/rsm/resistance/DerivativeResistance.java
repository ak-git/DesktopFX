package com.ak.rsm.resistance;

public interface DerivativeResistance extends Resistance, DerivativeResistivity {
  double dh();

  double dOhms();
}
