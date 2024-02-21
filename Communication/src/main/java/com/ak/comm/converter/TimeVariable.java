package com.ak.comm.converter;

import tec.uom.se.unit.Units;

import javax.measure.Unit;

public enum TimeVariable implements Variable<TimeVariable> {
  TIME {
    @Override
    public Unit<?> getUnit() {
      return Units.SECOND;
    }
  }
}
