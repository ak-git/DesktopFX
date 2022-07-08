package com.ak.comm.converter.prv_rr;

import javax.measure.Unit;

import com.ak.comm.converter.Variable;
import tec.uom.se.unit.MetricPrefix;
import tec.uom.se.unit.Units;

public enum PrvRRInputVariable implements Variable<PrvRRInputVariable> {
  TIME {
    @Override
    public Unit<?> getUnit() {
      return MetricPrefix.MILLI(Units.SECOND);
    }
  },
  RR
}
