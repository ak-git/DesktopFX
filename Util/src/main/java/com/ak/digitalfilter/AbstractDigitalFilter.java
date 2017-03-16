package com.ak.digitalfilter;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnegative;
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
    if (getFrequencyFactor() > 1) {
      return String.format("%s (f \u00b7 %.1f; delay %.1f)", getClass().getSimpleName(), getFrequencyFactor(), getDelay());
    }
    else if (getFrequencyFactor() < 1) {
      return String.format("%s (f / %.1f; delay %.1f)", getClass().getSimpleName(), 1.0 / getFrequencyFactor(), getDelay());
    }
    else {
      return String.format("%s (delay %.1f)", getClass().getSimpleName(), getDelay());
    }
  }

  static String newLineTabSpaces(@Nonnegative int len) {
    return Stream.generate(() -> SPACE).limit(len).collect(Collectors.joining(EMPTY, NEW_LINE, EMPTY));
  }

  final void illegalArgumentException(int[] in) {
    throw new IllegalArgumentException(String.format("%s %s", toString(), Arrays.toString(in)));
  }
}