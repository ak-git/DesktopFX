package com.ak.digitalfilter;

import javax.annotation.Nonnull;

final class FilterWrapper extends AbstractDigitalFilter {
  @Nonnull
  private final String filterName;
  @Nonnull
  private final DigitalFilter filter;

  FilterWrapper(@Nonnull String filterName, @Nonnull DigitalFilter filter) {
    this.filterName = filterName;
    this.filter = filter;
    this.filter.forEach(this::publish);
  }

  @Override
  public void accept(@Nonnull int... values) {
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
