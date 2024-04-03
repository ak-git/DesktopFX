package com.ak.digitalfilter;

final class RemoveConstantFilter extends AbstractOperableFilter {
  private final double alpha;
  private double xPrev;

  RemoveConstantFilter(double alpha) {
    this.alpha = alpha;
  }

  @Override
  public int applyAsInt(int in) {
    if (checkResetAndClear()) {
      xPrev = in;
    }
    double x = in + alpha * xPrev;
    int y = (int) (x - xPrev);
    xPrev = x;
    return y;
  }
}