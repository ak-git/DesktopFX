package com.ak.comm.converter.briko;

import com.ak.comm.converter.Variable;
import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.FilterBuilder;
import com.ak.util.Numbers;
import tec.uom.se.unit.MetricPrefix;

import javax.measure.Unit;
import java.util.Collections;
import java.util.Set;

import static com.ak.util.Strings.ANGLE;
import static tec.uom.se.unit.Units.METRE;
import static tec.uom.se.unit.Units.RADIAN;

public enum BrikoVariable implements Variable<BrikoVariable> {
  A,
  B,
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
  POSITION {
    @Override
    public DigitalFilter filter() {
      return FilterBuilder.of().angle().operator(() -> a -> Numbers.toInt(a * 4.0 / 360_000)).build();
    }

    @Override
    public Unit<?> getUnit() {
      return MetricPrefix.MILLI(METRE);
    }
  },
  ENCODER {
    @Override
    public Set<Option> options() {
      return Collections.emptySet();
    }

    @Override
    public Unit<?> getUnit() {
      return RADIAN.alternate(ANGLE).divide(1000.0);
    }
  };

  @Override
  public Set<Option> options() {
    return Variable.Option.addToDefault(Option.TEXT_VALUE_BANNER);
  }
}
