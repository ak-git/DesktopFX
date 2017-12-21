package com.ak.comm.converter.rsce;

import javax.measure.Unit;

import com.ak.comm.converter.Variable;
import tec.uom.se.unit.MetricPrefix;
import tec.uom.se.unit.Units;

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
