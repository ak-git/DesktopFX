package com.ak.util;

import tec.uom.se.quantity.Quantities;

import static tec.uom.se.AbstractUnit.ONE;
import static tec.uom.se.unit.MetricPrefix.MILLI;
import static tec.uom.se.unit.Units.METRE;
import static tec.uom.se.unit.Units.PERCENT;

public enum Metrics {
  ;

  public static double fromMilli(double mm) {
    return Quantities.getQuantity(mm, MILLI(METRE)).to(METRE).getValue().doubleValue();
  }

  public static double toMilli(double metre) {
    return Quantities.getQuantity(metre, METRE).to(MILLI(METRE)).getValue().doubleValue();
  }

  public static double fromPercents(double percents) {
    return Quantities.getQuantity(percents, PERCENT).to(ONE).getValue().doubleValue();
  }

  public static double toPercents(double ones) {
    return Quantities.getQuantity(ones, ONE).to(PERCENT).getValue().doubleValue();
  }
}
