package com.ak.comm.converter.aper;

import com.ak.comm.converter.DependentVariable;
import com.ak.digitalfilter.DigitalFilter;

public enum AperStage5Current1Variable implements DependentVariable<AperStage4Current1Variable, AperStage5Current1Variable> {
  R1,
  R2,
  CCR;

  @Override
  public final DigitalFilter filter() {
    return DependentVariable.super.filter();
  }

  @Override
  public final Class<AperStage4Current1Variable> getInputVariablesClass() {
    return AperStage4Current1Variable.class;
  }
}
