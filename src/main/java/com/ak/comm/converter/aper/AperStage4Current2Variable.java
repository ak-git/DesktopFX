package com.ak.comm.converter.aper;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.ak.comm.converter.DependentVariable;
import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.FilterBuilder;

public enum AperStage4Current2Variable implements DependentVariable<AperStage3Variable, AperStage4Current2Variable> {
  R1, MYO1,
  PK_PK_MYO1 {
    @Override
    public List<AperStage3Variable> getInputVariables() {
      return Collections.singletonList(AperStage3Variable.MYO1);
    }

    @Override
    public DigitalFilter filter() {
      return FilterBuilder.of().peakToPeak(2000).build();
    }

    @Override
    public Set<Option> options() {
      return Collections.singleton(Option.TEXT_VALUE_BANNER);
    }
  },
  CCR1,
  R2, MYO2,
  PK_PK_MYO2 {
    @Override
    public List<AperStage3Variable> getInputVariables() {
      return Collections.singletonList(AperStage3Variable.MYO2);
    }
  },
  CCR2;

  @Override
  public final Class<AperStage3Variable> getInputVariablesClass() {
    return AperStage3Variable.class;
  }
}
