package com.ak.appliance.aper.comm.converter;

import com.ak.appliance.aper.numbers.AperCoefficients;
import com.ak.appliance.aper.numbers.AperSurfaceCoefficientsChannel1;
import com.ak.appliance.aper.numbers.AperSurfaceCoefficientsChannel2;
import com.ak.comm.converter.DependentVariable;
import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.FilterBuilder;
import com.ak.numbers.common.CommonCoefficients;
import tech.units.indriya.unit.Units;

import javax.measure.MetricPrefix;
import javax.measure.Unit;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public enum AperStage2UnitsVariable implements DependentVariable<AperStage1Variable, AperStage2UnitsVariable> {
  R1 {
    @Override
    public List<AperStage1Variable> getInputVariables() {
      return List.of(AperStage1Variable.CCU1, AperStage1Variable.R1);
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
      return List.of(AperStage1Variable.CCU2, AperStage1Variable.R2);
    }

    @Override
    public DigitalFilter filter() {
      return FilterBuilder.asFilterBuilder(AperSurfaceCoefficientsChannel2.class).build();
    }
  },
  R3 {
    @Override
    public List<AperStage1Variable> getInputVariables() {
      return List.of(AperStage1Variable.CCU1, AperStage1Variable.R2);
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

    @Override
    public DigitalFilter filter() {
      return FilterBuilder.of().fir(CommonCoefficients.ECG).build();
    }
  },
  ECG2 {
    @Override
    public List<AperStage1Variable> getInputVariables() {
      return Collections.singletonList(AperStage1Variable.E2);
    }
  },
  MYO1 {
    @Override
    public List<AperStage1Variable> getInputVariables() {
      return Collections.singletonList(AperStage1Variable.E1);
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
  MYO2 {
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

    @Override
    public Set<Option> options() {
      return Collections.singleton(Option.TEXT_VALUE_BANNER);
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
