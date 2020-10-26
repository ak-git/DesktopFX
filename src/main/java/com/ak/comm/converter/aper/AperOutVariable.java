package com.ak.comm.converter.aper;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.measure.Unit;

import com.ak.comm.converter.DependentVariable;
import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.FilterBuilder;
import com.ak.numbers.aper.AperCoefficients;
import com.ak.numbers.aper.AperRheoCoefficients;
import com.ak.numbers.aper.AperSurfaceCoefficientsChannel1;
import com.ak.numbers.aper.AperSurfaceCoefficientsChannel2;
import tec.uom.se.unit.MetricPrefix;
import tec.uom.se.unit.Units;

public enum AperOutVariable implements DependentVariable<AperStage1Variable, AperOutVariable> {
  R1 {
    @Override
    public List<AperStage1Variable> getInputVariables() {
      return Arrays.asList(AperStage1Variable.CCU1, AperStage1Variable.R1);
    }

    @Override
    public Unit<?> getUnit() {
      return MetricPrefix.MILLI(Units.OHM);
    }

    @Override
    public DigitalFilter filter() {
      return AperOutVariable.filter(FilterBuilder.asFilterBuilder(AperSurfaceCoefficientsChannel1.class)).build();
    }
  },
  R2 {
    @Override
    public List<AperStage1Variable> getInputVariables() {
      return Arrays.asList(AperStage1Variable.CCU1, AperStage1Variable.R2);
    }

    @Override
    public DigitalFilter filter() {
      return AperOutVariable.filter(FilterBuilder.asFilterBuilder(AperSurfaceCoefficientsChannel2.class)).build();
    }
  },
  CCR {
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
      return AperOutVariable.filter(FilterBuilder.asFilterBuilder(AperCoefficients.ADC_TO_OHM)).build();
    }

    @Override
    public Set<Option> options() {
      return Collections.singleton(Option.TEXT_VALUE_BANNER);
    }
  },
  R1_AVG {
    @Override
    public List<AperStage1Variable> getInputVariables() {
      return Arrays.asList(AperStage1Variable.CCU1, AperStage1Variable.R1);
    }

    @Override
    public Unit<?> getUnit() {
      return MetricPrefix.MILLI(Units.OHM);
    }

    @Override
    public DigitalFilter filter() {
      return AperOutVariable.filter(FilterBuilder.asFilterBuilder(AperSurfaceCoefficientsChannel1.class)).rrs().build();
    }

    @Override
    public Set<Option> options() {
      return Collections.singleton(Option.TEXT_VALUE_BANNER);
    }
  },
  R2_AVG {
    @Override
    public List<AperStage1Variable> getInputVariables() {
      return Arrays.asList(AperStage1Variable.CCU1, AperStage1Variable.R2);
    }

    @Override
    public Unit<?> getUnit() {
      return MetricPrefix.MILLI(Units.OHM);
    }

    @Override
    public DigitalFilter filter() {
      return AperOutVariable.filter(FilterBuilder.asFilterBuilder(AperSurfaceCoefficientsChannel2.class)).rrs().build();
    }

    @Override
    public Set<Option> options() {
      return Collections.singleton(Option.TEXT_VALUE_BANNER);
    }
  };

  @Override
  public final Class<AperStage1Variable> getInputVariablesClass() {
    return AperStage1Variable.class;
  }

  /**
   * <p>Filters [dp = 0.01/5, ds = 0.001/5]:
   * Delay = 157.5 / 1000 Hz = 0.1575 sec
   * <ol>
   * <li>32 - 187.5 Hz @ 1000 Hz / 22 coeff</li>
   * <li>32 - 62.5 Hz @ 250 Hz / 29 coeff</li>
   * </ol>
   * </p>
   * <p>
   *
   * @param filterBuilder {@link FilterBuilder}
   * @return DigitalFilter
   */
  private static FilterBuilder filter(FilterBuilder filterBuilder) {
    return filterBuilder
        .decimate(AperRheoCoefficients.F_1000_32_187, 4)
        .decimate(AperRheoCoefficients.F_250_32_62, 2)
        .smoothingImpulsive(4)
        .interpolate(2, AperRheoCoefficients.F_250_32_62)
        .interpolate(4, AperRheoCoefficients.F_1000_32_187);
  }
}
