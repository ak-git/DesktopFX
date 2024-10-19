package com.ak.comm.converter;

import com.ak.util.Numbers;
import com.ak.util.Strings;
import tech.units.indriya.format.SimpleUnitFormat;

import javax.annotation.Nonnegative;
import javax.measure.MetricPrefix;
import javax.measure.Quantity;
import javax.measure.Unit;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public enum Variables {
  ;

  private static final String M_POINT = "m·";
  private static final String M_PAR = "m(";

  public static String toString(Quantity<?> quantity) {
    return String.join(Strings.SPACE, quantity.getValue().toString(), SimpleUnitFormat.getInstance().format(quantity.getUnit()));
  }

  public static <E extends Enum<E> & Variable<E>> String toString(E variable, int value) {
    return "%s = %,d %s".formatted(toString(variable), value, fixUnit(variable.getUnit()));
  }

  public static <E extends Enum<E> & Variable<E>> String toString(E variable) {
    String baseName = "%s.variables".formatted(variable.getClass().getPackage().getName());
    Logger logger = Logger.getLogger(variable.getClass().getName());
    if (logger.getFilter() == null) {
      try {
        var resourceBundle = ResourceBundle.getBundle(baseName, Locale.getDefault(), variable.getClass().getModule());
        if (resourceBundle.containsKey(variable.name())) {
          return Objects.toString(resourceBundle.getString(variable.name()), Strings.EMPTY);
        }
        else {
          Logger.getLogger(Variables.class.getName()).log(Level.WARNING,
              () -> "Missing resource key %s at file %s.properties".formatted(variable.name(), baseName)
          );
          logger.setFilter(_ -> false);
        }
      }
      catch (MissingResourceException e) {
        Logger.getLogger(Variables.class.getName()).log(Level.CONFIG,
            """
                Missing resource key %s at file %s.properties.
                module-info.java should opens %s to %s
                """
                .formatted(variable.name(), baseName, variable.getClass().getPackage(), Variables.class.getModule()), e
        );
        logger.setFilter(_ -> false);
      }
    }
    return variable.name();
  }

  public static <Q extends Quantity<Q>> String toString(int value, Unit<Q> unit, @Nonnegative int scaleFactor10) {
    int scale = Numbers.log10ToInt(unit.getConverterTo(unit.getSystemUnit()).convert(1.0));
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
    int formatZeros = Math.max(0, (displayScale - scale) - Numbers.log10ToInt(sf10));

    Unit<Q> displayUnit = unit.getSystemUnit();
    for (MetricPrefix metricPrefix : MetricPrefix.values()) {
      if (displayScale == metricPrefix.getExponent()) {
        displayUnit = displayUnit.prefix(metricPrefix);
        break;
      }
    }

    if (value == 0) {
      return "%d %s".formatted(value, fixUnit(sf10 > 10 ? displayUnit : unit));
    }
    else {
      double converted = unit.getConverterTo(displayUnit).convert(value);
      if (Math.abs(converted) < 1.0) {
        return "%,d %s".formatted(value, fixUnit(unit));
      }
      else {
        return "%%,.%df %%s".formatted(formatZeros).formatted(converted, fixUnit(displayUnit));
      }
    }
  }

  public static String fixUnit(Unit<?> unit) {
    var s = unit.toString();
    if (s.startsWith(M_PAR)) {
      return "m%s".formatted(fixUnit(unit.getSystemUnit()));
    }
    else if (s.startsWith(M_POINT) && Character.getType(s.charAt(s.length() - 1)) == Character.UPPERCASE_LETTER) {
      return "%s·m".formatted(s.substring(M_POINT.length()));
    }
    else {
      return s;
    }
  }

  public static <Q extends Quantity<Q>> Unit<Q> tryToUp3(Unit<Q> unit) {
    int d = Math.min(Numbers.log10ToInt(unit.getConverterTo(unit.getSystemUnit()).convert(1.0)), 0);
    d = 3 * Numbers.toInt(Math.ceil((d + 1) / 3.0));
    if (d == 0) {
      return unit.getSystemUnit();
    }
    else if (d < 0) {
      for (MetricPrefix metricPrefix : MetricPrefix.values()) {
        if (d == metricPrefix.getExponent()) {
          return unit.getSystemUnit().prefix(metricPrefix);
        }
      }
    }
    return unit;
  }
}
