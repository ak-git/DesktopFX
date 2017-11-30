package com.ak.comm.converter.aper.calibration;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.ak.comm.converter.DependentVariable;
import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.FilterBuilder;

public enum AperCalibrationCurrentVariable implements DependentVariable<AperVariable, AperCalibrationCurrentVariable> {
  U1,
  VALUE_U1 {
    @Override
    public List<AperVariable> getInputVariables() {
      return Collections.singletonList(AperVariable.U1);
    }

    @Override
    public DigitalFilter filter() {
      return FilterBuilder.of().rrs(1000).build();
    }

    @Override
    public Set<Option> options() {
      return Collections.singleton(Option.TEXT_VALUE_BANNER);
    }
  },
  U2,
  VALUE_U2 {
    @Override
    public List<AperVariable> getInputVariables() {
      return Collections.singletonList(AperVariable.U2);
    }
  };

  @Override
  public final Class<AperVariable> getInputVariablesClass() {
    return AperVariable.class;
  }
}
