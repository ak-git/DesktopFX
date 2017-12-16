package com.ak.comm.converter.aper;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.measure.Unit;

import com.ak.comm.converter.DependentVariable;
import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.FilterBuilder;
import com.ak.numbers.Interpolators;
import com.ak.numbers.aper.AperCoefficients;
import com.ak.numbers.aper.AperSurfaceCoefficients;
import tec.uom.se.unit.MetricPrefix;
import tec.uom.se.unit.Units;

public enum AperOutVariable implements DependentVariable<AperInVariable, AperOutVariable> {
  R1 {
    @Override
    public List<AperInVariable> getInputVariables() {
      return Arrays.asList(AperInVariable.CCU1, AperInVariable.R1);
    }

    @Override
    public Unit<?> getUnit() {
      return MetricPrefix.MILLI(Units.OHM);
    }

    @Override
    public DigitalFilter filter() {
      return FilterBuilder.of().biOperator(Interpolators.interpolator(AperSurfaceCoefficients.class)).
          smoothingImpulsive(10).build();
    }
  },
  ECG1 {
    @Override
    public List<AperInVariable> getInputVariables() {
      return Collections.singletonList(AperInVariable.E1);
    }

    @Override
    public DigitalFilter filter() {
      return FilterBuilder.of().fir(AperCoefficients.ECG).build();
    }
  },
  MYO1 {
    @Override
    public List<AperInVariable> getInputVariables() {
      return Collections.singletonList(AperInVariable.E1);
    }

    @Override
    public DigitalFilter filter() {
      return FilterBuilder.of().fir(AperCoefficients.MYO).comb(1000 / 50).build();
    }
  },
  CCU1 {
    @Override
    public Unit<?> getUnit() {
      return Units.OHM;
    }

    @Override
    public DigitalFilter filter() {
      return FilterBuilder.of().expSum().operator(Interpolators.interpolator(AperCoefficients.ADC_TO_OHM_1)).build();
    }

    @Override
    public Set<Option> options() {
      return Collections.singleton(Option.TEXT_VALUE_BANNER);
    }
  },

  R2 {
    @Override
    public List<AperInVariable> getInputVariables() {
      return Arrays.asList(AperInVariable.CCU2, AperInVariable.R2);
    }
  },
  ECG2 {
    @Override
    public List<AperInVariable> getInputVariables() {
      return Collections.singletonList(AperInVariable.E2);
    }
  },
  MYO2 {
    @Override
    public List<AperInVariable> getInputVariables() {
      return Collections.singletonList(AperInVariable.E2);
    }
  },
  CCU2 {
    @Override
    public DigitalFilter filter() {
      return FilterBuilder.of().expSum().operator(Interpolators.interpolator(AperCoefficients.ADC_TO_OHM_2)).build();
    }
  };

  @Override
  public final Class<AperInVariable> getInputVariablesClass() {
    return AperInVariable.class;
  }
}
