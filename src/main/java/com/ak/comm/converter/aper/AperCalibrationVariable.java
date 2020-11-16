package com.ak.comm.converter.aper;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import com.ak.comm.converter.DependentVariable;
import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.FilterBuilder;

public enum AperCalibrationVariable implements DependentVariable<AperStage1Variable, AperCalibrationVariable> {
  CC_ADC {
    @Override
    public List<AperStage1Variable> getInputVariables() {
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
    public List<AperStage1Variable> getInputVariables() {
      return Collections.singletonList(AperStage1Variable.R2);
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
    public List<AperStage1Variable> getInputVariables() {
      return Collections.singletonList(AperStage1Variable.R2);
    }
  };

  static final AperStage1Variable VAR_CC = AperStage1Variable.CCU1;
  static final AperStage1Variable VAR_PU = AperStage1Variable.R1;

  @Override
  public final Class<AperStage1Variable> getInputVariablesClass() {
    return AperStage1Variable.class;
  }

  @Override
  public List<AperStage1Variable> getInputVariables() {
    return Collections.singletonList(VAR_PU);
  }

  @Override
  public Set<Option> options() {
    return EnumSet.of(Option.TEXT_VALUE_BANNER);
  }
}
