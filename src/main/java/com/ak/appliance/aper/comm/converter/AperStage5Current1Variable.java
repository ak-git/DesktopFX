package com.ak.appliance.aper.comm.converter;

import com.ak.comm.converter.DependentVariable;
import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.FilterBuilder;
import com.ak.rsm.resistance.Resistance;
import com.ak.rsm.resistance.TetrapolarResistance;
import tec.uom.se.unit.MetricPrefix;

import javax.annotation.Nonnegative;
import javax.annotation.Nullable;
import javax.measure.Unit;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static tec.uom.se.unit.Units.METRE;
import static tec.uom.se.unit.Units.OHM;

public enum AperStage5Current1Variable implements DependentVariable<AperStage4Current1Variable, AperStage5Current1Variable> {
  R1,
  MYO1 {
    @Override
    public Set<Option> options() {
      return Collections.emptySet();
    }
  },
  R2,
  MYO2,
  APPARENT_06_18_RHO(6.0, 6.0 * 3.0) {
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
  APPARENT_30_18_RHO(6.0 * 3.0, 6.0 * 5.0) {
    @Override
    public List<AperStage4Current1Variable> getInputVariables() {
      return Collections.singletonList(AperStage4Current1Variable.R2);
    }

    @Override
    public Unit<?> getUnit() {
      return APPARENT_06_18_RHO.getUnit();
    }

    @Override
    public Set<Option> options() {
      return APPARENT_06_18_RHO.options();
    }
  },
  APPARENT_07_21_RHO(7.0, 7.0 * 3.0) {
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
  APPARENT_35_21_RHO(7.0 * 3.0, 7.0 * 5.0) {
    @Override
    public List<AperStage4Current1Variable> getInputVariables() {
      return Collections.singletonList(AperStage4Current1Variable.R2);
    }

    @Override
    public Unit<?> getUnit() {
      return APPARENT_07_21_RHO.getUnit();
    }

    @Override
    public Set<Option> options() {
      return APPARENT_07_21_RHO.options();
    }
  },
  APPARENT_08_24_RHO(8.0, 8.0 * 3.0) {
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
  APPARENT_40_24_RHO(8.0 * 3.0, 8.0 * 5.0) {
    @Override
    public List<AperStage4Current1Variable> getInputVariables() {
      return Collections.singletonList(AperStage4Current1Variable.R2);
    }

    @Override
    public Unit<?> getUnit() {
      return APPARENT_08_24_RHO.getUnit();
    }

    @Override
    public Set<Option> options() {
      return APPARENT_08_24_RHO.options();
    }
  },
  APPARENT_10_30_RHO(10.0, 10.0 * 3.0) {
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
  APPARENT_50_30_RHO(10.0 * 3.0, 10.0 * 5.0) {
    @Override
    public List<AperStage4Current1Variable> getInputVariables() {
      return Collections.singletonList(AperStage4Current1Variable.R2);
    }

    @Override
    public Unit<?> getUnit() {
      return APPARENT_10_30_RHO.getUnit();
    }

    @Override
    public Set<Option> options() {
      return APPARENT_10_30_RHO.options();
    }
  },
  CCR;

  @Nullable
  private final TetrapolarResistance.PreBuilder<Resistance> builder;

  AperStage5Current1Variable(@Nonnegative double smm, @Nonnegative double lmm) {
    builder = TetrapolarResistance.ofMilli(smm, lmm);
  }

  AperStage5Current1Variable() {
    builder = null;
  }

  @Override
  public final DigitalFilter filter() {
    if (builder == null) {
      return DependentVariable.super.filter();
    }
    else {
      return FilterBuilder.of().operator(() -> rMilli -> (int) Math.round(builder.ofOhms(rMilli).resistivity())).build();
    }
  }

  @Override
  public final Class<AperStage4Current1Variable> getInputVariablesClass() {
    return AperStage4Current1Variable.class;
  }
}
