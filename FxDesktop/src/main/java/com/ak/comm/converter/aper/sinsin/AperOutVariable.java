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
import com.ak.numbers.aper.AperRheoCoefficients;
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
      return AperOutVariable.filter(FilterBuilder.asFilterBuilder(AperSurfaceCoefficientsChannel1.class));
    }
  },
  R2 {
    @Override
    public List<AperInVariable> getInputVariables() {
      return Arrays.asList(AperInVariable.CCU1, AperInVariable.R2);
    }

    @Override
    public DigitalFilter filter() {
      return AperOutVariable.filter(FilterBuilder.asFilterBuilder(AperSurfaceCoefficientsChannel2.class));
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
      return AperOutVariable.filter(FilterBuilder.asFilterBuilder(AperCoefficients.ADC_TO_OHM));
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

  private static DigitalFilter filter(FilterBuilder filterBuilder) {
    return filterBuilder.smoothingImpulsive(12)
        .decimate(AperRheoCoefficients.F_1000_32_200, 4)
        .decimate(AperRheoCoefficients.F_250_32_75, 2)
        .fir(AperRheoCoefficients.F_125_32_50)
        .interpolate(2, AperRheoCoefficients.F_250_32_75)
        .interpolate(4, AperRheoCoefficients.F_1000_32_200).build();
  }
}
