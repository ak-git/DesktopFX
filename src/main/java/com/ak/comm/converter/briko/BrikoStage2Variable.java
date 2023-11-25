package com.ak.comm.converter.briko;

import com.ak.comm.converter.DependentVariable;
import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.FilterBuilder;
import com.ak.digitalfilter.briko.ChangeDirectionFilter;
import tec.uom.se.AbstractUnit;

import javax.annotation.Nonnull;
import javax.measure.Unit;
import java.util.List;

public enum BrikoStage2Variable implements DependentVariable<BrikoStage1Variable, BrikoStage2Variable> {
  FORCE1,
  FORCE2,
  POSITION,
  RESET_EVENT {
    @Override
    public List<BrikoStage1Variable> getInputVariables() {
      return List.of(BrikoStage1Variable.POSITION);
    }

    @Override
    public Unit<?> getUnit() {
      return AbstractUnit.ONE;
    }

    @Override
    public DigitalFilter filter() {
      return FilterBuilder.of()
          .chain(new ChangeDirectionFilter(BrikoStage1Variable.FREQUENCY / 2)).build();
    }
  };

  @Nonnull
  @Override
  public final Class<BrikoStage1Variable> getInputVariablesClass() {
    return BrikoStage1Variable.class;
  }
}
