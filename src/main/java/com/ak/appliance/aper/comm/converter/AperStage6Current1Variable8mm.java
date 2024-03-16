package com.ak.appliance.aper.comm.converter;

import com.ak.comm.converter.DependentVariable;

public enum AperStage6Current1Variable8mm implements DependentVariable<AperStage5Current1Variable, AperStage6Current1Variable8mm> {
  R1,
  MYO1,
  R2,
  MYO2,
  APPARENT_08_24_RHO,
  APPARENT_40_24_RHO,
  CCR;

  @Override
  public final Class<AperStage5Current1Variable> getInputVariablesClass() {
    return AperStage5Current1Variable.class;
  }
}
