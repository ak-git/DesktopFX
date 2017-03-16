package com.ak.comm.converter;

import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.measure.Unit;

public interface DependentVariable<IN extends Enum<IN> & Variable> extends Variable {
  @Nonnull
  String name();

  @Nonnull
  Class<IN> getInputVariablesClass();

  default Stream<IN> getInputVariables() {
    return Stream.of(Enum.valueOf(getInputVariablesClass(), name()));
  }

  @Override
  default Unit<?> getUnit() {
    return Enum.valueOf(getInputVariablesClass(), name()).getUnit();
  }
}
