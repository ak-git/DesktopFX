package com.ak.comm.converter.briko;

import com.ak.comm.converter.Variable;
import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.FilterBuilder;
import com.ak.util.Numbers;

import javax.measure.Unit;

import static tec.uom.se.unit.MetricPrefix.MILLI;
import static tec.uom.se.unit.Units.GRAM;
import static tec.uom.se.unit.Units.METRE;

public enum BrikoStage1Variable implements Variable<BrikoStage1Variable> {
  FORCE1 {
    @Override
    public DigitalFilter filter() {
      return FilterBuilder.of().operator(() -> x -> Numbers.toInt((0.1293 * x - 543.43)))
          .average(FREQUENCY / 50).smoothingImpulsive(10).autoZero(FREQUENCY).build();
    }

    @Override
    public Unit<?> getUnit() {
      return GRAM;
    }
  },
  FORCE2 {
    @Override
    public DigitalFilter filter() {
      return FilterBuilder.of().operator(() -> x -> Numbers.toInt((0.1241 * x - 3683.1)))
          .average(FREQUENCY / 50).smoothingImpulsive(10).autoZero(FREQUENCY).build();
    }
  },
  FORCE3,
  FORCE4,
  POSITION {
    @Override
    public DigitalFilter filter() {
      return FilterBuilder.of().operator(() -> x -> 4 * x / 1600).autoZero(FREQUENCY).build();
    }

    @Override
    public Unit<?> getUnit() {
      return MILLI(METRE);
    }
  },
  IGNORE;

  public static final int FREQUENCY = 1000;
}
