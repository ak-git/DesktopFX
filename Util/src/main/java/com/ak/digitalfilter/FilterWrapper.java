package com.ak.digitalfilter;

import java.util.Objects;

final class FilterWrapper extends AbstractDigitalFilter {
  private final String filterName;
  private final DigitalFilter filter;

  FilterWrapper(String filterName, DigitalFilter filter) {
    this.filterName = Objects.requireNonNull(filterName);
    this.filter = Objects.requireNonNull(filter);
    this.filter.forEach(this::publish);
  }

  @Override
  public void accept(int... values) {
    filter.accept(values);
  }

  @Override
  public void reset() {
    filter.reset();
  }

  @Override
  public int getOutputDataSize() {
    return filter.getOutputDataSize();
  }

  @Override
  public double getDelay() {
    return filter.getDelay();
  }

  @Override
  public double getFrequencyFactor() {
    return filter.getFrequencyFactor();
  }

  @Override
  public String toString() {
    return toString(filterName);
  }
}
