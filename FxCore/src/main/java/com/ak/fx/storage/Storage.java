package com.ak.fx.storage;

import java.util.Optional;
import java.util.prefs.BackingStoreException;

public interface Storage<T> {
  void save(T t);

  void update(T t);

  Optional<T> get();

  void delete() throws BackingStoreException;
}
