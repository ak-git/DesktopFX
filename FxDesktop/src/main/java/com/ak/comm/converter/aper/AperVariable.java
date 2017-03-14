package com.ak.comm.converter.aper;

import java.util.stream.Stream;

import javax.measure.Unit;

import com.ak.comm.converter.Variable;
import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.FilterBuilder;
import com.ak.digitalfilter.aper.AperCoefficients;
import org.apache.commons.math3.analysis.interpolation.AkimaSplineInterpolator;
import tec.uom.se.unit.MetricPrefix;
import tec.uom.se.unit.Units;

public enum AperVariable implements Variable<AperVariable> {
  R1 {
    @Override
    public Unit<?> getUnit() {
      return MetricPrefix.MILLI(Units.OHM);
    }

    @Override
    public DigitalFilter filter() {
      return FilterBuilder.of().function(in -> (int) Math.round(
          15000.0 * in /
              new AkimaSplineInterpolator().interpolate(
                  AperCoefficients.R_ADC_15_OHM.get(), AperCoefficients.R_VALUE_15_OHM.get()).value(166)
          )
      ).build();
    }
  },
  M1 {
    @Override
    public DigitalFilter filter() {
      return FilterBuilder.of().fir(AperCoefficients.MYO).build();
    }
  },
  I1 {
    @Override
    public Unit<?> getUnit() {
      return Units.OHM;
    }

    @Override
    public DigitalFilter filter() {
      return FilterBuilder.of().function(in -> (int) Math.round(
          new AkimaSplineInterpolator().interpolate(
              AperCoefficients.I_ADC.get(), AperCoefficients.I_OHM.get()).value(Math.min(Math.max(in, 0), 3000))
          )
      ).build();
    }

    @Override
    public Stream<AperVariable> getInputVariables() {
      return Stream.of(I1, R1);
    }
  },

  R2 {
    @Override
    public Unit<?> getUnit() {
      return R1.getUnit();
    }

    @Override
    public DigitalFilter filter() {
      return R1.filter();
    }
  },
  M2 {
    @Override
    public DigitalFilter filter() {
      return M1.filter();
    }
  },
  I2 {
    @Override
    public Unit<?> getUnit() {
      return I1.getUnit();
    }

    @Override
    public DigitalFilter filter() {
      return I1.filter();
    }
  }
}
