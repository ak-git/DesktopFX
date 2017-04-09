package com.ak.digitalfilter;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

public interface DigitalFilter extends IntsAcceptor {
  void forEach(@Nonnull IntsAcceptor after);

  @Nonnegative
  int getOutputDataSize();

  default double getDelay() {
    return 0.0;
  }

  @Nonnegative
  default double getFrequencyFactor() {
    return 1.0;
  }

  default double getDelay(double beforeDelay) {
    return beforeDelay + getDelay();
  }
}
