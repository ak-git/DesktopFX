package com.ak.comm.converter.aper.calibration;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.ak.comm.converter.DependentVariable;
import com.ak.comm.converter.aper.AperInVariable;
import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.FilterBuilder;

public enum AperCalibrationOhmVariable implements DependentVariable<AperInVariable, AperCalibrationOhmVariable> {
  ADC_PUMP_U {
    @Override
    public List<AperInVariable> getInputVariables() {
      return Collections.singletonList(VAR_CCU);
    }

    @Override
    public DigitalFilter filter() {
      return FilterBuilder.of().smoothingImpulsive(20).rrs(2000).build();
    }
  },
  ADC_R {
    @Override
    public List<AperInVariable> getInputVariables() {
      return Collections.singletonList(VAR_R);
    }

    @Override
    public DigitalFilter filter() {
      return FilterBuilder.of().smoothingImpulsive(20).rrs(2000).build();
    }
  };

  public static final AperInVariable VAR_R = AperInVariable.R1;
  public static final AperInVariable VAR_CCU = AperInVariable.CCU1;

  @Override
  public final Class<AperInVariable> getInputVariablesClass() {
    return AperInVariable.class;
  }

  @Override
  public Set<Option> options() {
    return Option.addToDefault(Option.TEXT_VALUE_BANNER);
  }
}
