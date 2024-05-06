package com.ak.comm.converter;

import tec.uom.se.AbstractUnit;

import javax.measure.Unit;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public interface DependentVariable<I extends Enum<I> & Variable<I>, O extends Enum<O> & Variable<O>> extends Variable<O> {
  Class<I> getInputVariablesClass();

  default List<I> getInputVariables() {
    return Collections.singletonList(Enum.valueOf(getInputVariablesClass(), name()));
  }

  @Override
  default Unit<?> getUnit() {
    return tryFindSame(Variable::getUnit, () -> {
      if (getInputVariables().size() == 1) {
        if (getInputVariables().getFirst() == this) {
          return AbstractUnit.ONE;
        }
        else {
          return getInputVariables().getFirst().getUnit();
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
        if (getInputVariables().getFirst() == this) {
          return Option.defaultOptions();
        }
        else {
          return getInputVariables().getFirst().options();
        }
      }
      else {
        return Option.defaultOptions();
      }
    });
  }
}
