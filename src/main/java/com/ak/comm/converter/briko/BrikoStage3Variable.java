package com.ak.comm.converter.briko;

import com.ak.comm.converter.DependentVariable;
import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.FilterBuilder;

import javax.annotation.Nonnull;
import javax.measure.Unit;
import java.util.List;
import java.util.function.IntBinaryOperator;
import java.util.function.Supplier;

import static tec.uom.se.unit.Units.GRAM;

public enum BrikoStage3Variable implements DependentVariable<BrikoStage2Variable, BrikoStage3Variable> {
  FORCE1 {
    @Override
    public List<BrikoStage2Variable> getInputVariables() {
      return List.of(BrikoStage2Variable.FORCE1, BrikoStage2Variable.RESET_EVENT);
    }

    @Override
    public DigitalFilter filter() {
      return FilterBuilder.of().biOperator(RESET_TO_ZERO).build();
    }

    @Override
    public Unit<?> getUnit() {
      return GRAM;
    }
  },
  FORCE2 {
    @Override
    public List<BrikoStage2Variable> getInputVariables() {
      return List.of(BrikoStage2Variable.FORCE2, BrikoStage2Variable.RESET_EVENT);
    }

    @Override
    public DigitalFilter filter() {
      return FilterBuilder.of().biOperator(RESET_TO_ZERO).build();
    }
  },
  POSITION;

  private static final Supplier<IntBinaryOperator> RESET_TO_ZERO = () -> new IntBinaryOperator() {
    private int base;

    @Override
    public int applyAsInt(int force, int resetEvent) {
      if (resetEvent > 0) {
        base = force;
      }
      return force - base;
    }
  };

  @Nonnull
  @Override
  public final Class<BrikoStage2Variable> getInputVariablesClass() {
    return BrikoStage2Variable.class;
  }
}
