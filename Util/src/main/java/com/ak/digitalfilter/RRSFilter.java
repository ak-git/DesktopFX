package com.ak.digitalfilter;

final class RRSFilter extends AbstractOperableFilter {
  private int n;
  private double y;

  @Override
  public int applyAsInt(int x) {
    n++;
    y += (1.0 / n) * (x - y);
    return (int) Math.round(y);
  }
}