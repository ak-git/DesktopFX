package com.ak.comm.converter.aper;

import com.ak.comm.converter.DependentVariable;
import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.FilterBuilder;
import com.ak.numbers.aper.AperRheoCoefficients;

public enum AperStage3Variable implements DependentVariable<AperStage2UnitsVariable, AperStage3Variable> {
  R1 {
    @Override
    public DigitalFilter filter() {
      return FilterBuilder.of()
          .decimate(AperRheoCoefficients.F_1000_32_187, 4)
          .decimate(AperRheoCoefficients.F_250_32_62, 2)
          .smoothingImpulsive(4)
          .interpolate(2, AperRheoCoefficients.F_250_32_62)
          .interpolate(4, AperRheoCoefficients.F_1000_32_187)
          .build();
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
