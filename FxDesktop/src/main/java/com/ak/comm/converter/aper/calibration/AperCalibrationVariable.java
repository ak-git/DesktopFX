package com.ak.comm.converter.aper.calibration;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.ak.comm.converter.DependentVariable;
import com.ak.comm.converter.aper.AperInVariable;
import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.FilterBuilder;

public enum AperCalibrationVariable implements DependentVariable<AperInVariable, AperCalibrationVariable> {
  CC_ADC {
    @Override
    public List<AperInVariable> getInputVariables() {
      return Collections.singletonList(VAR_CC);
    }
  },
  PU_ADC {
    @Override
    public List<AperInVariable> getInputVariables() {
      return Collections.singletonList(VAR_PU);
    }
  };

  public static final AperInVariable VAR_CC = AperInVariable.CCU1;
  public static final AperInVariable VAR_PU = AperInVariable.R1;

  @Override
  public final Class<AperInVariable> getInputVariablesClass() {
    return AperInVariable.class;
  }

  @Override
  public final DigitalFilter filter() {
    return FilterBuilder.of().smoothingImpulsive(20).build();
  }

  @Override
  public final Set<Option> options() {
    return Option.addToDefault(Option.TEXT_VALUE_BANNER);
  }
}
