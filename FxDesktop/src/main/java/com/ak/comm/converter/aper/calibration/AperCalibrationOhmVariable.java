package com.ak.comm.converter.aper.calibration;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.measure.Unit;

import com.ak.comm.converter.DependentVariable;
import com.ak.comm.converter.aper.AperInVariable;
import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.FilterBuilder;
import com.ak.numbers.Interpolators;
import com.ak.numbers.aper.AperCoefficients;
import tec.uom.se.unit.Units;

public enum AperCalibrationOhmVariable implements DependentVariable<AperInVariable, AperCalibrationOhmVariable> {
  R {
    @Override
    public List<AperInVariable> getInputVariables() {
      return Collections.singletonList(AperInVariable.R1);
    }

    @Override
    public DigitalFilter filter() {
      return FilterBuilder.of().smoothingImpulsive(20).rrs(2000).build();
    }
  },
  STD_R {
    @Override
    public List<AperInVariable> getInputVariables() {
      return Collections.singletonList(AperInVariable.R1);
    }

    @Override
    public DigitalFilter filter() {
      return FilterBuilder.of().std(2000).smoothingImpulsive(20).build();
    }

    @Override
    public Set<Option> options() {
      return Collections.singleton(Option.TEXT_VALUE_BANNER);
    }
  },
  U {
    @Override
    public List<AperInVariable> getInputVariables() {
      return Collections.singletonList(AperInVariable.CCU1);
    }

    @Override
    public DigitalFilter filter() {
      return FilterBuilder.of().smoothingImpulsive(20).rrs(2000).build();
    }
  },
  RI {
    @Override
    public Unit<?> getUnit() {
      return Units.OHM;
    }

    @Override
    public List<AperInVariable> getInputVariables() {
      return Collections.singletonList(AperInVariable.CCU1);
    }

    @Override
    public DigitalFilter filter() {
      return FilterBuilder.of().smoothingImpulsive(20).operator(Interpolators.interpolator(AperCoefficients.ADC_TO_OHM_1)).build();
    }

    @Override
    public Set<Option> options() {
      return Collections.singleton(Option.TEXT_VALUE_BANNER);
    }
  };

  @Override
  public final Class<AperInVariable> getInputVariablesClass() {
    return AperInVariable.class;
  }

  @Override
  public Set<Option> options() {
    return Option.addToDefault(Option.TEXT_VALUE_BANNER);
  }
}
