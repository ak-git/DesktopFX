package com.ak.comm.converter.aper.sinsin;

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
import tec.uom.se.unit.Units;

public enum AperOutVariable implements DependentVariable<AperInVariable, AperOutVariable> {
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
