package com.ak.util;

import javax.annotation.Nonnull;

public enum PropertiesSupport {
  OUT_CONVERTER_PATH;

  public String value() {
    return System.getProperty(key(), OSDirectories.VENDOR_ID).trim();
  }

  public void update(@Nonnull String value) {
    System.setProperty(key(), value);
  }

  public void clear() {
    System.clearProperty(key());
  }

  @Nonnull
  public String[] split() {
    return value().split(",");
  }

  String key() {
    return name().toLowerCase();
  }
}

