package com.ak.digitalfilter;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import static com.ak.util.Strings.EMPTY;
import static com.ak.util.Strings.NEW_LINE;
import static com.ak.util.Strings.SPACE;

abstract class AbstractDigitalFilter implements DigitalFilter {
  private static final IntsAcceptor EMPTY_ACCEPTOR = IntsAcceptor.empty();
  private IntsAcceptor after = EMPTY_ACCEPTOR;

  @Override
  public final void forEach(@Nonnull IntsAcceptor after) {
    Objects.requireNonNull(after);
    if (this.after.equals(EMPTY_ACCEPTOR)) {
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
    return String.format("%s (delay %.1f)", getClass().getSimpleName(), getDelay());
  }

  @Nonnull
  static String newLineTabSpaces(@Nonnegative int len) {
    return Stream.generate(() -> SPACE).limit(len).collect(Collectors.joining(EMPTY, NEW_LINE, EMPTY));
  }
}
