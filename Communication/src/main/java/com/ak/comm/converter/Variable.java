package com.ak.comm.converter;

import javax.measure.Unit;

import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.FilterBuilder;
import tec.uom.se.AbstractUnit;

public interface Variable<E extends Enum<E> & Variable<E>> {
  default Unit<?> getUnit() {
    return Variables.tryFindSame(this, e -> e.getUnit(), () -> AbstractUnit.ONE);
  }

  default DigitalFilter filter() {
    return Variables.tryFindSame(this, e -> e.filter(), () -> FilterBuilder.of().build());
  }

  default boolean isVisible() {
    return Variables.tryFindSame(this, e -> e.isVisible(), () -> true);
  }

  String name();

  Class<E> getDeclaringClass();
}
