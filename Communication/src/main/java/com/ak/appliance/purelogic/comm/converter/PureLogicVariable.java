package com.ak.appliance.purelogic.comm.converter;

import com.ak.comm.converter.Variable;
import tec.uom.se.unit.MetricPrefix;
import tec.uom.se.unit.Units;

import javax.measure.Unit;

public enum PureLogicVariable implements Variable<PureLogicVariable> {
  POSITION {
    @Override
    public Unit<?> getUnit() {
      return MetricPrefix.MICRO(Units.METRE);
    }
  }
}
