package com.ak.comm.converter.aper.calibration;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.measure.Unit;

import com.ak.comm.converter.DependentVariable;
import com.ak.comm.converter.aper.AperInVariable;
import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.FilterBuilder;
import com.ak.numbers.Interpolators;
import com.ak.numbers.aper.AperCoefficients;
import tec.uom.se.unit.Units;

public enum AperCalibrationOhmVariable implements DependentVariable<AperInVariable, AperCalibrationOhmVariable> {
  ADC_R {
    @Override
    public List<AperInVariable> getInputVariables() {
      return Collections.singletonList(VAR_R);
    }

    @Override
    public DigitalFilter filter() {
      return FilterBuilder.of().smoothingImpulsive(20).rrs(2000).build();
    }
  },
  STD_ADC_R {
    @Override
    public List<AperInVariable> getInputVariables() {
      return Collections.singletonList(VAR_R);
    }

    @Override
    public DigitalFilter filter() {
      return FilterBuilder.of().std(2000).smoothingImpulsive(20).build();
    }

    @Override
    public Set<Option> options() {
      return Collections.singleton(Option.TEXT_VALUE_BANNER);
    }
  },
  ADC_PUMP_U {
    @Override
    public List<AperInVariable> getInputVariables() {
      return Collections.singletonList(VAR_CCU);
    }

    @Override
    public DigitalFilter filter() {
      return FilterBuilder.of().smoothingImpulsive(20).rrs(2000).build();
    }
  },
  RI {
    @Override
    public Unit<?> getUnit() {
      return Units.OHM;
    }

    @Override
    public List<AperInVariable> getInputVariables() {
      return Collections.singletonList(VAR_CCU);
    }

    @Override
    public DigitalFilter filter() {
      return FilterBuilder.of().smoothingImpulsive(20).operator(Interpolators.interpolator(COEFF_ADC_TO_OHM)).build();
    }

    @Override
    public Set<Option> options() {
      return Collections.singleton(Option.TEXT_VALUE_BANNER);
    }
  };

  public static final AperInVariable VAR_R = AperInVariable.R1;
  public static final AperInVariable VAR_CCU = AperInVariable.CCU1;
  public static final AperCoefficients COEFF_ADC_TO_OHM = AperCoefficients.ADC_TO_OHM_1;

  @Override
  public final Class<AperInVariable> getInputVariablesClass() {
    return AperInVariable.class;
  }

  @Override
  public Set<Option> options() {
    return Option.addToDefault(Option.TEXT_VALUE_BANNER);
  }
}
