package com.ak.fx.storage;

import com.ak.util.Strings;

import javax.annotation.Nonnull;
import java.util.Objects;

public final class StringStorage extends AbstractStorage<String> {
  private static final String KEY = "key";

  public StringStorage(Class<?> c, String nodeName) {
    super(c, nodeName);
  }

  @Override
  public void save(String value) {
    preferences().put(KEY, Objects.requireNonNull(value));
  }

  @Override
  public void update(String value) {
    throw new UnsupportedOperationException(value);
  }

  @Nonnull
  @Override
  public String get() {
    return preferences().get(KEY, Strings.EMPTY);
  }
}
