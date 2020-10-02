package com.ak.util;

import javax.annotation.Nonnull;

public enum PropertiesSupport {
  CACHE;

  public final boolean check() {
    return Boolean.parseBoolean(value());
  }

  public final String value() {
    return System.getProperty(key(), Boolean.TRUE.toString()).trim();
  }

  public final void update(@Nonnull String value) {
    System.setProperty(key(), value);
  }

  public final void clear() {
    System.clearProperty(key());
  }

  @Nonnull
  public final String[] split() {
    return value().split(",");
  }

  private String key() {
    return name().toLowerCase();
  }
}

