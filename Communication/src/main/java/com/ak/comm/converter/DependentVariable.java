package com.ak.comm.converter;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.measure.Unit;

import tec.uom.se.AbstractUnit;

public interface DependentVariable<IN extends Enum<IN> & Variable<IN>, OUT extends Enum<OUT> & Variable<OUT>> extends Variable<OUT> {
  @Nonnull
  Class<IN> getInputVariablesClass();

  default List<IN> getInputVariables() {
    return Collections.singletonList(Enum.valueOf(getInputVariablesClass(), name()));
  }

  @Override
  default Unit<?> getUnit() {
    return tryFindSame(out -> out.getUnit(), () -> {
      if (getInputVariables().size() == 1) {
        return getInputVariables().get(0).getUnit();
      }
      else {
        return AbstractUnit.ONE;
      }
    });
  }

  @Override
  default boolean isVisible() {
    return tryFindSame(out -> out.isVisible(), () -> getInputVariables().size() != 1 || getInputVariables().get(0).isVisible());
  }
}
