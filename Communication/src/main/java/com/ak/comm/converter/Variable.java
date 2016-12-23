package com.ak.comm.converter;

import javax.measure.Unit;

import tec.uom.se.AbstractUnit;

public interface Variable<E extends Enum<E> & Variable<E>> {
  default Unit<?> getUnit() {
    return AbstractUnit.ONE;
  }
}
