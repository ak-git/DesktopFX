package com.ak.comm.converter.aper;

import java.util.stream.Stream;

import javax.measure.Unit;

import com.ak.comm.converter.DependentVariable;
import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.FilterBuilder;
import com.ak.digitalfilter.aper.AperCoefficients;
import org.apache.commons.math3.analysis.interpolation.AkimaSplineInterpolator;
import tec.uom.se.unit.MetricPrefix;
import tec.uom.se.unit.Units;

public enum AperOutVariable implements DependentVariable<AperVariable> {
  R1 {
    @Override
    public Stream<AperVariable> getInputVariables() {
      return Stream.of(AperVariable.R1, AperVariable.RI1);
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
  RI1;

  @Override
  public final Class<AperVariable> getInputVariablesClass() {
    return AperVariable.class;
  }
}
