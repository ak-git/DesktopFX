package com.ak.comm.converter.aper;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnegative;
import javax.measure.Unit;

import com.ak.comm.converter.DependentVariable;
import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.FilterBuilder;
import com.ak.rsm.TetrapolarSystem;
import tec.uom.se.unit.MetricPrefix;

import static tec.uom.se.unit.Units.METRE;
import static tec.uom.se.unit.Units.OHM;

/**
 * Electrode systems: [7 x 21 mm] and [21 x 35 mm]
 */
public enum AperStage5Current1Variable7x21x35 implements DependentVariable<AperStage4Current1Variable, AperStage5Current1Variable7x21x35> {
  APPARENT_RHO_07_21_CHANNEL(7.0, 7.0 * 3.0) {
    @Override
    public List<AperStage4Current1Variable> getInputVariables() {
      return Collections.singletonList(AperStage4Current1Variable.R1);
    }
  },
  APPARENT_RHO_21_35_CHANNEL(7.0 * 3.0, 7.0 * 5.0) {
    @Override
    public List<AperStage4Current1Variable> getInputVariables() {
      return Collections.singletonList(AperStage4Current1Variable.R2);
    }
  },
  CCR(0.0, 0.0) {
    @Override
    public Unit<?> getUnit() {
      return AperStage5Current1Variable.CCR.getUnit();
    }

    @Override
    public DigitalFilter filter() {
      return AperStage5Current1Variable.CCR.filter();
    }
  };

  private final TetrapolarSystem system;

  AperStage5Current1Variable7x21x35(@Nonnegative double smm, @Nonnegative double lmm) {
    system = new TetrapolarSystem(smm, lmm, MetricPrefix.MILLI(METRE));
  }

  @Override
  public final Class<AperStage4Current1Variable> getInputVariablesClass() {
    return AperStage4Current1Variable.class;
  }

  @Override
  public Unit<?> getUnit() {
    return MetricPrefix.MILLI(OHM).multiply(MetricPrefix.DECI(METRE));
  }

  @Override
  public DigitalFilter filter() {
    return FilterBuilder.of().operator(() -> rMilli -> (int) Math.round(system.getApparent(rMilli * 10.0))).build();
  }
}
