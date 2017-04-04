package com.ak.comm.converter.aper;

import javax.measure.Unit;

import com.ak.comm.converter.Variable;
import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.FilterBuilder;
import com.ak.numbers.Interpolators;
import com.ak.numbers.aper.AperCoefficients;
import tec.uom.se.unit.MetricPrefix;
import tec.uom.se.unit.Units;

public enum AperInVariable implements Variable {
  R1,
  M1 {
    @Override
    public Unit<?> getUnit() {
      return MetricPrefix.MILLI(Units.VOLT);
    }

    @Override
    public DigitalFilter filter() {
      return FilterBuilder.of().fir(AperCoefficients.MYO).build();
    }
  },
  RI1 {
    @Override
    public Unit<?> getUnit() {
      return Units.OHM;
    }

    @Override
    public DigitalFilter filter() {
      return FilterBuilder.of().operator(Interpolators.interpolator(AperCoefficients.I_ADC_TO_OHM)).build();
    }
  },

  R2,
  M2 {
    @Override
    public Unit<?> getUnit() {
      return M1.getUnit();
    }

    @Override
    public DigitalFilter filter() {
      return M1.filter();
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
  }
}
