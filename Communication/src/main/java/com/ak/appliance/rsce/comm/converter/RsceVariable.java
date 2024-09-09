package com.ak.appliance.rsce.comm.converter;

import com.ak.comm.converter.Variable;
import tech.units.indriya.unit.Units;

import javax.measure.MetricPrefix;
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
