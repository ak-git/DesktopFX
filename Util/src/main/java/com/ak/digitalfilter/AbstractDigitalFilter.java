package com.ak.digitalfilter;

import java.util.Objects;

import javax.annotation.Nonnull;

abstract class AbstractDigitalFilter implements DigitalFilter {
  private IntsAcceptor after;

  @Override
  public final void forEach(@Nonnull IntsAcceptor after) {
    Objects.requireNonNull(after);
    if (this.after == null) {
      this.after = after;
    }
    else {
      throw new IllegalStateException(this.after.toString());
    }
  }

  final void publish(int... out) {
    if (after != null) {
      after.accept(out);
    }
  }
}
