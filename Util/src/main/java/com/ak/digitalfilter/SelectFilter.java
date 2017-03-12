package com.ak.digitalfilter;

import java.util.Arrays;
import java.util.Objects;

import javax.annotation.Nonnull;

final class SelectFilter extends AbstractDigitalFilter {
  @Nonnull
  private final DigitalFilter outFilter;
  @Nonnull
  private final int[] selectedIndexes;

  SelectFilter(@Nonnull int[] selectedIndexes, @Nonnull DigitalFilter outFilter) {
    Objects.requireNonNull(selectedIndexes);
    this.selectedIndexes = Arrays.copyOf(selectedIndexes, selectedIndexes.length);
    this.outFilter = outFilter;
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
  public void accept(@Nonnull int... values) {
    int[] selected = new int[selectedIndexes.length];
    for (int i = 0; i < selected.length; i++) {
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
  public int size() {
    return outFilter.size();
  }
}
