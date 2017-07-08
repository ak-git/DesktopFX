package com.ak.digitalfilter;

import javax.measure.Quantity;
import javax.measure.quantity.Frequency;

import tec.uom.se.quantity.Quantities;
import tec.uom.se.unit.Units;

public enum Frequencies {
  ;

  public static final Quantity<Frequency> HZ_1000 = Quantities.getQuantity(1000, Units.HERTZ);
  public static final Quantity<Frequency> HZ_200 = Quantities.getQuantity(200, Units.HERTZ);
}
