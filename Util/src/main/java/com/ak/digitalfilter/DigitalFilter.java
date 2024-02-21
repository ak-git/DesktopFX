package com.ak.digitalfilter;

import javax.annotation.Nonnegative;

public interface DigitalFilter extends IntsAcceptor {
  void forEach(IntsAcceptor after);

  void reset();

  @Nonnegative
  int getOutputDataSize();

  default double getDelay() {
    return 0.0;
  }

  @Nonnegative
  default double getFrequencyFactor() {
    return 1.0;
  }
}
