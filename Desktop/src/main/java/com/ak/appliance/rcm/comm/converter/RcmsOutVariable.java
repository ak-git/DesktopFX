package com.ak.appliance.rcm.comm.converter;

import com.ak.comm.converter.DependentVariable;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public enum RcmsOutVariable implements DependentVariable<RcmOutVariable, RcmsOutVariable> {
  RHEO_1,
  BASE_1,
  ECG,
  RHEO_2,
  BASE_2,
  QS_1 {
    @Override
    public Set<Option> options() {
      return EnumSet.noneOf(Option.class);
    }
  },
  QS_2 {
    @Override
    public List<RcmOutVariable> getInputVariables() {
      return List.of(RcmOutVariable.QS_1);
    }

    @Override
    public Set<Option> options() {
      return Collections.singleton(Option.TEXT_VALUE_BANNER);
    }
  };

  @Override
  public Class<RcmOutVariable> getInputVariablesClass() {
    return RcmOutVariable.class;
  }
}
