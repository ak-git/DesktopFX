package com.ak.comm.converter;

import javax.measure.Unit;

import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.FilterBuilder;
import tec.uom.se.AbstractUnit;

public interface Variable<E extends Enum<E> & Variable<E>> {
  default Unit<?> getUnit() {
    return Variables.tryFindSame(name(), getDeclaringClass(), e -> e.getUnit(), () -> AbstractUnit.ONE);
  }

  default DigitalFilter filter() {
    return Variables.tryFindSame(name(), getDeclaringClass(), e -> e.filter(), () -> FilterBuilder.of().build());
  }

  default String toString(int value) {
    return String.format("%s = %d %s", name(), value, getUnit());
  }

  default String toName() {
    return String.format("%s, %s", name(), getUnit());
  }

  String name();

  Class<E> getDeclaringClass();
}
