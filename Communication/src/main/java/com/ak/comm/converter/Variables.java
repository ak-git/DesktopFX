package com.ak.comm.converter;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.measure.Quantity;
import javax.measure.Unit;

import com.ak.util.Strings;
import tec.uom.se.format.LocalUnitFormat;
import tec.uom.se.unit.MetricPrefix;

public enum Variables {
  ;

  public static String toString(@Nonnull Quantity<?> quantity) {
    return String.join(Strings.SPACE, quantity.getValue().toString(), toString(quantity.getUnit()));
  }

  public static <E extends Enum<E> & Variable<E>> String toString(@Nonnull E variable, int value) {
    return "%s = %,d %s".formatted(toString(variable), value, variable.getUnit());
  }

  public static <E extends Enum<E> & Variable<E>> String toString(@Nonnull E variable) {
    String baseName = variable.getClass().getPackage().getName() + ".variables";
    String name;
    try {
      ResourceBundle resourceBundle = ResourceBundle.getBundle(baseName, Locale.getDefault(), variable.getClass().getModule());
      if (resourceBundle.containsKey(variable.name())) {
        name = Objects.toString(resourceBundle.getString(variable.name()), Strings.EMPTY);
      }
      else {
        Logger.getLogger(Variables.class.getName()).log(Level.CONFIG,
            () -> "Missing resource key %s at file %s.properties".formatted(variable.name(), baseName));
        name = variable.name();
      }
    }
    catch (MissingResourceException e) {
      name = variable.name();
    }
    return name;
  }

  public static <Q extends Quantity<Q>> String toString(int value, @Nonnull Unit<Q> unit, @Nonnegative int scaleFactor10) {
    int scale = (int) Math.rint(StrictMath.log10(unit.getConverterTo(unit.getSystemUnit()).convert(1.0)));
    int displayScale = scale + 1;
    while (displayScale % 3 != 0) {
      displayScale++;
    }

    double sf10 = scaleFactor10;
    if (scaleFactor10 == 1 && value != 0) {
      for (int i = value; i % 10 == 0; i /= 10) {
        sf10 *= 10;
      }
    }
    int formatZeros = Math.max(0, (displayScale - scale) - (int) Math.rint(StrictMath.log10(sf10)));

    Unit<Q> displayUnit = unit.getSystemUnit();
    for (MetricPrefix metricPrefix : MetricPrefix.values()) {
      if (displayScale == (int) Math.rint(StrictMath.log10(metricPrefix.getConverter().convert(1.0)))) {
        displayUnit = displayUnit.transform(metricPrefix.getConverter());
        break;
      }
    }

    if (value == 0) {
      return "%d %s".formatted(value, sf10 > 10 ? displayUnit : unit);
    }
    else {
      double converted = unit.getConverterTo(displayUnit).convert(value);
      if (Math.abs(converted) < 1.0) {
        return "%,d %s".formatted(value, unit);
      }
      else {
        return "%%,.%df %%s".formatted(formatZeros).formatted(converted, displayUnit);
      }
    }
  }

  public static <E extends Enum<E> & Variable<E>> String toName(@Nonnull E variable) {
    return String.join(", ", variable.name(), variable.getUnit().toString());
  }

  private static String toString(@Nonnull Unit<?> unit) {
    return LocalUnitFormat.getInstance().format(unit);
  }
}
