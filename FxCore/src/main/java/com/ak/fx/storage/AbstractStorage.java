package com.ak.fx.storage;

import javax.annotation.Nonnull;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

abstract class AbstractStorage<T> implements Storage<T> {
  @Nonnull
  private final Preferences preferences;

  AbstractStorage(@Nonnull Class<?> c, @Nonnull String nodeName) {
    preferences = Preferences.userNodeForPackage(c).node(nodeName);
  }

  @Nonnull
  final Preferences preferences() {
    return preferences;
  }

  @Override
  public void delete() throws BackingStoreException {
    preferences.clear();
  }
}
