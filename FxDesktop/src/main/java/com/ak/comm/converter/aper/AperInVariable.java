package com.ak.comm.converter.aper;

import javax.measure.Unit;

import com.ak.comm.converter.Variable;
import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.FilterBuilder;
import com.ak.numbers.Coefficients;
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
      return FilterBuilder.of().operator(() -> adc -> (int) Math.round((adc - ((1 << 17) * 25)) / 6.0)).build();
    }
  },
  CCU1,
  R2,
  E2,
  CCU2;

  public static DigitalFilter ccrFilter(Coefficients c) {
    return FilterBuilder.asFilterBuilder(c).smoothingImpulsive(10).build();
  }
}
