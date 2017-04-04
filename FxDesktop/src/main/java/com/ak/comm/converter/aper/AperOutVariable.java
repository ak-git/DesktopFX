package com.ak.comm.converter.aper;

import java.util.stream.Stream;

import javax.measure.Unit;

import com.ak.comm.converter.DependentVariable;
import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.FilterBuilder;
import com.ak.numbers.Interpolators;
import tec.uom.se.unit.MetricPrefix;
import tec.uom.se.unit.Units;

import static com.ak.numbers.aper.AperCoefficients.RI_VADC_0;
import static com.ak.numbers.aper.AperCoefficients.RI_VADC_15000;

public enum AperOutVariable implements DependentVariable<AperInVariable> {
  R1 {
    @Override
    public Stream<AperInVariable> getInputVariables() {
      return Stream.of(AperInVariable.RI1, AperInVariable.R1);
    }

    @Override
    public Unit<?> getUnit() {
      return MetricPrefix.MILLI(Units.OHM);
    }

    @Override
    public DigitalFilter filter() {
      return FilterBuilder.of().biOperator(Interpolators.interpolator(RI_VADC_0, RI_VADC_15000)).build();
    }
  },
  RI1,

  R2 {
    @Override
    public Stream<AperInVariable> getInputVariables() {
      return Stream.of(AperInVariable.RI2, AperInVariable.R2);
    }

    @Override
    public Unit<?> getUnit() {
      return R1.getUnit();
    }

    @Override
    public DigitalFilter filter() {
      return R1.filter();
    }
  },
  RI2 {
    @Override
    public Unit<?> getUnit() {
      return RI1.getUnit();
    }

    @Override
    public DigitalFilter filter() {
      return RI1.filter();
    }
  };

  @Override
  public final Class<AperInVariable> getInputVariablesClass() {
    return AperInVariable.class;
  }
}
