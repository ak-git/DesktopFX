package com.ak.digitalfilter;

import javax.annotation.Nonnegative;

final class RecursiveRunningSumFilter extends AbstractOperableFilter {
  private final CombFilter combIntegrate;
  private final IntegrateFilter integrator;
  private final int averageFactor;

  RecursiveRunningSumFilter(@Nonnegative int averageFactor) {
    combIntegrate = new CombFilter(averageFactor);
    integrator = new IntegrateFilter();
    this.averageFactor = averageFactor;
  }

  @Override
  public double getDelay() {
    return combIntegrate.getDelay() + integrator.getDelay();
  }

  @Override
  public int applyAsInt(int in) {
    return combIntegrate.andThen(integrator).applyAsInt(in) / averageFactor;
  }

  @Override
  public String toString() {
    return String.format("RRS%d (delay %.1f)", averageFactor, getDelay());
  }
}