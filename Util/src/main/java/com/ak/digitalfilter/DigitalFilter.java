package com.ak.digitalfilter;

public interface DigitalFilter extends IntsAcceptor {
  void forEach(IntsAcceptor after);

  void reset();

  int getOutputDataSize();

  default double getDelay() {
    return 0.0;
  }

  default double getFrequencyFactor() {
    return 1.0;
  }
}
