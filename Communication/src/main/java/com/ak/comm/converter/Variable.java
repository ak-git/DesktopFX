package com.ak.comm.converter;

import javax.measure.Unit;

import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.FilterBuilder;
import tec.uom.se.AbstractUnit;

public interface Variable {
  default Unit<?> getUnit() {
    return AbstractUnit.ONE;
  }

  default DigitalFilter filter() {
    return FilterBuilder.of().build();
  }

  default String toString(int value) {
    return String.format("%s = %d %s", this, value, getUnit());
  }
}
