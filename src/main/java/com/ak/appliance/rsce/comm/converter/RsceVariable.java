package com.ak.appliance.rsce.comm.converter;

import com.ak.comm.converter.Variable;
import tec.uom.se.unit.MetricPrefix;
import tec.uom.se.unit.Units;

import javax.measure.Unit;

public enum RsceVariable implements Variable<RsceVariable> {
  R1 {
    @Override
    public Unit<?> getUnit() {
      return MetricPrefix.CENTI(Units.OHM);
    }
  },
  R2 {
    @Override
    public Unit<?> getUnit() {
      return MetricPrefix.CENTI(Units.OHM);
    }
  },
  ACCELEROMETER,
  OPEN {
    @Override
    public Unit<?> getUnit() {
      return Units.PERCENT;
    }
  },
  ROTATE {
    @Override
    public Unit<?> getUnit() {
      return Units.PERCENT;
    }
  },
  FINGER_CLOSED
}
