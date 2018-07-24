package com.ak.util;

import tec.uom.se.quantity.Quantities;

import static tec.uom.se.unit.MetricPrefix.MILLI;
import static tec.uom.se.unit.Units.METRE;

public enum Metrics {
  ;

  public static double fromMilli(double mm) {
    return Quantities.getQuantity(mm, MILLI(METRE)).to(METRE).getValue().doubleValue();
  }
}
