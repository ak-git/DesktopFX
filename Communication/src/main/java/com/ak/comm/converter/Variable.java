package com.ak.comm.converter;

import java.util.EnumSet;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.measure.Unit;

import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.FilterBuilder;
import com.ak.util.Strings;
import tec.uom.se.AbstractUnit;

public interface Variable<E extends Enum<E> & Variable<E>> {
  enum Option {
    VISIBLE, FORCE_ZERO_IN_RANGE
  }

  default Unit<?> getUnit() {
    return tryFindSame(e -> e.getUnit(), () -> AbstractUnit.ONE);
  }

  default DigitalFilter filter() {
    return tryFindSame(e -> e.filter(), () -> FilterBuilder.of().build());
  }

  default Set<Option> options() {
    return tryFindSame(e -> e.options(), () -> EnumSet.of(Option.VISIBLE));
  }

  String name();

  Class<E> getDeclaringClass();

  default <T> T tryFindSame(@Nonnull Function<E, T> function, @Nonnull Supplier<T> orElse) {
    String s = name().replaceFirst(".*\\D+", Strings.EMPTY);
    if (s.isEmpty() || Integer.parseInt(s) == 1) {
      return orElse.get();
    }
    else {
      return function.apply(Enum.valueOf(getDeclaringClass(), name().replaceFirst("\\d+", "1")));
    }
  }
}
