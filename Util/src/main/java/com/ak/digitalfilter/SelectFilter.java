package com.ak.digitalfilter;

import java.util.Arrays;
import java.util.Objects;

final class SelectFilter extends AbstractDigitalFilter {
  private final DigitalFilter outFilter;
  private final int[] selectedIndexes;

  SelectFilter(int[] selectedIndexes, DigitalFilter outFilter) {
    this.selectedIndexes = selectedIndexes.clone();
    this.outFilter = Objects.requireNonNull(outFilter);
    outFilter.forEach(this::publish);
  }

  @Override
  public double getDelay() {
    return outFilter.getDelay();
  }

  @Override
  public double getFrequencyFactor() {
    return outFilter.getFrequencyFactor();
  }

  @Override
  public void accept(int... values) {
    var selected = new int[selectedIndexes.length];
    for (var i = 0; i < selected.length; i++) {
      if (selectedIndexes[i] < values.length) {
        selected[i] = values[selectedIndexes[i]];
      }
      else {
        illegalArgumentException(values);
      }
    }
    outFilter.accept(selected);
  }

  @Override
  public void reset() {
    outFilter.reset();
  }

  @Override
  public int getOutputDataSize() {
    return outFilter.getOutputDataSize();
  }

  @Override
  public String toString() {
    return toString("%s (indexes = %s) - ".formatted(getClass().getSimpleName(), Arrays.toString(selectedIndexes)), outFilter);
  }
}
