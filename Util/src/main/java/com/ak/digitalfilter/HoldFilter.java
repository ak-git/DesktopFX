package com.ak.digitalfilter;

import java.util.Arrays;

import javax.annotation.Nonnegative;

final class HoldFilter extends AbstractBufferFilter {
  private static final int LOST_COUNT = 1;

  HoldFilter(@Nonnegative int size) {
    super(size);
  }

  @Override
  int apply(int nowIndex) {
    return 0;
  }

  int[] getSorted() {
    int[] buffer = buffer();
    Arrays.sort(buffer);
    return Arrays.copyOfRange(buffer, LOST_COUNT, buffer.length - LOST_COUNT);
  }
}
