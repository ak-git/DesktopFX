package com.ak.comm.converter.aper.sincos;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.measure.Unit;

import com.ak.comm.converter.DependentVariable;
import com.ak.comm.converter.aper.AperInVariable;
import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.FilterBuilder;
import com.ak.numbers.aper.sincos.AperCoefficients;
import com.ak.numbers.aper.sincos.AperSurfaceCoefficientsChannel1;
import com.ak.numbers.aper.sincos.AperSurfaceCoefficientsChannel2;
import com.ak.numbers.common.CommonCoefficients;
import tec.uom.se.unit.MetricPrefix;
import tec.uom.se.unit.Units;

public enum AperOutVariable implements DependentVariable<AperInVariable, AperOutVariable> {
  R1 {
    @Override
    public List<AperInVariable> getInputVariables() {
      return Arrays.asList(AperInVariable.CCU1, AperInVariable.R1);
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
  ECG1 {
    @Override
    public List<AperInVariable> getInputVariables() {
      return Collections.singletonList(AperInVariable.E1);
    }

    @Override
    public DigitalFilter filter() {
      return FilterBuilder.of().fir(CommonCoefficients.ECG).build();
    }
  },
  MYO1 {
    @Override
    public List<AperInVariable> getInputVariables() {
      return Collections.singletonList(AperInVariable.E1);
    }

    @Override
    public DigitalFilter filter() {
      return FilterBuilder.of().iirMATLAB(
          new double[] {
              0.9022774304591, 0, 0, 0,
              0, 0, 0, 0,
              0, 0, 0, 0,
              0, 0, 0, 0,
              0, 0, 0, 0,
              -0.9022774304591
          },
          new double[] {
              1, 0, 0, 0,
              0, 0, 0, 0,
              0, 0, 0, 0,
              0, 0, 0, 0,
              0, 0, 0, 0,
              -0.8045548609183
          }
      ).fir(CommonCoefficients.MYO).build();
    }
  },
  CCR1 {
    @Override
    public List<AperInVariable> getInputVariables() {
      return Collections.singletonList(AperInVariable.CCU1);
    }

    @Override
    public Unit<?> getUnit() {
      return Units.OHM;
    }

    @Override
    public DigitalFilter filter() {
      return FilterBuilder.asFilterBuilder(AperCoefficients.ADC_TO_OHM).smoothingImpulsive(10).build();
    }

    @Override
    public Set<Option> options() {
      return Collections.singleton(Option.TEXT_VALUE_BANNER);
    }
  },

  R2 {
    @Override
    public List<AperInVariable> getInputVariables() {
      return Arrays.asList(AperInVariable.CCU2, AperInVariable.R2);
    }

    @Override
    public DigitalFilter filter() {
      return FilterBuilder.asFilterBuilder(AperSurfaceCoefficientsChannel2.class).build();
    }
  },
  ECG2 {
    @Override
    public List<AperInVariable> getInputVariables() {
      return Collections.singletonList(AperInVariable.E2);
    }
  },
  MYO2 {
    @Override
    public List<AperInVariable> getInputVariables() {
      return Collections.singletonList(AperInVariable.E2);
    }
  },
  CCR2 {
    @Override
    public List<AperInVariable> getInputVariables() {
      return Collections.singletonList(AperInVariable.CCU2);
    }
  };

  @Override
  public final Class<AperInVariable> getInputVariablesClass() {
    return AperInVariable.class;
  }
}
