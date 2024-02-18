package com.ak.fx.storage;

import javax.annotation.Nullable;
import java.util.prefs.BackingStoreException;

public interface Storage<T> {
  void save(T t);

  void update(T t);

  @Nullable
  T get();

  void delete() throws BackingStoreException;
}
