package com.ak.comm.converter.aper;

import com.ak.comm.converter.DependentVariable;

public enum AperStage4Current2Variable implements DependentVariable<AperStage3Variable, AperStage4Current2Variable> {
  R1, ECG1, CCR1, R2, ECG2, CCR2;

  @Override
  public final Class<AperStage3Variable> getInputVariablesClass() {
    return AperStage3Variable.class;
  }
}
