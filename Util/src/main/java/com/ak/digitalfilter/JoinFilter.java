package com.ak.digitalfilter;

import java.util.Arrays;

import javax.annotation.Nonnull;

final class JoinFilter extends AbstractDigitalFilter {
  @Nonnull
  private final DigitalFilter outFilter;
  @Nonnull
  private final int[] joinIndexes;

  JoinFilter(@Nonnull DigitalFilter outFilter, @Nonnull int... joinIndexes) {
    this.outFilter = outFilter;
    this.joinIndexes = Arrays.copyOf(joinIndexes, joinIndexes.length);
    outFilter.forEach(this::publish);
  }

  @Override
  public void accept(@Nonnull int... values) {
    int[] selected = new int[joinIndexes.length];
    for (int i = 0; i < selected.length; i++) {
      selected[i] = values[joinIndexes[i]];
    }
    outFilter.accept(selected);
  }

  @Override
  public int size() {
    return outFilter.size();
  }
}
