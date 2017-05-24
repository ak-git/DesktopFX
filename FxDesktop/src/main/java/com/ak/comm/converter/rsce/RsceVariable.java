package com.ak.comm.converter.rsce;

import javax.measure.Unit;

import com.ak.comm.converter.Variable;
import tec.uom.se.unit.MetricPrefix;
import tec.uom.se.unit.Units;

public enum RsceVariable implements Variable<RsceVariable> {
  R1, R2;

  @Override
  public Unit<?> getUnit() {
    return MetricPrefix.CENTI(Units.OHM);
  }
}
