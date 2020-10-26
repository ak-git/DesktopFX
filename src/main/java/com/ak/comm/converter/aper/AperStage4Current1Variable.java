package com.ak.comm.converter.aper;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.ak.comm.converter.DependentVariable;
import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.FilterBuilder;

public enum AperStage4Current1Variable implements DependentVariable<AperStage3Current1Variable, AperStage4Current1Variable> {
  R1,
  R2,
  R3 {
    @Override
    public List<AperStage3Current1Variable> getInputVariables() {
      return Collections.singletonList(AperStage3Current1Variable.R1);
    }

    @Override
    public DigitalFilter filter() {
      return FilterBuilder.of().operator(() -> x -> x * 2).build();
    }
  },
  R4 {
    @Override
    public List<AperStage3Current1Variable> getInputVariables() {
      return Arrays.asList(AperStage3Current1Variable.R2, AperStage3Current1Variable.R1);
    }

    @Override
    public DigitalFilter filter() {
      return FilterBuilder.of().biOperator(() -> (r2, r1) -> (r2 - r1) * 2).build();
    }
  },
  CCR;

  @Override
  public final Class<AperStage3Current1Variable> getInputVariablesClass() {
    return AperStage3Current1Variable.class;
  }
}
