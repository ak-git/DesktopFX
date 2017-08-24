package com.ak.comm.converter;

import java.util.MissingResourceException;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.measure.Quantity;
import javax.measure.Unit;

import com.ak.util.Strings;
import tec.uom.se.format.LocalUnitFormat;

public enum Variables {
  ;

  public static String toString(@Nonnull Quantity<?> quantity) {
    return String.format("%s %s", String.valueOf(quantity.getValue()), toString(quantity.getUnit()));
  }

  public static <E extends Enum<E> & Variable<E>> String toString(@Nonnull E variable, int value) {
    return String.format("%s = %d %s", variable.name(), value, toString(variable.getUnit()));
  }

  public static <E extends Enum<E> & Variable<E>> String toString(@Nonnull E variable) {
    String baseName = variable.getClass().getPackage().getName() + ".variables";
    try {
      ResourceBundle resourceBundle = ResourceBundle.getBundle(baseName);
      if (resourceBundle.containsKey(variable.name())) {
        return Objects.toString(resourceBundle.getString(variable.name()), Strings.EMPTY);
      }
      else {
        Logger.getLogger(Variables.class.getName()).log(Level.CONFIG,
            String.format("Missing resource key %s at file %s.properties", variable.name(), baseName));
      }
    }
    catch (MissingResourceException e) {
      Logger.getLogger(Variables.class.getName()).log(Level.CONFIG,
          String.format("Missing resource file %s.properties for %s key", baseName, variable.name()));
    }
    return variable.name();
  }

  public static <E extends Enum<E> & Variable<E>> String toName(@Nonnull E variable) {
    return String.format("%s, %s", variable.name(), toString(variable.getUnit()));
  }

  private static String toString(@Nonnull Unit<?> unit) {
    return LocalUnitFormat.getInstance().format(unit);
  }
}
