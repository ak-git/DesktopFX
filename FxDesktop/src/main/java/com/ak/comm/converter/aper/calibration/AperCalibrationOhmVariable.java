package com.ak.comm.converter.aper.calibration;

import java.util.Collections;
import java.util.List;

import javax.measure.Unit;

import com.ak.comm.converter.DependentVariable;
import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.FilterBuilder;
import com.ak.numbers.Interpolators;
import com.ak.numbers.aper.AperCoefficients;
import tec.uom.se.unit.Units;

public enum AperCalibrationOhmVariable implements DependentVariable<AperVariable, AperCalibrationOhmVariable> {
  R1,
  R1_PEAK_TO_PEAK {
    @Override
    public List<AperVariable> getInputVariables() {
      return Collections.singletonList(AperVariable.R1);
    }

    @Override
    public DigitalFilter filter() {
      return FilterBuilder.of().peakToPeak(1000).smoothingImpulsive(20).build();
    }
  },
  RI1 {
    @Override
    public Unit<?> getUnit() {
      return Units.OHM;
    }

    @Override
    public List<AperVariable> getInputVariables() {
      return Collections.singletonList(AperVariable.U1);
    }

    @Override
    public DigitalFilter filter() {
      return FilterBuilder.of().smoothingImpulsive(20).operator(Interpolators.interpolator(AperCoefficients.ADC_TO_OHM_1)).build();
    }
  },
  R2,
  RI2 {
    @Override
    public List<AperVariable> getInputVariables() {
      return Collections.singletonList(AperVariable.U2);
    }

    @Override
    public DigitalFilter filter() {
      return FilterBuilder.of().smoothingImpulsive(20).operator(Interpolators.interpolator(AperCoefficients.ADC_TO_OHM_2)).build();
    }
  };

  @Override
  public final Class<AperVariable> getInputVariablesClass() {
    return AperVariable.class;
  }
}
