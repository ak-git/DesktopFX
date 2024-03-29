package com.ak.appliance.aper.comm.converter;

import com.ak.comm.converter.DependentVariable;

public enum AperStage6Current1Variable7mm implements DependentVariable<AperStage5Current1Variable, AperStage6Current1Variable7mm> {
  R1,
  MYO1,
  R2,
  MYO2,
  APPARENT_07_21_RHO,
  APPARENT_35_21_RHO,
  CCR;

  @Override
  public final Class<AperStage5Current1Variable> getInputVariablesClass() {
    return AperStage5Current1Variable.class;
  }
}
