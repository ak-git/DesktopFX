package com.ak.digitalfilter;

import javax.annotation.Nonnull;
import javax.measure.Quantity;
import javax.measure.quantity.Frequency;
import javax.measure.quantity.Time;

import tec.uom.se.quantity.Quantities;
import tec.uom.se.unit.Units;

enum Filters {
  ;

  static Quantity<Frequency> getFrequency(DigitalFilter filter, @Nonnull Quantity<Frequency> frequency) {
    return Quantities.getQuantity(frequency.to(Units.HERTZ).getValue().doubleValue() * filter.getFrequencyFactor(), Units.HERTZ);
  }

  static Quantity<Time> getDelay(DigitalFilter filter, @Nonnull Quantity<Frequency> frequency) {
    return Quantities.getQuantity(frequency.to(Units.HERTZ).inverse().getValue().doubleValue() * filter.getDelay(), Units.SECOND);
  }
}
