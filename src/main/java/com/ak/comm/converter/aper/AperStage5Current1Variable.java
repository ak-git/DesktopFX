package com.ak.comm.converter.aper;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnegative;
import javax.annotation.Nullable;
import javax.measure.Unit;

import com.ak.comm.converter.DependentVariable;
import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.FilterBuilder;
import com.ak.rsm.TetrapolarSystem;
import tec.uom.se.unit.MetricPrefix;

import static tec.uom.se.unit.Units.METRE;
import static tec.uom.se.unit.Units.OHM;

public enum AperStage5Current1Variable implements DependentVariable<AperStage4Current1Variable, AperStage5Current1Variable> {
  R1,
  R2,
  APPARENT_RHO_07_21_CHANNEL(7.0, 7.0 * 3.0) {
    @Override
    public List<AperStage4Current1Variable> getInputVariables() {
      return Collections.singletonList(AperStage4Current1Variable.R1);
    }

    @Override
    public Unit<?> getUnit() {
      return MetricPrefix.MILLI(OHM).multiply(METRE);
    }

    @Override
    public Set<Option> options() {
      return Collections.singleton(Option.TEXT_VALUE_BANNER);
    }
  },
  APPARENT_RHO_21_35_CHANNEL(7.0 * 3.0, 7.0 * 5.0) {
    @Override
    public List<AperStage4Current1Variable> getInputVariables() {
      return Collections.singletonList(AperStage4Current1Variable.R2);
    }

    @Override
    public Unit<?> getUnit() {
      return APPARENT_RHO_07_21_CHANNEL.getUnit();
    }

    @Override
    public Set<Option> options() {
      return APPARENT_RHO_07_21_CHANNEL.options();
    }
  },
  CCR;

  @Nullable
  private final TetrapolarSystem system;

  AperStage5Current1Variable(@Nonnegative double smm, @Nonnegative double lmm) {
    system = TetrapolarSystem.milli(0.1).s(smm).l(lmm);
  }

  AperStage5Current1Variable() {
    system = null;
  }

  @Override
  public final DigitalFilter filter() {
    if (system == null) {
      return DependentVariable.super.filter();
    }
    else {
      return FilterBuilder.of().operator(() -> rMilli -> (int) Math.round(system.getApparent(rMilli))).build();
    }
  }

  @Override
  public final Class<AperStage4Current1Variable> getInputVariablesClass() {
    return AperStage4Current1Variable.class;
  }
}
