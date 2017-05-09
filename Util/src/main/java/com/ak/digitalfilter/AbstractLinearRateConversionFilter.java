package com.ak.digitalfilter;

import java.util.function.IntUnaryOperator;

abstract class AbstractLinearRateConversionFilter extends AbstractUnaryFilter {
  final IntUnaryOperator comb = new CombFilter(1);
  final IntUnaryOperator integrator = new IntegrateFilter();

  @Override
  public final double getDelay() {
    return getDelay(0.0);
  }
}