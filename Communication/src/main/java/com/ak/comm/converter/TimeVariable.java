package com.ak.comm.converter;

import javax.measure.Unit;

import tec.uom.se.unit.Units;

public enum TimeVariable implements Variable<TimeVariable> {
  TIME {
    @Override
    public Unit<?> getUnit() {
      return Units.SECOND;
    }
  }
}
