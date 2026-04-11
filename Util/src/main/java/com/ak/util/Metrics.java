package com.ak.util;


import tech.units.indriya.AbstractUnit;
import tech.units.indriya.quantity.Quantities;
import tech.units.indriya.unit.Units;

import javax.measure.MetricPrefix;
import javax.measure.Quantity;
import javax.measure.Unit;

public enum Metrics {
  ;

  private interface UnitConversion<Q extends Quantity<Q>> {
    static <Q extends Quantity<Q>> double convert(Unit<Q> from, double value, Unit<Q> to) {
      if (Double.isFinite(value)) {
        return Quantities.getQuantity(value, from).to(to).getValue().doubleValue();
      }
      else {
        return value;
      }
    }

    double to(double value, Unit<Q> unit);

    default double toSI(double value) {
      return value;
    }
  }

  public enum Length implements UnitConversion<javax.measure.quantity.Length> {
    METRE {
      @Override
      public double to(double value, Unit<javax.measure.quantity.Length> toUnit) {
        return UnitConversion.convert(Units.METRE, value, toUnit);
      }
    },
    MILLI {
      @Override
      public double to(double value, Unit<javax.measure.quantity.Length> toUnit) {
        return UnitConversion.convert(MetricPrefix.MILLI(Units.METRE), value, toUnit);
      }

      @Override
      public double toSI(double value) {
        return to(value, Units.METRE);
      }
    }
  }

  public enum Dimensionless implements UnitConversion<javax.measure.quantity.Dimensionless> {
    PERCENT {
      @Override
      public double to(double value, Unit<javax.measure.quantity.Dimensionless> toUnit) {
        return UnitConversion.convert(Units.PERCENT, value, toUnit);
      }

      @Override
      public double toSI(double value) {
        return to(value, AbstractUnit.ONE);
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
