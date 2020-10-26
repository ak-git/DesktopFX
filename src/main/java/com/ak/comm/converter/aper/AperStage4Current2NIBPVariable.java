package com.ak.comm.converter.aper;

import com.ak.comm.converter.DependentVariable;
import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.FilterBuilder;
import com.ak.numbers.aper.AperRheoCoefficients;

public enum AperStage4Current2NIBPVariable implements DependentVariable<AperStage3Current2Variable, AperStage4Current2NIBPVariable> {
  R1, ECG1, R2, ECG2, CCR1, CCR2;

  @Override
  public final Class<AperStage3Current2Variable> getInputVariablesClass() {
    return AperStage3Current2Variable.class;
  }

  @Override
  public final DigitalFilter filter() {
    return FilterBuilder.of()
        .decimate(AperRheoCoefficients.F_1000_32_187, 4)
        .decimate(AperRheoCoefficients.F_250_32_62, 2)
        .build();
  }
}
