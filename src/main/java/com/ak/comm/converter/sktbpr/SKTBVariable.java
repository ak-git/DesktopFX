package com.ak.comm.converter.sktbpr;

import com.ak.comm.converter.Variable;

import javax.measure.Unit;

import static tec.uom.se.unit.Units.RADIAN;

public enum SKTBVariable implements Variable<SKTBVariable> {
  ROTATE, FLEX;

  @Override
  public Unit<?> getUnit() {
    return RADIAN.alternate("°");
  }
}
