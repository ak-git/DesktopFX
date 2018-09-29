package com.ak.comm.converter;

import java.util.Collections;
import java.util.List;
import java.util.Set;

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
    return tryFindSame(Variable::getUnit, () -> {
      if (getInputVariables().size() == 1) {
        if (getInputVariables().get(0) == this) {
          return AbstractUnit.ONE;
        }
        else {
          return getInputVariables().get(0).getUnit();
        }
      }
      else {
        return AbstractUnit.ONE;
      }
    });
  }

  @Override
  default Set<Option> options() {
    return tryFindSame(Variable::options, () -> {
      if (getInputVariables().size() == 1) {
        if (getInputVariables().get(0) == this) {
          return Option.defaultOptions();
        }
        else {
          return getInputVariables().get(0).options();
        }
      }
      else {
        return Option.defaultOptions();
      }
    });
  }
}
