package com.ak.comm.converter.prv_rr;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.measure.Unit;

import com.ak.comm.converter.DependentVariable;
import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.FilterBuilder;
import tec.uom.se.unit.MetricPrefix;
import tec.uom.se.unit.Units;

public enum PrvRROutputVariable implements DependentVariable<PrvRRInputVariable, PrvRROutputVariable> {
  TIME {
    @Override
    public Set<Option> options() {
      return Collections.emptySet();
    }
  },
  RR {
    @Override
    public Unit<?> getUnit() {
      return MetricPrefix.MILLI(Units.SECOND);
    }
  },
  PULSE {
    @Override
    public DigitalFilter filter() {
      return FilterBuilder.of().operator(() -> x -> Math.toIntExact(Math.round(60.0 * 1000 / x))).build();
    }

    @Override
    public List<PrvRRInputVariable> getInputVariables() {
      return List.of(PrvRRInputVariable.RR);
    }
  };

  @Nonnull
  @Override
  public Class<PrvRRInputVariable> getInputVariablesClass() {
    return PrvRRInputVariable.class;
  }
}
