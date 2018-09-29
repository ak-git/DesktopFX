package com.ak.comm.converter.rcm;

import java.util.Collections;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.measure.Unit;

import com.ak.comm.converter.DependentVariable;
import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.FilterBuilder;
import com.ak.numbers.Coefficients;
import com.ak.numbers.Interpolators;
import com.ak.numbers.rcm.RcmCoefficients;
import tec.uom.se.unit.MetricPrefix;
import tec.uom.se.unit.Units;

public enum RcmOutVariable implements DependentVariable<RcmInVariable, RcmOutVariable> {
  RHEO_1 {
    @Override
    public Unit<?> getUnit() {
      return MetricPrefix.MILLI(Units.OHM);
    }
  },
  BASE_1 {
    @Override
    public Unit<?> getUnit() {
      return Units.OHM;
    }
  },
  QS_1 {
    @Override
    public Unit<?> getUnit() {
      return Units.OHM;
    }

    @Override
    public DigitalFilter filter() {
      return qOsFilter(RcmCoefficients.ADC_TO_OHM_1);
    }

    @Override
    public Set<Option> options() {
      return Collections.singleton(Option.TEXT_VALUE_BANNER);
    }
  },
  ECG {
    @Override
    public Unit<?> getUnit() {
      return MetricPrefix.MILLI(Units.VOLT);
    }
  },
  RHEO_2,
  BASE_2,
  QS_2 {
    @Override
    public DigitalFilter filter() {
      return qOsFilter(RcmCoefficients.ADC_TO_OHM_2);
    }
  };

  @Nonnull
  @Override
  public Class<RcmInVariable> getInputVariablesClass() {
    return RcmInVariable.class;
  }

  private static DigitalFilter qOsFilter(Coefficients adcToOhm) {
    return FilterBuilder.of().operator(Interpolators.interpolator(adcToOhm)).smoothingImpulsive(10).build();
  }
}
