package com.ak.comm.converter.aper.myo;

import java.util.Collections;
import java.util.List;

import com.ak.comm.converter.DependentVariable;
import com.ak.comm.converter.aper.AperInVariable;
import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.FilterBuilder;
import com.ak.numbers.aper.AperCoefficients;

public enum AperEMGVariable implements DependentVariable<AperInVariable, AperEMGVariable> {
  M1 {
    @Override
    public List<AperInVariable> getInputVariables() {
      return Collections.singletonList(AperInVariable.E1);
    }

    @Override
    public DigitalFilter filter() {
      return FilterBuilder.of().fir(AperCoefficients.MYO).comb(1000 / 50).build();
    }
  },
  RI1,
  M2 {
    @Override
    public List<AperInVariable> getInputVariables() {
      return Collections.singletonList(AperInVariable.E2);
    }
  },
  RI2;

  @Override
  public final Class<AperInVariable> getInputVariablesClass() {
    return AperInVariable.class;
  }
}
