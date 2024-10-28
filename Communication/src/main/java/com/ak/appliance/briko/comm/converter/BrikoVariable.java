package com.ak.appliance.briko.comm.converter;

import com.ak.comm.converter.Variable;
import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.FilterBuilder;
import com.ak.util.Numbers;
import tech.units.indriya.unit.Units;

import javax.measure.MetricPrefix;
import javax.measure.Unit;
import java.util.Set;

public enum BrikoVariable implements Variable<BrikoVariable> {
  C1 {
    @Override
    public Unit<?> getUnit() {
      return MetricPrefix.MICRO(Units.VOLT);
    }

    @Override
    public DigitalFilter filter() {
      return FilterBuilder.of().operator(() -> x -> Numbers.toInt(x * 0.000_1589)).build();
    }

    @Override
    public Set<Option> options() {
      return Variable.Option.addToDefault(Option.TEXT_VALUE_BANNER);
    }
  },
  C2,
  C3,
  C4,
  C5,
  C6,
  C7,
  C8;

  public static final int FREQUENCY = 1000;
}
