package com.ak.digitalfilter;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import static com.ak.util.Strings.EMPTY;
import static com.ak.util.Strings.NEW_LINE;
import static com.ak.util.Strings.SPACE;

abstract class AbstractDigitalFilter implements DigitalFilter {
  @Nonnull
  private IntsAcceptor after = EMPTY_INTS_ACCEPTOR;

  @Override
  public final void forEach(@Nonnull IntsAcceptor after) {
    Objects.requireNonNull(after);
    if (this.after.equals(EMPTY_INTS_ACCEPTOR)) {
      this.after = after;
    }
    else {
      throw new IllegalStateException(toString());
    }
  }

  final void publish(@Nonnull int... out) {
    after.accept(out);
  }

  @Override
  public String toString() {
    return toString(getClass().getSimpleName());
  }

  static String toString(@Nonnull String base, @Nonnull DigitalFilter filter) {
    return base + filter.toString().replaceAll(NEW_LINE,
        Stream.generate(() -> SPACE).limit(base.length()).collect(Collectors.joining(EMPTY, NEW_LINE, EMPTY)));
  }

  final String toString(@Nonnull String filterName) {
    if (getFrequencyFactor() > 1) {
      return String.format("%s (f \u00b7 %.1f)", filterName, getFrequencyFactor());
    }
    else if (getFrequencyFactor() < 1) {
      return String.format("%s (f / %.1f)", filterName, 1.0 / getFrequencyFactor());
    }
    else {
      return String.format("%s (delay %.1f)", filterName, getDelay());
    }
  }

  final void illegalArgumentException(@Nonnull int[] in) {
    throw new IllegalArgumentException(String.format("%s %s", this, Arrays.toString(in)));
  }
}