package com.ak.comm.converter.aper.sinsin;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.measure.Unit;

import com.ak.comm.converter.DependentVariable;
import com.ak.comm.converter.aper.AperInVariable;
import com.ak.digitalfilter.DigitalFilter;
import com.ak.numbers.aper.sinsin.AperCoefficients;
import com.ak.numbers.aper.sinsin.AperSurfaceCoefficientsChannel1;
import com.ak.numbers.aper.sinsin.AperSurfaceCoefficientsChannel2;
import tec.uom.se.unit.MetricPrefix;
import tec.uom.se.unit.Units;

import static com.ak.comm.converter.aper.AperInVariable.ccrFilter;
import static com.ak.comm.converter.aper.AperInVariable.rheoFilter;

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
      return rheoFilter(AperSurfaceCoefficientsChannel1.class);
    }
  },
  R2 {
    @Override
    public List<AperInVariable> getInputVariables() {
      return Arrays.asList(AperInVariable.CCU1, AperInVariable.R2);
    }

    @Override
    public DigitalFilter filter() {
      return rheoFilter(AperSurfaceCoefficientsChannel2.class);
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
      return ccrFilter(AperCoefficients.ADC_TO_OHM);
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
