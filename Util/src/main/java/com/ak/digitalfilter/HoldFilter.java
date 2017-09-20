package com.ak.digitalfilter;

import java.util.Arrays;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

final class HoldFilter extends AbstractBufferFilter {
  private final int lostCount;

  private HoldFilter(@Nonnull Builder builder) {
    super(builder.size);
    lostCount = builder.lostCount;
  }

  @Override
  int apply(int nowIndex) {
    return 0;
  }

  int[] getSorted() {
    int[] buffer = buffer();
    Arrays.sort(buffer);
    return Arrays.copyOfRange(buffer, lostCount, buffer.length - lostCount);
  }

  static final class Builder {
    @Nonnegative
    private final int size;
    @Nonnegative
    private int lostCount;

    Builder(@Nonnegative int size) {
      this.size = size;
    }

    HoldFilter lostCount(@Nonnegative int lostCount) {
      this.lostCount = lostCount;
      return new HoldFilter(this);
    }
  }
}
