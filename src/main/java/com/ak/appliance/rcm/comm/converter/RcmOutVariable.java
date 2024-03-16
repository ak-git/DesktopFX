package com.ak.appliance.rcm.comm.converter;

import com.ak.appliance.rcm.numbers.RcmBaseSurfaceCoefficientsChannel1;
import com.ak.appliance.rcm.numbers.RcmBaseSurfaceCoefficientsChannel2;
import com.ak.appliance.rcm.numbers.RcmCoefficients;
import com.ak.appliance.rcm.numbers.RcmSimpleCoefficients;
import com.ak.comm.converter.DependentVariable;
import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.FilterBuilder;
import com.ak.numbers.Coefficients;
import com.ak.numbers.Interpolators;
import tec.uom.se.unit.MetricPrefix;
import tec.uom.se.unit.Units;

import javax.measure.Unit;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.IntUnaryOperator;

public enum RcmOutVariable implements DependentVariable<RcmInVariable, RcmOutVariable> {
  RHEO_1 {
    @Override
    public List<RcmInVariable> getInputVariables() {
      return List.of(RcmInVariable.QS_1, RcmInVariable.RHEO_1);
    }

    @Override
    public Unit<?> getUnit() {
      return MetricPrefix.MICRO(Units.OHM);
    }

    @Override
    public DigitalFilter filter() {
      return getRheoFilter(RcmCoefficients.RHEO_ADC_TO_260_MILLI.of(1));
    }

    @Override
    public Set<Option> options() {
      return Option.addToDefault(Option.INVERSE, Option.FORCE_ZERO_IN_RANGE);
    }
  },
  BASE_1 {
    @Override
    public List<RcmInVariable> getInputVariables() {
      return List.of(RcmInVariable.QS_1, RcmInVariable.BASE_1);
    }

    @Override
    public Unit<?> getUnit() {
      return MetricPrefix.MILLI(Units.OHM);
    }

    @Override
    public DigitalFilter filter() {
      return RcmOutVariable.getBaseFilter(RcmBaseSurfaceCoefficientsChannel1.class);
    }
  },
  QS_1 {
    @Override
    public Unit<?> getUnit() {
      return Units.OHM;
    }

    @Override
    public DigitalFilter filter() {
      return getQoSFilter(RcmCoefficients.CC_ADC_TO_OHM.of(1));
    }

    @Override
    public Set<Option> options() {
      return Collections.singleton(Option.TEXT_VALUE_BANNER);
    }
  },
  ECG {
    @Override
    public Unit<?> getUnit() {
      return MetricPrefix.MILLI(Units.VOLT);
    }

    @Override
    public DigitalFilter filter() {
      return RcmOutVariable.smoothing(FilterBuilder.of());
    }

    @Override
    public Set<Option> options() {
      return Option.addToDefault(Option.FORCE_ZERO_IN_RANGE);
    }
  },
  RHEO_2 {
    @Override
    public List<RcmInVariable> getInputVariables() {
      return List.of(RcmInVariable.QS_2, RcmInVariable.RHEO_2);
    }

    @Override
    public DigitalFilter filter() {
      return getRheoFilter(RcmCoefficients.RHEO_ADC_TO_260_MILLI.of(2));
    }
  },
  BASE_2 {
    @Override
    public List<RcmInVariable> getInputVariables() {
      return List.of(RcmInVariable.QS_2, RcmInVariable.BASE_2);
    }

    @Override
    public DigitalFilter filter() {
      return RcmOutVariable.getBaseFilter(RcmBaseSurfaceCoefficientsChannel2.class);
    }
  },
  QS_2 {
    @Override
    public DigitalFilter filter() {
      return getQoSFilter(RcmCoefficients.CC_ADC_TO_OHM.of(2));
    }
  };

  @Override
  public Class<RcmInVariable> getInputVariablesClass() {
    return RcmInVariable.class;
  }

  private static DigitalFilter getRheoFilter(Coefficients rheoAdcTo260Milli) {
    IntUnaryOperator rheo260ADC = Interpolators.interpolator(rheoAdcTo260Milli).get();
    return smoothing(FilterBuilder.of().biOperator(() -> (ccADC, rheoADC) -> (int) Math.round(260.0 * 1000.0 * rheoADC / rheo260ADC.applyAsInt(ccADC))));
  }

  /**
   * <p>Filters [dp = 0.01/5, ds = 0.01]:
   * Delay = 341.0 / 200 Hz = 1.705 sec
   * <ol>
   * <li>0.05 - 22.5 Hz @ 200 Hz / 22 coeff</li>
   * <li>0.05 - 2.5 Hz @ 25 Hz / 25 coeff</li>
   * <li>0.05 - 1.2 Hz @ 5 Hz / 10 coeff</li>
   * </ol>
   * </p>
   *
   * @param coeffEnum surface coefficients.
   * @param <C>       surface coefficients class.
   * @return DigitalFilter
   */
  private static <C extends Enum<C> & Coefficients> DigitalFilter getBaseFilter(Class<C> coeffEnum) {
    return smoothing(FilterBuilder.asFilterBuilder(coeffEnum)
        .decimate(RcmSimpleCoefficients.BR_F200, 8)
        .decimate(RcmSimpleCoefficients.BR_F025, 5)
        .fir(RcmSimpleCoefficients.BR_F005)
        .interpolate(5, RcmSimpleCoefficients.BR_F025)
        .interpolate(8, RcmSimpleCoefficients.BR_F200)
    );
  }

  private static DigitalFilter getQoSFilter(Coefficients adcToOhm) {
    return smoothing(FilterBuilder.asFilterBuilder(adcToOhm));
  }

  private static DigitalFilter smoothing(FilterBuilder filterBuilder) {
    return filterBuilder.smoothingImpulsive(4).build();
  }
}
