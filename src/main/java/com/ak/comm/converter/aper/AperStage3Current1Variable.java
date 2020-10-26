package com.ak.comm.converter.aper;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.ak.comm.converter.DependentVariable;
import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.FilterBuilder;
import com.ak.numbers.aper.AperRheoCoefficients;

public enum AperStage3Current1Variable implements DependentVariable<AperStage2UnitsVariable, AperStage3Current1Variable> {
  R1,
  R2 {
    @Override
    public List<AperStage2UnitsVariable> getInputVariables() {
      return Collections.singletonList(AperStage2UnitsVariable.R3);
    }
  },
  CCR {
    @Override
    public List<AperStage2UnitsVariable> getInputVariables() {
      return Collections.singletonList(AperStage2UnitsVariable.CCR1);
    }

    @Override
    public Set<Option> options() {
      return Collections.singleton(Option.TEXT_VALUE_BANNER);
    }
  };

  @Override
  public final Class<AperStage2UnitsVariable> getInputVariablesClass() {
    return AperStage2UnitsVariable.class;
  }

  @Override
  public final DigitalFilter filter() {
    return FilterBuilder.of()
        .decimate(AperRheoCoefficients.F_1000_32_187, 4)
        .decimate(AperRheoCoefficients.F_250_32_62, 2)
        .smoothingImpulsive(4)
        .interpolate(2, AperRheoCoefficients.F_250_32_62)
        .interpolate(4, AperRheoCoefficients.F_1000_32_187)
        .build();
  }
}
