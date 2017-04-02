package com.ak.comm.converter.aper;

import java.util.stream.Stream;

import javax.measure.Unit;

import com.ak.comm.converter.DependentVariable;
import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.FilterBuilder;
import com.ak.numbers.Coefficients;
import com.ak.numbers.Interpolators;
import com.ak.numbers.aper.AperCoefficients;
import tec.uom.se.unit.MetricPrefix;
import tec.uom.se.unit.Units;

import static com.ak.numbers.aper.AperCoefficients.IADC_VADC_0;
import static com.ak.numbers.aper.AperCoefficients.IADC_VADC_15000;

public enum AperOutVariable implements DependentVariable<AperInVariable> {
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
      return FilterBuilder.of().biOperator(Interpolators.interpolator(new Coefficients[] {IADC_VADC_0, IADC_VADC_15000})).build();
    }
  },
  RI1 {
    @Override
    public Unit<?> getUnit() {
      return Units.OHM;
    }

    @Override
    public DigitalFilter filter() {
      return FilterBuilder.of().operator(Interpolators.interpolator(AperCoefficients.I_ADC_TO_OHM)).build();
    }
  },

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
  RI2 {
    @Override
    public Unit<?> getUnit() {
      return RI1.getUnit();
    }

    @Override
    public DigitalFilter filter() {
      return RI1.filter();
    }
  };

  @Override
  public final Class<AperInVariable> getInputVariablesClass() {
    return AperInVariable.class;
  }
}
