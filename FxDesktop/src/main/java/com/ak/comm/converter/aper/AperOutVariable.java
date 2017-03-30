package com.ak.comm.converter.aper;

import java.util.stream.Stream;

import javax.measure.Unit;

import com.ak.comm.converter.DependentVariable;
import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.FilterBuilder;
import com.ak.numbers.aper.AperCoefficients;
import org.apache.commons.math3.analysis.interpolation.AkimaSplineInterpolator;
import tec.uom.se.unit.MetricPrefix;
import tec.uom.se.unit.Units;

public enum AperOutVariable implements DependentVariable<AperInVariable> {
  R1 {
    @Override
    public Stream<AperInVariable> getInputVariables() {
      return Stream.of(AperInVariable.R1, AperInVariable.RI1);
    }

    @Override
    public Unit<?> getUnit() {
      return MetricPrefix.MILLI(Units.OHM);
    }

    @Override
    public DigitalFilter filter() {
      return FilterBuilder.of().biOperator((v, rI) -> (int) Math.round(
          15000.0 * v /
              new AkimaSplineInterpolator().interpolate(
                  AperCoefficients.R_ADC_15_OHM.get(), AperCoefficients.R_VALUE_15_OHM.get()).value(rI)
      )).build();
    }
  },
  RI1,

  R2 {
    @Override
    public Stream<AperInVariable> getInputVariables() {
      return Stream.of(AperInVariable.R2, AperInVariable.RI2);
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
  RI2;

  @Override
  public final Class<AperInVariable> getInputVariablesClass() {
    return AperInVariable.class;
  }
}
