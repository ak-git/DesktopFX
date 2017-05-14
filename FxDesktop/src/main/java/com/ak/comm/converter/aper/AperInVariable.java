package com.ak.comm.converter.aper;

import java.util.Optional;

import javax.annotation.Nullable;
import javax.measure.Unit;

import com.ak.comm.converter.ADCVariable;
import com.ak.comm.converter.Variable;
import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.FilterBuilder;
import com.ak.numbers.Interpolators;
import com.ak.numbers.aper.AperCoefficients;
import tec.uom.se.unit.MetricPrefix;
import tec.uom.se.unit.Units;

public enum AperInVariable implements Variable {
  R1(null),
  E1(null) {
    @Override
    public Unit<?> getUnit() {
      return MetricPrefix.MILLI(Units.VOLT);
    }
  },
  RI1(null) {
    @Override
    public Unit<?> getUnit() {
      return Units.OHM;
    }

    @Override
    public DigitalFilter filter() {
      return FilterBuilder.of().operator(Interpolators.interpolator(AperCoefficients.I_ADC_TO_OHM)).build();
    }
  },

  R2(R1),
  E2(E1),
  RI2(RI1);

  @Nullable
  private final Variable analog;

  AperInVariable(@Nullable Variable analog) {
    this.analog = analog;
  }


  @Override
  public Unit<?> getUnit() {
    return analog().getUnit();
  }

  @Override
  public DigitalFilter filter() {
    return analog().filter();
  }

  private Variable analog() {
    return Optional.ofNullable(analog).orElse(ADCVariable.ADC);
  }
}
