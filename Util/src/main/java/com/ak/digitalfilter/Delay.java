package com.ak.digitalfilter;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.measure.Quantity;
import javax.measure.quantity.Frequency;
import javax.measure.quantity.Time;

import tec.uom.se.quantity.Quantities;
import tec.uom.se.unit.Units;

interface Delay {
  @Nonnegative
  default double getDelay() {
    return 0.0;
  }

  @Nonnull
  default Quantity<Time> getDelay(@Nonnull Quantity<Frequency> frequency) {
    return Quantities.getQuantity(frequency.to(Units.HERTZ).inverse().getValue().doubleValue() * getDelay(), Units.SECOND);
  }
}
