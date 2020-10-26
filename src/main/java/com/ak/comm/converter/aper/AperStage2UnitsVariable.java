package com.ak.comm.converter.aper;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.measure.Unit;

import com.ak.comm.converter.DependentVariable;
import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.FilterBuilder;
import com.ak.numbers.aper.AperCoefficients;
import com.ak.numbers.aper.AperSurfaceCoefficientsChannel1;
import com.ak.numbers.aper.AperSurfaceCoefficientsChannel2;
import tec.uom.se.unit.MetricPrefix;
import tec.uom.se.unit.Units;

public enum AperStage2UnitsVariable implements DependentVariable<AperStage1Variable, AperStage2UnitsVariable> {
  R1 {
    @Override
    public List<AperStage1Variable> getInputVariables() {
      return Arrays.asList(AperStage1Variable.CCU1, AperStage1Variable.R1);
    }

    @Override
    public Unit<?> getUnit() {
      return MetricPrefix.MILLI(Units.OHM);
    }

    @Override
    public DigitalFilter filter() {
      return FilterBuilder.asFilterBuilder(AperSurfaceCoefficientsChannel1.class).build();
    }
  },
  R2 {
    @Override
    public List<AperStage1Variable> getInputVariables() {
      return Arrays.asList(AperStage1Variable.CCU2, AperStage1Variable.R2);
    }

    @Override
    public DigitalFilter filter() {
      return FilterBuilder.asFilterBuilder(AperSurfaceCoefficientsChannel2.class).build();
    }
  },
  R3 {
    @Override
    public List<AperStage1Variable> getInputVariables() {
      return Arrays.asList(AperStage1Variable.CCU1, AperStage1Variable.R2);
    }

    @Override
    public DigitalFilter filter() {
      return FilterBuilder.asFilterBuilder(AperSurfaceCoefficientsChannel2.class).build();
    }
  },
  ECG1 {
    @Override
    public List<AperStage1Variable> getInputVariables() {
      return Collections.singletonList(AperStage1Variable.E1);
    }
  },
  ECG2 {
    @Override
    public List<AperStage1Variable> getInputVariables() {
      return Collections.singletonList(AperStage1Variable.E2);
    }
  },
  CCR1 {
    @Override
    public List<AperStage1Variable> getInputVariables() {
      return Collections.singletonList(AperStage1Variable.CCU1);
    }

    @Override
    public Unit<?> getUnit() {
      return Units.OHM;
    }

    @Override
    public DigitalFilter filter() {
      return FilterBuilder.asFilterBuilder(AperCoefficients.ADC_TO_OHM).build();
    }
  },
  CCR2 {
    @Override
    public List<AperStage1Variable> getInputVariables() {
      return Collections.singletonList(AperStage1Variable.CCU2);
    }
  };

  @Override
  public final Class<AperStage1Variable> getInputVariablesClass() {
    return AperStage1Variable.class;
  }
}
