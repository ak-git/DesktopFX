package com.ak.comm.converter.aper;

import java.util.Collections;
import java.util.List;

import javax.measure.Unit;

import com.ak.comm.converter.DependentVariable;
import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.FilterBuilder;

import static tec.uom.se.unit.Units.METRE;
import static tec.uom.se.unit.Units.OHM;

public enum AperStage6Current1Variable implements DependentVariable<AperStage5Current1Variable, AperStage6Current1Variable> {
  APPARENT_RHO_CHANNEL_1 {
    @Override
    public List<AperStage5Current1Variable> getInputVariables() {
      return Collections.singletonList(AperStage5Current1Variable.R1);
    }

    @Override
    public Unit<?> getUnit() {
      return OHM.multiply(METRE);
    }

    @Override
    public DigitalFilter filter() {
      return FilterBuilder.of().operator(() -> r -> r * 2).build();
    }
  },
  APPARENT_RHO_CHANNEL_2 {
    @Override
    public List<AperStage5Current1Variable> getInputVariables() {
      return Collections.singletonList(AperStage5Current1Variable.R2);
    }
  },
  CCR;

  @Override
  public final Class<AperStage5Current1Variable> getInputVariablesClass() {
    return AperStage5Current1Variable.class;
  }
}
