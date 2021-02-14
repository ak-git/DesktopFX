package com.ak.comm.converter.purelogic;

import javax.measure.Unit;

import com.ak.comm.converter.Variable;
import tec.uom.se.unit.MetricPrefix;
import tec.uom.se.unit.Units;

public enum PureLogicVariable implements Variable<PureLogicVariable> {
  POSITION {
    @Override
    public Unit<?> getUnit() {
      return MetricPrefix.MICRO(Units.METRE);
    }
  }
}
