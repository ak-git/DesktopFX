package com.ak.comm.converter;

import javax.measure.Unit;

import tec.uom.se.AbstractUnit;

public interface Variable {
  default Unit<?> getUnit() {
    return AbstractUnit.ONE;
  }
}
