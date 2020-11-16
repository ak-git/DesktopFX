package com.ak.rsm;

final class Potential1Layer extends AbstractPotentialLayer {
  Potential1Layer(double r) {
    super(r);
  }

  double value() {
    return value(r -> 1.0 / r);
  }
}
