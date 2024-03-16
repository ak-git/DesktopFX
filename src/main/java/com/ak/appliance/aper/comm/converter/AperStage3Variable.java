package com.ak.appliance.aper.comm.converter;

import com.ak.comm.converter.DependentVariable;
import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.FilterBuilder;

public enum AperStage3Variable implements DependentVariable<AperStage2UnitsVariable, AperStage3Variable> {
  R1 {
    @Override
    public DigitalFilter filter() {
      return FilterBuilder.of().smoothingImpulsive(10).build();
    }
  }, R2, R3,
  ECG1, ECG2,
  MYO1, MYO2,
  CCR1 {
    @Override
    public DigitalFilter filter() {
      return R1.filter();
    }
  }, CCR2;

  @Override
  public final Class<AperStage2UnitsVariable> getInputVariablesClass() {
    return AperStage2UnitsVariable.class;
  }
}
