package com.ak.comm.converter.briko;

import com.ak.comm.converter.DependentVariable;
import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.FilterBuilder;
import com.ak.util.Metrics;
import com.ak.util.Numbers;
import tec.uom.se.unit.Units;

import javax.annotation.Nonnull;
import javax.measure.Unit;
import java.util.List;
import java.util.Set;

public enum BrikoStage2Variable implements DependentVariable<BrikoStage1Variable, BrikoStage2Variable> {
  FORCE1,
  FORCE2,
  POSITION,
  PRESSURE {
    @Override
    public DigitalFilter filter() {
      return FilterBuilder.of()
          .biOperator(() -> (force1, force2) -> {
            double newtons = (Math.abs(force1) + Math.abs(force2)) / 100.0;
            double radius = Metrics.Length.MILLI.to(19.0, Units.METRE) / 2.0;
            double area = Math.PI * radius * radius;
            return Numbers.toInt(newtons / area);
          })
          .build();
    }

    @Override
    public List<BrikoStage1Variable> getInputVariables() {
      return List.of(BrikoStage1Variable.FORCE1, BrikoStage1Variable.FORCE2);
    }

    @Override
    public Unit<?> getUnit() {
      return Units.PASCAL;
    }
  };

  @Nonnull
  @Override
  public final Class<BrikoStage1Variable> getInputVariablesClass() {
    return BrikoStage1Variable.class;
  }

  @Override
  public final Set<Option> options() {
    return Option.addToDefault(Option.TEXT_VALUE_BANNER);
  }
}
