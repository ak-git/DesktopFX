package com.ak.comm.converter.briko;

import java.util.Collections;
import java.util.Set;

import javax.measure.Unit;

import com.ak.comm.converter.Variable;
import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.FilterBuilder;
import tec.uom.se.unit.Units;

public enum BrikoVariable implements Variable<BrikoVariable> {
  AD1,
  AD2,
  HX1 {
    @Override
    public Set<Option> options() {
      return Option.defaultOptions();
    }

    @Override
    public DigitalFilter filter() {
      return FilterBuilder.of().operator(() -> adc -> Math.toIntExact(Math.round(-0.0038 * adc + 1228.3))).build();
    }

    @Override
    public Unit<?> getUnit() {
      return Units.GRAM;
    }
  },
  HX2,
  A1,
  A2;

  @Override
  public Set<Option> options() {
    return Collections.emptySet();
  }
}
