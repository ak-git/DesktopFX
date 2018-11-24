package com.ak.digitalfilter;

import javax.annotation.Nonnegative;

abstract class AbstractExcessBufferFilter extends AbstractBufferFilter {
  AbstractExcessBufferFilter(@Nonnegative int size) {
    super(size + 1);
  }

  @Nonnegative
  @Override
  public final double getDelay() {
    return ((length() - 1) - 1) / 2.0;
  }
}