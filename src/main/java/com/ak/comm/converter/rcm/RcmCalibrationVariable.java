package com.ak.comm.converter.rcm;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

import com.ak.comm.converter.DependentVariable;
import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.FilterBuilder;

public enum RcmCalibrationVariable implements DependentVariable<RcmInVariable, RcmCalibrationVariable> {
  CC_ADC {
    @Override
    public List<RcmInVariable> getInputVariables() {
      return Collections.singletonList(VAR_QS);
    }
  },
  BASE_ADC {
    @Override
    public List<RcmInVariable> getInputVariables() {
      return Collections.singletonList(VAR_BASE);
    }
  },
  RHEO_ADC {
    @Override
    public List<RcmInVariable> getInputVariables() {
      return Collections.singletonList(VAR_RHEO);
    }

    @Override
    public DigitalFilter filter() {
      return FilterBuilder.of().build();
    }

    @Override
    public Set<Option> options() {
      return EnumSet.of(Option.VISIBLE);
    }
  },
  AVG_RHEO_ADC {
    @Override
    public List<RcmInVariable> getInputVariables() {
      return Collections.singletonList(VAR_RHEO);
    }

    @Override
    public DigitalFilter filter() {
      return FilterBuilder.of().rrs().build();
    }

    @Override
    public Set<Option> options() {
      return EnumSet.of(Option.TEXT_VALUE_BANNER);
    }
  },
  MIN_RHEO_ADC {
    @Override
    public List<RcmInVariable> getInputVariables() {
      return Collections.singletonList(VAR_RHEO);
    }

    @Override
    public DigitalFilter filter() {
      return FilterBuilder.of().peakToPeak(400).build();
    }

    @Override
    public Set<Option> options() {
      return EnumSet.of(Option.TEXT_VALUE_BANNER);
    }
  };

  @Override
  public DigitalFilter filter() {
    return FilterBuilder.of().rrs().build();
  }

  @Override
  public Set<Option> options() {
    return Option.addToDefault(Option.TEXT_VALUE_BANNER);
  }

  @Nonnull
  @Override
  public Class<RcmInVariable> getInputVariablesClass() {
    return RcmInVariable.class;
  }

  static final RcmInVariable VAR_RHEO = RcmInVariable.RHEO_1;
  static final RcmInVariable VAR_BASE = RcmInVariable.BASE_1;
  static final RcmInVariable VAR_QS = RcmInVariable.QS_1;
}
