package com.ak.comm.converter.aper;

import javax.measure.Unit;

import com.ak.comm.converter.Variable;
import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.FilterBuilder;
import com.ak.numbers.common.CommonCoefficients;
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
      return FilterBuilder.of().operator(() -> adc -> (int) Math.round((adc - ((1 << 17) * 25)) / 6.0)).iirMATLAB(
          new double[] {
              0.9022774304591, 0, 0, 0,
              0, 0, 0, 0,
              0, 0, 0, 0,
              0, 0, 0, 0,
              0, 0, 0, 0,
              -0.9022774304591
          },
          new double[] {
              1, 0, 0, 0,
              0, 0, 0, 0,
              0, 0, 0, 0,
              0, 0, 0, 0,
              0, 0, 0, 0,
              -0.8045548609183
          }
      ).fir(CommonCoefficients.MYO).build();
    }
  },
  CCU1,
  R2,
  E2,
  CCU2
}