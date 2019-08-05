package com.ak.util;

import javax.annotation.Nonnull;

public enum PropertiesSupport {
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
      return System.getProperty(key(), OSDirectory.VENDOR_ID).trim();
    }
  };

  private static final String PROPERTIES = "properties";

  public boolean check() {
    return !System.getProperty(key(), Strings.EMPTY).isEmpty();
  }

  public abstract String value();

  public final void set(@Nonnull String value) {
    System.setProperty(key(), value);
  }

  public final void clear() {
    System.clearProperty(key());
  }

  String key() {
    return name().toLowerCase();
  }

  public static String addExtension(String fileName) {
    return String.format("%s.%s", fileName, PROPERTIES);
  }
}

