package com.ak.storage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface Storage<T> {
  void save(@Nonnull T t);

  void update(@Nonnull T t);

  @Nullable
  T get();

  void delete();
}
