package com.ak.appliance.aper.comm.converter;

import com.ak.comm.converter.DependentVariable;

public enum AperStage6Current1Variable10mm implements DependentVariable<AperStage5Current1Variable, AperStage6Current1Variable10mm> {
  R1,
  MYO1,
  R2,
  MYO2,
  APPARENT_10_30_RHO,
  APPARENT_50_30_RHO,
  CCR;

  @Override
  public final Class<AperStage5Current1Variable> getInputVariablesClass() {
    return AperStage5Current1Variable.class;
  }
}
