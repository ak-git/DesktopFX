package com.ak.digitalfilter;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.ak.util.Strings.*;

abstract class AbstractDigitalFilter implements DigitalFilter {
  private static final IntsAcceptor EMPTY_INTS_ACCEPTOR = values -> {
  };
  private IntsAcceptor after = EMPTY_INTS_ACCEPTOR;

  @Override
  public final void forEach(IntsAcceptor after) {
    Objects.requireNonNull(after);
    if (this.after.equals(EMPTY_INTS_ACCEPTOR)) {
      this.after = after;
    }
    else {
      throw new IllegalStateException(toString());
    }
  }

  final void publish(int... out) {
    after.accept(out);
  }

  @Override
  public String toString() {
    return toString(getClass().getSimpleName());
  }

  static String toString(CharSequence base, DigitalFilter filter) {
    return base + filter.toString().replaceAll(NEW_LINE,
        Stream.generate(() -> SPACE).limit(base.length()).collect(Collectors.joining(EMPTY, NEW_LINE, EMPTY)));
  }

  final String toString(String filterName) {
    if (getFrequencyFactor() > 1) {
      return "%s (f · %.1f)".formatted(filterName, getFrequencyFactor());
    }
    else if (getFrequencyFactor() < 1) {
      return "%s (f / %.1f)".formatted(filterName, 1.0 / getFrequencyFactor());
    }
    else {
      return "%s (delay %.1f)".formatted(filterName, getDelay());
    }
  }

  final void illegalArgumentException(int[] in) {
    throw new IllegalArgumentException(String.join(SPACE, toString(), Arrays.toString(in)));
  }
}