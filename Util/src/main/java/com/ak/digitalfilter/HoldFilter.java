package com.ak.digitalfilter;

import java.util.Arrays;

final class HoldFilter extends AbstractBufferFilter {
  private final int lostCount;

  private HoldFilter(Builder builder) {
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
    private final int size;
    private int lostCount;

    Builder(int size) {
      this.size = size;
    }

    HoldFilter lostCount(int lostCount) {
      this.lostCount = lostCount;
      return new HoldFilter(this);
    }
  }
}
