package com.ak.comm.converter.aper;

import javax.measure.Unit;

import com.ak.comm.converter.Variable;
import com.ak.comm.converter.VariableProperties;
import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.FilterBuilder;
import com.ak.numbers.Interpolators;
import com.ak.numbers.aper.AperCoefficients;
import tec.uom.se.unit.MetricPrefix;
import tec.uom.se.unit.Units;

public enum AperInVariable implements Variable<AperInVariable> {
  R1,
  E1 {
    @Override
    public Unit<?> getUnit() {
      return MetricPrefix.MICRO(Units.VOLT);
    }

    @Override
    public DigitalFilter filter() {
      return FilterBuilder.of().operator(() -> adc -> (int) Math.round((adc - ((1 << 17) * 25)) / 6.5)).build();
    }
  },
  @VariableProperties(display = false)
  RI1 {
    @Override
    public Unit<?> getUnit() {
      return Units.OHM;
    }

    @Override
    public DigitalFilter filter() {
      return FilterBuilder.of().expSum().operator(Interpolators.interpolator(AperCoefficients.I_ADC_TO_OHM)).build();
    }
  },

  R2,
  E2,
  @VariableProperties(display = false)
  RI2
}
