package com.ak.comm.converter.aper.calibration;

import java.util.Collections;
import java.util.EnumSet;
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

    @Override
    public DigitalFilter filter() {
      return FilterBuilder.of().smoothingImpulsive(10).rrs().build();
    }
  },
  PU_ADC_1 {
    @Override
    public DigitalFilter filter() {
      return FilterBuilder.of().smoothingImpulsive(10).rrs().build();
    }
  },
  PU_ADC_2 {
    @Override
    public DigitalFilter filter() {
      return FilterBuilder.of().smoothingImpulsive(10).rrs().build();
    }

    @Override
    public List<AperInVariable> getInputVariables() {
      return Collections.singletonList(AperInVariable.R2);
    }
  },
  PU_1 {
    @Override
    public Set<Option> options() {
      return EnumSet.of(Option.VISIBLE);
    }
  },
  PU_2 {
    @Override
    public Set<Option> options() {
      return EnumSet.of(Option.VISIBLE);
    }

    @Override
    public List<AperInVariable> getInputVariables() {
      return Collections.singletonList(AperInVariable.R2);
    }
  };

  static final AperInVariable VAR_CC = AperInVariable.CCU1;
  static final AperInVariable VAR_PU = AperInVariable.R1;

  @Override
  public final Class<AperInVariable> getInputVariablesClass() {
    return AperInVariable.class;
  }

  @Override
  public List<AperInVariable> getInputVariables() {
    return Collections.singletonList(VAR_PU);
  }

  @Override
  public Set<Option> options() {
    return EnumSet.of(Option.TEXT_VALUE_BANNER);
  }
}
