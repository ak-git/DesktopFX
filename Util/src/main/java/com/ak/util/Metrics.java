package com.ak.util;


import tech.units.indriya.AbstractUnit;
import tech.units.indriya.quantity.Quantities;
import tech.units.indriya.unit.Units;

import javax.measure.MetricPrefix;
import javax.measure.Quantity;
import javax.measure.Unit;
import java.util.function.DoubleUnaryOperator;

public enum Metrics {
  ;

  public static final DoubleUnaryOperator MILLI = mm -> Length.MILLI.to(mm, Units.METRE);

  private interface UnitConversion<Q extends Quantity<Q>> {
    static <Q extends Quantity<Q>> double convert(Unit<Q> from, double value, Unit<Q> to) {
      if (Double.isNaN(value)) {
        return Double.NaN;
      }
      else {
        return Quantities.getQuantity(value, from).to(to).getValue().doubleValue();
      }
    }

    double to(double value, Unit<Q> unit);
  }

  public enum Length implements UnitConversion<javax.measure.quantity.Length> {
    MILLI {
      @Override
      public double to(double value, Unit<javax.measure.quantity.Length> toUnit) {
        return UnitConversion.convert(MetricPrefix.MILLI(Units.METRE), value, toUnit);
      }
    },
    METRE {
      @Override
      public double to(double value, Unit<javax.measure.quantity.Length> toUnit) {
        return UnitConversion.convert(Units.METRE, value, toUnit);
      }
    }
  }

  public enum Dimensionless implements UnitConversion<javax.measure.quantity.Dimensionless> {
    PERCENT {
      @Override
      public double to(double value, Unit<javax.measure.quantity.Dimensionless> toUnit) {
        return UnitConversion.convert(Units.PERCENT, value, toUnit);
      }
    },
    ONE {
      @Override
      public double to(double value, Unit<javax.measure.quantity.Dimensionless> toUnit) {
        return UnitConversion.convert(AbstractUnit.ONE, value, toUnit);
      }
    }
  }
}
