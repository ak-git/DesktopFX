package com.ak.comm.converter.aper.ecg;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.measure.Unit;

import com.ak.comm.converter.DependentVariable;
import com.ak.comm.converter.aper.AperInVariable;
import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.FilterBuilder;
import com.ak.numbers.Interpolators;
import com.ak.numbers.aper.AperCoefficients;
import com.ak.numbers.aper.AperSurfaceCoefficients;
import tec.uom.se.unit.MetricPrefix;
import tec.uom.se.unit.Units;

public enum AperECGVariable implements DependentVariable<AperInVariable, AperECGVariable> {
  R1 {
    @Override
    public List<AperInVariable> getInputVariables() {
      return Arrays.asList(AperInVariable.RI1, AperInVariable.R1);
    }

    @Override
    public Unit<?> getUnit() {
      return MetricPrefix.MILLI(Units.OHM);
    }

    @Override
    public DigitalFilter filter() {
      return FilterBuilder.of().biOperator(Interpolators.interpolator(AperSurfaceCoefficients.class)).
          smoothingImpulsive(10).build();
    }
  },
  ECG1 {
    @Override
    public List<AperInVariable> getInputVariables() {
      return Collections.singletonList(AperInVariable.E1);
    }

    @Override
    public DigitalFilter filter() {
      return FilterBuilder.of().fir(AperCoefficients.ECG).build();
    }
  },
  RI1,

  R2 {
    @Override
    public List<AperInVariable> getInputVariables() {
      return Arrays.asList(AperInVariable.RI2, AperInVariable.R2);
    }
  },
  ECG2 {
    @Override
    public List<AperInVariable> getInputVariables() {
      return Collections.singletonList(AperInVariable.E2);
    }
  },
  RI2;

  @Override
  public final Class<AperInVariable> getInputVariablesClass() {
    return AperInVariable.class;
  }
}
