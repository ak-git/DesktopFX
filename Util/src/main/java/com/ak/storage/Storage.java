package com.ak.storage;

import javax.annotation.Nonnull;

public interface Storage<T> {
  void save(@Nonnull T t);

  void update(@Nonnull T t);

  @Nonnull
  T get();

  void delete();
}
