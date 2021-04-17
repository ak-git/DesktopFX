package com.ak.fx.storage;

import javax.annotation.Nonnull;

import com.ak.util.Strings;

public final class StringStorage extends AbstractStorage<String> {
  private static final String KEY = "key";

  public StringStorage(@Nonnull Class<?> c, @Nonnull String nodeName) {
    super(c, nodeName);
  }

  @Override
  public void save(@Nonnull String value) {
    preferences().put(KEY, value);
  }

  @Override
  public void update(@Nonnull String value) {
    throw new UnsupportedOperationException(value);
  }

  @Nonnull
  @Override
  public String get() {
    return preferences().get(KEY, Strings.EMPTY);
  }
}
