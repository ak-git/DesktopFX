package com.ak.comm.converter.rcm.calibration;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.measure.Unit;

import com.ak.comm.converter.DependentVariable;
import com.ak.comm.converter.rcm.RcmInVariable;
import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.FilterBuilder;
import tec.uom.se.unit.MetricPrefix;
import tec.uom.se.unit.Units;

public enum RcmCalibrationVariable implements DependentVariable<RcmInVariable, RcmCalibrationVariable> {
  RHEO {
    @Override
    public Unit<?> getUnit() {
      return MetricPrefix.MILLI(Units.OHM);
    }

    @Override
    public DigitalFilter filter() {
      return FilterBuilder.of().build();
    }

    @Override
    public Set<Option> options() {
      return EnumSet.of(Option.VISIBLE);
    }

    @Override
    public List<RcmInVariable> getInputVariables() {
      return Collections.singletonList(VAR_RHEO);
    }
  },
  BASE {
    @Override
    public Unit<?> getUnit() {
      return Units.OHM;
    }

    @Override
    public List<RcmInVariable> getInputVariables() {
      return Collections.singletonList(VAR_BASE);
    }
  },
  QS {
    @Override
    public List<RcmInVariable> getInputVariables() {
      return Collections.singletonList(VAR_QS);
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
