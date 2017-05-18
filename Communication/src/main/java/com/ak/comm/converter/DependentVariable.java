package com.ak.comm.converter;

import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.measure.Unit;

import tec.uom.se.AbstractUnit;

public interface DependentVariable<IN extends Enum<IN> & Variable> extends Variable {
  @Nonnull
  Class<IN> getInputVariablesClass();

  default Stream<IN> getInputVariables() {
    return Stream.of(Enum.valueOf(getInputVariablesClass(), name()));
  }

  @Override
  default Unit<?> getUnit() {
    try {
      if (getInputVariables().count() == 1) {
        return getInputVariables().findFirst().map(in -> in.getUnit()).orElseThrow(IllegalArgumentException::new);
      }
      else {
        return AbstractUnit.ONE;
      }
    }
    catch (IllegalArgumentException e) {
      return AbstractUnit.ONE;
    }
  }
}
