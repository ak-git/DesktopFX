package com.ak.comm.converter.aper.calibration;

import com.ak.comm.converter.DependentVariable;
import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.FilterBuilder;

public enum AperCalibrationCurrentVariable implements DependentVariable<AperVariable, AperCalibrationCurrentVariable> {
  U1,
  U2;

  @Override
  public final DigitalFilter filter() {
    return FilterBuilder.of().smoothingImpulsive(20).build();
  }

  @Override
  public final Class<AperVariable> getInputVariablesClass() {
    return AperVariable.class;
  }
}
