package com.ak.comm.converter;

import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import com.ak.util.Strings;

class Variables {
  private Variables() {
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
