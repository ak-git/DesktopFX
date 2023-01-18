package com.ak.comm.converter.aper;

import com.ak.comm.converter.DependentVariable;

public enum AperStage6Current1Variable6mm implements DependentVariable<AperStage5Current1Variable, AperStage6Current1Variable6mm> {
  R1,
  MYO1,
  R2,
  MYO2,
  APPARENT_06_18_RHO,
  APPARENT_30_18_RHO,
  CCR;

  @Override
  public final Class<AperStage5Current1Variable> getInputVariablesClass() {
    return AperStage5Current1Variable.class;
  }
}
