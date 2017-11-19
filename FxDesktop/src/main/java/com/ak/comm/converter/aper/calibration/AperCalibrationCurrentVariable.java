package com.ak.comm.converter.aper.calibration;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.ak.comm.converter.DependentVariable;
import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.FilterBuilder;

public enum AperCalibrationCurrentVariable implements DependentVariable<AperVariable, AperCalibrationCurrentVariable> {
  RI1 {
    @Override
    public DigitalFilter filter() {
      return FilterBuilder.of().expSum().build();
    }

    @Override
    public Set<Option> options() {
      return Option.addToDefault(Option.TEXT_VALUE_BANNER);
    }
  },
  STD_RI1 {
    @Override
    public List<AperVariable> getInputVariables() {
      return Collections.singletonList(AperVariable.RI1);
    }

    @Override
    public DigitalFilter filter() {
      return FilterBuilder.of().std(1000).build();
    }

    @Override
    public Set<Option> options() {
      return Collections.singleton(Option.TEXT_VALUE_BANNER);
    }
  },
  RI2,
  STD_RI2 {
    @Override
    public List<AperVariable> getInputVariables() {
      return Collections.singletonList(AperVariable.RI2);
    }
  };

  @Override
  public final Class<AperVariable> getInputVariablesClass() {
    return AperVariable.class;
  }
}
