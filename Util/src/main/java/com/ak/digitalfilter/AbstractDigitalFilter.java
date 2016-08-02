package com.ak.digitalfilter;

import java.util.function.IntConsumer;

import javax.annotation.Nonnull;

abstract class AbstractDigitalFilter implements DigitalFilter {
  @Nonnull
  private IntConsumer after = value -> {
  };

  @Override
  public void forEach(@Nonnull IntConsumer after) {
    this.after = after;
  }

  final void publish(int out) {
    after.accept(out);
  }
}
