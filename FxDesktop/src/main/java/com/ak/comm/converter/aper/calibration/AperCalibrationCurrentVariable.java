package com.ak.comm.converter.aper.calibration;

import java.util.Set;

import com.ak.comm.converter.DependentVariable;
import com.ak.comm.converter.aper.AperInVariable;
import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.FilterBuilder;

public enum AperCalibrationCurrentVariable implements DependentVariable<AperInVariable, AperCalibrationCurrentVariable> {
  CCU1,
  CCU2;

  @Override
  public final DigitalFilter filter() {
    return FilterBuilder.of().smoothingImpulsive(20).build();
  }

  @Override
  public final Class<AperInVariable> getInputVariablesClass() {
    return AperInVariable.class;
  }

  @Override
  public final Set<Option> options() {
    return Option.addToDefault(Option.TEXT_VALUE_BANNER);
  }
}
