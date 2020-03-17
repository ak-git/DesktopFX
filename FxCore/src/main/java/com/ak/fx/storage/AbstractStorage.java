package com.ak.fx.storage;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.annotation.Nonnull;

abstract class AbstractStorage<T> implements Storage<T> {
  private final Preferences preferences;

  AbstractStorage(@Nonnull Class<?> c, @Nonnull String nodeName) {
    preferences = Preferences.userNodeForPackage(c).node(nodeName);
  }

  final Preferences preferences() {
    return preferences;
  }

  @Override
  public void delete() throws BackingStoreException {
    preferences.clear();
  }
}
