package com.ak.comm.converter.aper;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.ak.comm.converter.DependentVariable;
import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.FilterBuilder;

public enum Aper2OutVariable implements DependentVariable<AperOutVariable, Aper2OutVariable> {
  R1 {
    @Override
    public List<AperOutVariable> getInputVariables() {
      return Collections.singletonList(AperOutVariable.R1);
    }

    @Override
    public DigitalFilter filter() {
      return FilterBuilder.of().operator(() -> x -> x * 2).build();
    }
  },
  R2 {
    @Override
    public List<AperOutVariable> getInputVariables() {
      return Arrays.asList(AperOutVariable.R2, AperOutVariable.R1);
    }

    @Override
    public DigitalFilter filter() {
      return FilterBuilder.of().biOperator(() -> (r2, r1) -> (r2 - r1) * 2).build();
    }
  },
  CCR {
    @Override
    public List<AperOutVariable> getInputVariables() {
      return Collections.singletonList(AperOutVariable.CCR);
    }

    @Override
    public Set<Option> options() {
      return Collections.singleton(Option.TEXT_VALUE_BANNER);
    }
  };

  @Override
  public final Class<AperOutVariable> getInputVariablesClass() {
    return AperOutVariable.class;
  }
}
