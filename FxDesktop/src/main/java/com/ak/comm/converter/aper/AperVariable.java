package com.ak.comm.converter.aper;

import javax.measure.Unit;

import com.ak.comm.converter.Variable;
import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.FilterBuilder;
import com.ak.digitalfilter.aper.AperCoefficients;
import org.apache.commons.math3.analysis.interpolation.AkimaSplineInterpolator;
import tec.uom.se.unit.MetricPrefix;
import tec.uom.se.unit.Units;

public enum AperVariable implements Variable {
  R1,
  M1 {
    @Override
    public Unit<?> getUnit() {
      return MetricPrefix.MILLI(Units.VOLT);
    }

    @Override
    public DigitalFilter filter() {
      return FilterBuilder.of().fir(AperCoefficients.MYO).build();
    }
  },
  RI1 {
    @Override
    public Unit<?> getUnit() {
      return Units.OHM;
    }

    @Override
    public DigitalFilter filter() {
      return FilterBuilder.of().operator(in -> (int) Math.round(
          new AkimaSplineInterpolator().interpolate(
              AperCoefficients.I_ADC.get(), AperCoefficients.I_OHM.get()).value(Math.min(Math.max(in, 0), 3000))
          )
      ).build();
    }
  },

  R2,
  M2 {
    @Override
    public Unit<?> getUnit() {
      return M1.getUnit();
    }

    @Override
    public DigitalFilter filter() {
      return M1.filter();
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
  }
}
