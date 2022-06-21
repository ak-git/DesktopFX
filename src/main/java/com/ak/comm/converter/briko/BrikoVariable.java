package com.ak.comm.converter.briko;

import java.util.Collections;
import java.util.Set;

import javax.measure.Unit;

import com.ak.comm.converter.Variable;
import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.FilterBuilder;
import com.ak.util.Metrics;
import tec.uom.se.unit.Units;

public enum BrikoVariable implements Variable<BrikoVariable> {
  A {
    @Override
    public Set<Option> options() {
      return Option.defaultOptions();
    }

    @Override
    public DigitalFilter filter() {
      return FilterBuilder.of().smoothingImpulsive(10)
          .operator(() -> gram -> Math.max(Math.toIntExact(Math.round(gram / 1000.0 / (Metrics.fromMilli(100.0) * Metrics.fromMilli(60.0)))), 0))
          .build();
    }

    @Override
    public Unit<?> getUnit() {
      return Units.PASCAL;
    }

  },
  B,
  C,
  D,
  E,
  F;

  @Override
  public Set<Option> options() {
    return Collections.emptySet();
  }
}
