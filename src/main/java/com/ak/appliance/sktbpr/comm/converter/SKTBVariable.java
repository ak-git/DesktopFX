package com.ak.appliance.sktbpr.comm.converter;

import com.ak.comm.converter.Variable;

import javax.measure.Unit;

import static com.ak.util.Strings.ANGLE;
import static tec.uom.se.unit.Units.RADIAN;

public enum SKTBVariable implements Variable<SKTBVariable> {
  ROTATE, FLEX;

  @Override
  public Unit<?> getUnit() {
    return RADIAN.alternate(ANGLE);
  }
}
