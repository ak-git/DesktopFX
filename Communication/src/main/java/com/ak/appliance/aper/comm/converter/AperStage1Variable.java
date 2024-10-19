package com.ak.appliance.aper.comm.converter;

import com.ak.comm.converter.Variable;
import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.FilterBuilder;
import tech.units.indriya.unit.Units;

import javax.measure.MetricPrefix;
import javax.measure.Unit;

public enum AperStage1Variable implements Variable<AperStage1Variable> {
  R1,
  E1 {
    @Override
    public Unit<?> getUnit() {
      return MetricPrefix.MICRO(Units.VOLT);
    }

    @Override
    public DigitalFilter filter() {
      return FilterBuilder.of().operator(() -> adc -> (int) Math.round((adc - ((1 << 17) * 25)) * 0.2223218)).build();
    }
  },
  CCU1,
  R2,
  E2,
  CCU2
}
