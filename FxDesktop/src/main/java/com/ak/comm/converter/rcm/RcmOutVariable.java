package com.ak.comm.converter.rcm;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.measure.Unit;

import com.ak.comm.converter.DependentVariable;
import com.ak.digitalfilter.DigitalFilter;
import com.ak.numbers.Coefficients;
import com.ak.numbers.Interpolators;
import com.ak.numbers.rcm.RcmBaseSurfaceCoefficientsChannel1;
import com.ak.numbers.rcm.RcmBaseSurfaceCoefficientsChannel2;
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
    public List<RcmInVariable> getInputVariables() {
      return Arrays.asList(RcmInVariable.QS_1, RcmInVariable.BASE_1);
    }

    @Override
    public Unit<?> getUnit() {
      return MetricPrefix.MILLI(Units.OHM);
    }

    @Override
    public DigitalFilter filter() {
      return Interpolators.asFilterBuilder(RcmBaseSurfaceCoefficientsChannel1.class).build();
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
  BASE_2 {
    @Override
    public List<RcmInVariable> getInputVariables() {
      return Arrays.asList(RcmInVariable.QS_2, RcmInVariable.BASE_2);
    }

    @Override
    public DigitalFilter filter() {
      return Interpolators.asFilterBuilder(RcmBaseSurfaceCoefficientsChannel2.class).build();
    }
  },
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
    return Interpolators.asFilterBuilder(adcToOhm).build();
  }
}
