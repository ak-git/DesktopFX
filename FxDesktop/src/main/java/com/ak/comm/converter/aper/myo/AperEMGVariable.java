package com.ak.comm.converter.aper.myo;

import java.util.stream.Stream;

import com.ak.comm.converter.DependentVariable;
import com.ak.comm.converter.VariableProperties;
import com.ak.comm.converter.aper.AperInVariable;
import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.FilterBuilder;
import com.ak.numbers.aper.AperCoefficients;

public enum AperEMGVariable implements DependentVariable<AperInVariable, AperEMGVariable> {
  M1 {
    @Override
    public Stream<AperInVariable> getInputVariables() {
      return Stream.of(AperInVariable.E1);
    }

    @Override
    public DigitalFilter filter() {
      return FilterBuilder.of().fir(AperCoefficients.MYO).build();
    }
  },
  @VariableProperties(display = false)
  RI1,
  M2 {
    @Override
    public Stream<AperInVariable> getInputVariables() {
      return Stream.of(AperInVariable.E2);
    }
  },
  @VariableProperties(display = false)
  RI2;

  @Override
  public final Class<AperInVariable> getInputVariablesClass() {
    return AperInVariable.class;
  }
}
