package com.ak.comm.converter.aper;

import com.ak.comm.converter.DependentVariable;

public enum AperStage4Current2Variable implements DependentVariable<AperStage3Variable, AperStage4Current2Variable> {
  R1, MYO1, CCR1, R2, MYO2, CCR2;

  @Override
  public final Class<AperStage3Variable> getInputVariablesClass() {
    return AperStage3Variable.class;
  }
}
