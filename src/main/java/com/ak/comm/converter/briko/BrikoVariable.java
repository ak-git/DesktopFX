package com.ak.comm.converter.briko;

import com.ak.comm.converter.Variable;
import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.FilterBuilder;

import javax.measure.Unit;
import java.util.Collections;
import java.util.Set;

import static com.ak.util.Strings.ANGLE;
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
  ENCODER1 {
    @Override
    public DigitalFilter filter() {
      return FilterBuilder.of().angle().build();
    }

    @Override
    public Unit<?> getUnit() {
      return RADIAN.alternate(ANGLE).divide(1000.0);
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
