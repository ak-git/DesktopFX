package com.ak.comm.converter.rcm;

import javax.annotation.Nonnull;
import javax.measure.Unit;

import com.ak.comm.converter.DependentVariable;
import tec.uom.se.unit.MetricPrefix;
import tec.uom.se.unit.Units;

public enum RcmOutVariable implements DependentVariable<RcmInVariable, RcmOutVariable> {
  RHEO_1 {
    @Override
    public Unit<?> getUnit() {
      return MetricPrefix.MILLI(Units.OHM);
    }
  },
  BASE_1 {
    @Override
    public Unit<?> getUnit() {
      return Units.OHM;
    }
  },
  ECG {
    @Override
    public Unit<?> getUnit() {
      return MetricPrefix.MILLI(Units.VOLT);
    }
  },
  RHEO_2 {
    @Override
    public Unit<?> getUnit() {
      return MetricPrefix.MILLI(Units.OHM);
    }
  },
  BASE_2 {
    @Override
    public Unit<?> getUnit() {
      return Units.OHM;
    }
  };

  @Nonnull
  @Override
  public Class<RcmInVariable> getInputVariablesClass() {
    return RcmInVariable.class;
  }
}
