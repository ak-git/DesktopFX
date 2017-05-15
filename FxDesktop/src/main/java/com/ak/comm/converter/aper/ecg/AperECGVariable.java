package com.ak.comm.converter.aper.ecg;

import java.util.stream.Stream;

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

public enum AperECGVariable implements DependentVariable<AperInVariable> {
  R1 {
    @Override
    public Stream<AperInVariable> getInputVariables() {
      return Stream.of(AperInVariable.RI1, AperInVariable.R1);
    }

    @Override
    public Unit<?> getUnit() {
      return MetricPrefix.MILLI(Units.OHM);
    }

    @Override
    public DigitalFilter filter() {
      return FilterBuilder.of().biOperator(Interpolators.interpolator(AperSurfaceCoefficients.class)).
          smoothingImpulsive(10).fir(AperCoefficients.RHEO).build();
    }
  },
  ECG1 {
    @Override
    public Stream<AperInVariable> getInputVariables() {
      return Stream.of(AperInVariable.E1);
    }

    @Override
    public DigitalFilter filter() {
      return FilterBuilder.of().fir(AperCoefficients.ECG).build();
    }
  },
  RI1,

  R2 {
    @Override
    public Stream<AperInVariable> getInputVariables() {
      return Stream.of(AperInVariable.RI2, AperInVariable.R2);
    }

    @Override
    public Unit<?> getUnit() {
      return R1.getUnit();
    }

    @Override
    public DigitalFilter filter() {
      return R1.filter();
    }
  },
  ECG2 {
    @Override
    public Stream<AperInVariable> getInputVariables() {
      return Stream.of(AperInVariable.E2);
    }

    @Override
    public DigitalFilter filter() {
      return ECG1.filter();
    }
  },
  RI2;

  @Override
  public final Class<AperInVariable> getInputVariablesClass() {
    return AperInVariable.class;
  }
}
