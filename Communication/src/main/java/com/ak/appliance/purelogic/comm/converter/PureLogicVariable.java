package com.ak.appliance.purelogic.comm.converter;

import com.ak.comm.converter.Variable;
import tech.units.indriya.unit.Units;

import javax.measure.MetricPrefix;
import javax.measure.Unit;

public enum PureLogicVariable implements Variable<PureLogicVariable> {
  POSITION {
    @Override
    public Unit<?> getUnit() {
      return MetricPrefix.MICRO(Units.METRE);
    }
  }
}
