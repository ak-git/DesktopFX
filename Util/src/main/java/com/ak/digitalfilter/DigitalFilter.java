package com.ak.digitalfilter;

import java.util.function.IntConsumer;

import javax.annotation.Nonnull;

interface DigitalFilter extends Delay, IntConsumer {
  void forEach(@Nonnull IntConsumer after);
}
