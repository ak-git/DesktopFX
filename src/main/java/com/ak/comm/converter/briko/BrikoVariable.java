package com.ak.comm.converter.briko;

import com.ak.comm.converter.Variable;
import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.FilterBuilder;
import com.ak.util.Numbers;

import javax.measure.Unit;
import java.util.Collections;
import java.util.Set;

import static tec.uom.se.unit.Units.GRAM;

public enum BrikoVariable implements Variable<BrikoVariable> {
  A {
    @Override
    public DigitalFilter filter() {
      return FilterBuilder.of().smoothingImpulsive(10).operator(() -> x -> Numbers.toInt((0.1234 * x - 94374))).build();
    }

    @Override
    public Unit<?> getUnit() {
      return GRAM;
    }
  },
  B {
    @Override
    public DigitalFilter filter() {
      return FilterBuilder.of().smoothingImpulsive(10).operator(() -> x -> x + 2_015_500)
              .operator(() -> x -> Numbers.toInt((0.1235 * x))).build();
    }

    @Override
    public Unit<?> getUnit() {
      return GRAM;
    }
  },
  C {
    @Override
    public Set<Option> options() {
      return Collections.emptySet();
    }
  },
  D {
    @Override
    public Set<Option> options() {
      return Collections.emptySet();
    }
  },
  ENCODER1 {
    @Override
    public Set<Option> options() {
      return Collections.emptySet();
    }
  },
  ENCODER2 {
    @Override
    public Set<Option> options() {
      return Collections.emptySet();
    }
  };

  @Override
  public Set<Option> options() {
    return Variable.Option.addToDefault(Option.TEXT_VALUE_BANNER);
  }
}
