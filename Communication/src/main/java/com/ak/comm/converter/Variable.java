package com.ak.comm.converter;

import com.ak.digitalfilter.DigitalFilter;
import com.ak.digitalfilter.FilterBuilder;
import com.ak.util.Strings;
import tech.units.indriya.AbstractUnit;

import javax.measure.Unit;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

public interface Variable<E extends Enum<E> & Variable<E>> {
  enum Option {
    VISIBLE, TEXT_VALUE_BANNER, INVERSE, FORCE_ZERO_IN_RANGE;

    public static Set<Option> defaultOptions() {
      return EnumSet.of(VISIBLE);
    }

    public static Set<Option> addToDefault(Option... option) {
      return EnumSet.of(VISIBLE, option);
    }
  }

  default Unit<?> getUnit() {
    return tryFindSame(Variable::getUnit, () -> AbstractUnit.ONE);
  }

  default DigitalFilter filter() {
    return tryFindSame(Variable::filter, () -> FilterBuilder.of().build());
  }

  default Set<Option> options() {
    return tryFindSame(Variable::options, Option::defaultOptions);
  }

  default int indexBy(Option option) {
    if (options().contains(option)) {
      return EnumSet.allOf(getDeclaringClass()).stream().filter(e -> e.options().contains(option)).mapToInt(Enum::ordinal).sorted()
          .reduce(-1, (acc, now) -> now > ordinal() ? acc : acc + 1);
    }
    else {
      return -1;
    }
  }

  String name();

  int ordinal();

  Class<E> getDeclaringClass();

  default <T> T tryFindSame(Function<E, T> function, Supplier<T> orElse) {
    var s = Strings.numberSuffix(name());
    if (s.isEmpty() || Integer.parseInt(s) == 1) {
      return orElse.get();
    }
    else {
      return function.apply(Enum.valueOf(getDeclaringClass(), name().replaceFirst("\\d+", "1")));
    }
  }
}
