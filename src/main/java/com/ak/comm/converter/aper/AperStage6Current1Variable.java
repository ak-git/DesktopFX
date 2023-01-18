package com.ak.comm.converter.aper;

import java.util.Set;

import com.ak.comm.converter.DependentVariable;

public enum AperStage6Current1Variable implements DependentVariable<AperStage5Current1Variable, AperStage6Current1Variable> {
  R1,
  CCR;

  @Override
  public final Class<AperStage5Current1Variable> getInputVariablesClass() {
    return AperStage5Current1Variable.class;
  }

  @Override
  public Set<Option> options() {
    return Option.addToDefault(Option.TEXT_VALUE_BANNER);
  }
}
