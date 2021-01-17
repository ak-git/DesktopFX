package com.ak.comm.converter.aper;

import java.util.Collections;
import java.util.List;

import com.ak.comm.converter.DependentVariable;
import com.ak.digitalfilter.DigitalFilter;

public enum AperStage3Current2NIBPVariable implements DependentVariable<AperStage2UnitsVariable, AperStage3Current2NIBPVariable> {
  R1, R2,
  ECG {
    @Override
    public List<AperStage2UnitsVariable> getInputVariables() {
      return Collections.singletonList(AperStage2UnitsVariable.ECG2);
    }
  }, CCR1, CCR2;

  @Override
  public final Class<AperStage2UnitsVariable> getInputVariablesClass() {
    return AperStage2UnitsVariable.class;
  }

  @Override
  public final DigitalFilter filter() {
    return AperStage3Current1NIBPVariable.CCR.filter();
  }
}
