package com.ak.comm.converter.aper.sinsin;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.measure.Unit;

import com.ak.comm.converter.DependentVariable;
import com.ak.comm.converter.aper.AperInVariable;
import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.FilterBuilder;
import com.ak.numbers.Interpolators;
import com.ak.numbers.aper.sinsin.AperCoefficients;
import com.ak.numbers.aper.sinsin.AperSurfaceCoefficientsChannel1;
import com.ak.numbers.aper.sinsin.AperSurfaceCoefficientsChannel2;
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
      return FilterBuilder.of().biOperator(Interpolators.interpolator(AperSurfaceCoefficientsChannel1.class)).build();
    }
  },
  R2 {
    @Override
    public List<AperInVariable> getInputVariables() {
      return Arrays.asList(AperInVariable.CCU1, AperInVariable.R2);
    }

    @Override
    public DigitalFilter filter() {
      return FilterBuilder.of().biOperator(Interpolators.interpolator(AperSurfaceCoefficientsChannel2.class)).build();
    }
  },
  CCR {
    @Override
    public List<AperInVariable> getInputVariables() {
      return Collections.singletonList(AperInVariable.CCU1);
    }

    @Override
    public Unit<?> getUnit() {
      return Units.OHM;
    }

    @Override
    public DigitalFilter filter() {
      return FilterBuilder.of().operator(Interpolators.interpolator(AperCoefficients.ADC_TO_OHM)).smoothingImpulsive(10).build();
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
}
