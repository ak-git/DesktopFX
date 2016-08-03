package com.ak.digitalfilter;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

interface DigitalFilter extends Delay, IntsAcceptor {
  void forEach(@Nonnull IntsAcceptor after);

  @Nonnegative
  int size();
}
