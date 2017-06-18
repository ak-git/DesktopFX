package com.ak.comm.converter;

import java.lang.reflect.Field;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

import com.ak.util.Strings;

public enum Variables {
  ;

  public static <E extends Enum<E> & Variable<E>> boolean isDisplay(E variable) {
    try {
      Field field = variable.getDeclaringClass().getDeclaredField(variable.name());
      if (field.isAnnotationPresent(VariableProperties.class)) {
        return field.getAnnotation(VariableProperties.class).display();
      }
    }
    catch (NoSuchFieldException e) {
      Logger.getLogger(Variables.class.getName()).log(Level.WARNING, toName(variable), e);
    }
    return true;
  }

  public static <E extends Enum<E> & Variable<E>> String toString(E variable, int value) {
    return String.format("%s = %d %s", variable.name(), value, variable.getUnit());
  }

  public static <E extends Enum<E> & Variable<E>> String toName(E variable) {
    return String.format("%s, %s", variable.name(), variable.getUnit());
  }

  static <E extends Enum<E> & Variable<E>, T> T tryFindSame(@Nonnull String name, @Nonnull Class<E> eClass,
                                                            @Nonnull Function<E, T> function, @Nonnull Supplier<T> orElse) {
    String s = name.replaceFirst(".*\\D+", Strings.EMPTY);
    if (s.isEmpty() || Integer.parseInt(s) == 1) {
      return orElse.get();
    }
    else {
      return function.apply(Enum.valueOf(eClass, name.replaceFirst("\\d+", "1")));
    }
  }
}
