package com.ak.appliance.caliper.comm.converter;

import com.ak.comm.converter.Variable;
import tech.units.indriya.unit.Units;

import javax.measure.MetricPrefix;
import javax.measure.Unit;
import java.util.Set;

public enum CaliperVariable implements Variable<CaliperVariable> {
  D;

  public static final int FREQUENCY = 10;

  @Override
  public Set<Option> options() {
    return Option.addToDefault(Option.TEXT_VALUE_BANNER);
  }

  @Override
  public Unit<?> getUnit() {
    return MetricPrefix.MICRO(Units.METRE);
  }
}
