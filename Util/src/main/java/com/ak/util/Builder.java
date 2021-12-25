package com.ak.util;

import javax.annotation.Nonnull;

@FunctionalInterface
public interface Builder<T> {
  @Nonnull
  T build();
}
