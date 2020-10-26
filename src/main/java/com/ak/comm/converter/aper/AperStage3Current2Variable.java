package com.ak.comm.converter.aper;

import java.util.Collections;
import java.util.Set;

import com.ak.comm.converter.DependentVariable;
import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.FilterBuilder;
import com.ak.numbers.common.CommonCoefficients;

public enum AperStage3Current2Variable implements DependentVariable<AperStage2UnitsVariable, AperStage3Current2Variable> {
  R1,
  ECG1 {
    @Override
    public DigitalFilter filter() {
      return FilterBuilder.of().fir(CommonCoefficients.ECG).build();
    }
  },
  CCR1 {
    @Override
    public Set<Option> options() {
      return Collections.singleton(Option.TEXT_VALUE_BANNER);
    }
  },
  R2, ECG2, CCR2;

  @Override
  public final Class<AperStage2UnitsVariable> getInputVariablesClass() {
    return AperStage2UnitsVariable.class;
  }
}
