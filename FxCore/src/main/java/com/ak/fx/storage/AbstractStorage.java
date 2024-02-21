package com.ak.fx.storage;

import java.util.Objects;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

abstract class AbstractStorage<T> implements Storage<T> {
  private final Preferences preferences;

  AbstractStorage(Class<?> c, String nodeName) {
    preferences = Preferences.userNodeForPackage(c).node(Objects.requireNonNull(nodeName));
  }

  final Preferences preferences() {
    return preferences;
  }

  @Override
  public void delete() throws BackingStoreException {
    preferences.clear();
  }
}
