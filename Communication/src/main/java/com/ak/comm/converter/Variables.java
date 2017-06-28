package com.ak.comm.converter;

import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import com.ak.util.Strings;

public enum Variables {
  ;

  public static <E extends Enum<E> & Variable<E>> String toString(E variable, int value) {
    return String.format("%s = %d %s", variable.name(), value, variable.getUnit());
  }

  public static <E extends Enum<E> & Variable<E>> String toName(E variable) {
    return String.format("%s, %s", variable.name(), variable.getUnit());
  }

  static <E extends Enum<E> & Variable<E>, T> T tryFindSame(Variable<E> variable,
                                                            @Nonnull Function<E, T> function, @Nonnull Supplier<T> orElse) {
    String s = variable.name().replaceFirst(".*\\D+", Strings.EMPTY);
    if (s.isEmpty() || Integer.parseInt(s) == 1) {
      return orElse.get();
    }
    else {
      return function.apply(Enum.valueOf(variable.getDeclaringClass(), variable.name().replaceFirst("\\d+", "1")));
    }
  }
}
