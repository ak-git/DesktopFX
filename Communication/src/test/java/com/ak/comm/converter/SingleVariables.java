package com.ak.comm.converter;

import java.util.Collections;
import java.util.List;

public enum SingleVariables implements DependentVariable<TwoVariables, SingleVariables> {
  E1;

  @Override
  public Class<TwoVariables> getInputVariablesClass() {
    return TwoVariables.class;
  }

  @Override
  public List<TwoVariables> getInputVariables() {
    return Collections.singletonList(TwoVariables.V2);
  }
}
