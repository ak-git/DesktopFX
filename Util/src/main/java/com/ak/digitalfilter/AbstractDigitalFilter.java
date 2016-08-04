package com.ak.digitalfilter;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

abstract class AbstractDigitalFilter implements DigitalFilter {
  static final String NEW_LINE = String.format("%n");
  static final String EMPTY = "";
  private static final String SPACE = " ";
  private IntsAcceptor after;

  @Override
  public final void forEach(@Nonnull IntsAcceptor after) {
    Objects.requireNonNull(after);
    if (this.after == null) {
      this.after = after;
    }
    else {
      throw new IllegalStateException(toString());
    }
  }

  final void publish(int... out) {
    if (after != null) {
      after.accept(out);
    }
  }

  @Override
  public String toString() {
    return String.format("%s (delay %.1f)", getClass().getSimpleName(), getDelay());
  }

  @Nonnull
  static String newLineTabSpaces(@Nonnegative int len) {
    return Stream.generate(() -> SPACE).limit(len).collect(Collectors.joining(EMPTY, NEW_LINE, EMPTY));
  }
}
