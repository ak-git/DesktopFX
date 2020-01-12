package com.ak.fx.storage;

import java.util.prefs.BackingStoreException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface Storage<T> {
  void save(@Nonnull T t);

  void update(@Nonnull T t);

  @Nullable
  T get();

  void delete() throws BackingStoreException;
}
