package com.ak.fx.storage;

import com.ak.util.Strings;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

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

  @Override
  public Optional<String> get() {
    return Optional.of(preferences().get(KEY, Strings.EMPTY)).filter(Predicate.not(String::isBlank));
  }
}
