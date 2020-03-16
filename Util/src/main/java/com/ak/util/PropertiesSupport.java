package com.ak.util;

import javax.annotation.Nonnull;

public enum PropertiesSupport {
  CONTEXT {
    @Override
    public String value() {
      return "aper,briko";
    }
  },
  CACHE {
    @Override
    public boolean check() {
      return Boolean.parseBoolean(value());
    }

    @Override
    public String value() {
      return System.getProperty(key(), Boolean.TRUE.toString()).trim();
    }
  },
  OUT_CONVERTER_PATH {
    @Override
    public String value() {
      return System.getProperty(key(), OSDirectories.VENDOR_ID).trim();
    }
  };

  public boolean check() {
    return !System.getProperty(key(), Strings.EMPTY).isEmpty();
  }

  public abstract String value();

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

  String key() {
    return name().toLowerCase();
  }
}

