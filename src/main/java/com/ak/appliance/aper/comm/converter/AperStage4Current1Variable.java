package com.ak.appliance.aper.comm.converter;

import com.ak.comm.converter.DependentVariable;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public enum AperStage4Current1Variable implements DependentVariable<AperStage3Variable, AperStage4Current1Variable> {
  R1,
  MYO1,
  R2 {
    @Override
    public List<AperStage3Variable> getInputVariables() {
      return Collections.singletonList(AperStage3Variable.R3);
    }
  },
  MYO2,
  CCR {
    @Override
    public List<AperStage3Variable> getInputVariables() {
      return Collections.singletonList(AperStage3Variable.CCR1);
    }

    @Override
    public Set<Option> options() {
      return Collections.singleton(Option.TEXT_VALUE_BANNER);
    }
  };

  @Override
  public final Class<AperStage3Variable> getInputVariablesClass() {
    return AperStage3Variable.class;
  }
}
