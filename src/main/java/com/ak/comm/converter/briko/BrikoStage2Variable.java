package com.ak.comm.converter.briko;

import com.ak.comm.converter.DependentVariable;
import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.FilterBuilder;
import com.ak.util.Numbers;

import javax.annotation.Nonnull;
import javax.measure.Unit;
import java.util.Set;

import static tec.uom.se.unit.Units.GRAM;

public enum BrikoStage2Variable implements DependentVariable<BrikoStage1Variable, BrikoStage2Variable> {
  FORCE1 {
    @Override
    public DigitalFilter filter() {
      return FilterBuilder.of()
              .operator(() -> x -> Numbers.toInt((0.1234 * x - 94_374)))
              .average(FREQUENCY / 50).smoothingImpulsive(10).autoZero()
          .build();
    }
  },
  FORCE2 {
    @Override
    public DigitalFilter filter() {
      return FilterBuilder.of()
              .operator(() -> x -> x + 2_015_500).operator(() -> x -> Numbers.toInt((0.1235 * x)))
              .average(FREQUENCY / 50).smoothingImpulsive(10).autoZero()
          .build();
    }
  };

  public static final int FREQUENCY = 1000;

  @Nonnull
  @Override
  public final Class<BrikoStage1Variable> getInputVariablesClass() {
    return BrikoStage1Variable.class;
  }

  @Override
  public final Set<Option> options() {
    return Option.addToDefault(Option.TEXT_VALUE_BANNER);
  }

  @Override
  public final Unit<?> getUnit() {
    return GRAM;
  }
}
