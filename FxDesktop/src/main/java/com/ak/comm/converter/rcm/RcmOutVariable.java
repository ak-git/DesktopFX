package com.ak.comm.converter.rcm;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.IntUnaryOperator;

import javax.annotation.Nonnull;
import javax.measure.Unit;

import com.ak.comm.converter.DependentVariable;
import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.FilterBuilder;
import com.ak.numbers.Coefficients;
import com.ak.numbers.Interpolators;
import com.ak.numbers.rcm.RcmBaseSurfaceCoefficientsChannel1;
import com.ak.numbers.rcm.RcmBaseSurfaceCoefficientsChannel2;
import com.ak.numbers.rcm.RcmCoefficients;
import tec.uom.se.unit.MetricPrefix;
import tec.uom.se.unit.Units;

public enum RcmOutVariable implements DependentVariable<RcmInVariable, RcmOutVariable> {
  RHEO_1 {
    @Override
    public List<RcmInVariable> getInputVariables() {
      return Arrays.asList(RcmInVariable.QS_1, RcmInVariable.RHEO_1);
    }

    @Override
    public Unit<?> getUnit() {
      return MetricPrefix.MICRO(Units.OHM);
    }

    @Override
    public DigitalFilter filter() {
      return getRheoFilter(RcmCoefficients.RHEO_ADC_TO_260_MILLI_1);
    }

    @Override
    public Set<Option> options() {
      return Option.addToDefault(Option.INVERSE, Option.FORCE_ZERO_IN_RANGE);
    }
  },
  BASE_1 {
    @Override
    public List<RcmInVariable> getInputVariables() {
      return Arrays.asList(RcmInVariable.QS_1, RcmInVariable.BASE_1);
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
      return getQoSFilter(RcmCoefficients.CC_ADC_TO_OHM_1);
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
      return Arrays.asList(RcmInVariable.QS_2, RcmInVariable.RHEO_2);
    }

    @Override
    public DigitalFilter filter() {
      return getRheoFilter(RcmCoefficients.RHEO_ADC_TO_260_MILLI_2);
    }
  },
  BASE_2 {
    @Override
    public List<RcmInVariable> getInputVariables() {
      return Arrays.asList(RcmInVariable.QS_2, RcmInVariable.BASE_2);
    }

    @Override
    public DigitalFilter filter() {
      return RcmOutVariable.getBaseFilter(RcmBaseSurfaceCoefficientsChannel2.class);
    }
  },
  QS_2 {
    @Override
    public DigitalFilter filter() {
      return getQoSFilter(RcmCoefficients.CC_ADC_TO_OHM_2);
    }
  };

  @Nonnull
  @Override
  public Class<RcmInVariable> getInputVariablesClass() {
    return RcmInVariable.class;
  }

  private static DigitalFilter getRheoFilter(Coefficients rheoAdcTo260Milli) {
    IntUnaryOperator rheo260ADC = Interpolators.interpolator(rheoAdcTo260Milli).get();
    return smoothing(FilterBuilder.of().biOperator(() -> (ccADC, rheoADC) -> (int) Math.round(260.0 * 1000.0 * rheoADC / rheo260ADC.applyAsInt(ccADC))));
  }

  /**
   * <p>Filters [dp = 0.01/5, ds = 0.001]:
   * Delay = 425.0 / 200 Hz = 2.125 sec
   * <ol>
   * <li>0.05 - 22.5 Hz @ 200 Hz / 27 coeff</li>
   * <li>0.05 - 2.5 Hz @ 25 Hz / 31 coeff</li>
   * <li>0.05 - 1.2 Hz @ 5 Hz / 12 coeff</li>
   * </ol>
   * </p>
   *
   * @param coeffEnum surface coefficients.
   * @param <C>       surface coefficients class.
   * @return DigitalFilter
   */
  private static <C extends Enum<C> & Coefficients> DigitalFilter getBaseFilter(@Nonnull Class<C> coeffEnum) {
    return smoothing(FilterBuilder.asFilterBuilder(coeffEnum)
        .decimate(RcmCoefficients.BR_F200, 8)
        .decimate(RcmCoefficients.BR_F025, 5)
        .fir(RcmCoefficients.BR_F005)
        .interpolate(8, RcmCoefficients.BR_F025)
        .interpolate(5, RcmCoefficients.BR_F200)
    );
  }

  private static DigitalFilter getQoSFilter(Coefficients adcToOhm) {
    return smoothing(FilterBuilder.asFilterBuilder(adcToOhm));
  }

  private static DigitalFilter smoothing(FilterBuilder filterBuilder) {
    return filterBuilder.smoothingImpulsive(4).build();
  }
}
