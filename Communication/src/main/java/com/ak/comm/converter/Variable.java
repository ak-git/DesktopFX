package com.ak.comm.converter;

import java.util.stream.Stream;

import javax.measure.Unit;

import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.FilterBuilder;
import tec.uom.se.AbstractUnit;

public interface Variable<E extends Enum<E> & Variable<E>> {
  default Unit<?> getUnit() {
    return AbstractUnit.ONE;
  }

  default DigitalFilter filter() {
    return FilterBuilder.of().build();
  }

  default Stream<E> getInputVariables() {
    return Stream.empty();
  }
}
