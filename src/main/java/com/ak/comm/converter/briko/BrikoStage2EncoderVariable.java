package com.ak.comm.converter.briko;

import com.ak.comm.converter.DependentVariable;
import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.FilterBuilder;
import tec.uom.se.unit.MetricPrefix;

import javax.annotation.Nonnull;
import javax.measure.Unit;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.ak.comm.converter.briko.BrikoStage2Variable.FREQUENCY;
import static tec.uom.se.unit.Units.METRE;

public enum BrikoStage2EncoderVariable implements DependentVariable<BrikoStage1Variable, BrikoStage2EncoderVariable> {
  ENCODER1 {
    @Override
    public DigitalFilter filter() {
      return FilterBuilder.of()
          .average(FREQUENCY / 50).smoothingImpulsive(10)
          .build();
    }

    @Override
    public Set<Option> options() {
      return Collections.emptySet();
    }
  },
  POSITION1 {
    @Override
    public DigitalFilter filter() {
      return FilterBuilder.of().operator(() -> angle -> angle * 4 / 360_000).build();
    }

    @Override
    public List<BrikoStage1Variable> getInputVariables() {
      return List.of(BrikoStage1Variable.ENCODER1);
    }

    @Override
    public Set<Option> options() {
      return Option.addToDefault(Option.TEXT_VALUE_BANNER);
    }

    @Override
    public Unit<?> getUnit() {
      return MetricPrefix.MILLI(METRE);
    }
  };

  @Nonnull
  @Override
  public final Class<BrikoStage1Variable> getInputVariablesClass() {
    return BrikoStage1Variable.class;
  }
}
