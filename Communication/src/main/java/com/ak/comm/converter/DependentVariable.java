package com.ak.comm.converter;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.measure.Unit;

import tec.uom.se.AbstractUnit;

public interface DependentVariable<IN extends Enum<IN> & Variable<IN>, OUT extends Enum<OUT> & Variable<OUT>> extends Variable<OUT> {
  @Nonnull
  Class<IN> getInputVariablesClass();

  default Stream<IN> getInputVariables() {
    return Stream.of(Enum.valueOf(getInputVariablesClass(), name()));
  }

  @Override
  default Unit<?> getUnit() {
    return Variables.tryFindSame(name(), getDeclaringClass(), out -> out.getUnit(), () -> {
      List<IN> inputVars = getInputVariables().collect(Collectors.toList());
      if (inputVars.size() == 1) {
        return inputVars.get(0).getUnit();
      }
      else {
        return AbstractUnit.ONE;
      }
    });
  }

  @Override
  default boolean isVisible() {
    return Variables.tryFindSame(name(), getDeclaringClass(), out -> out.isVisible(), () -> {
      List<IN> inputVars = getInputVariables().collect(Collectors.toList());
      return inputVars.size() != 1 || inputVars.get(0).isVisible();
    });
  }
}
