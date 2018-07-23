package com.ak.util;

import java.util.Optional;

public enum PropertiesSupport {
  CACHE {
    @Override
    public boolean check() {
      return Boolean.valueOf(
          Optional.ofNullable(System.getProperty(key(), Boolean.TRUE.toString())).orElse(Strings.EMPTY).trim());
    }
  };

  public abstract boolean check();

  public final String key() {
    return name().toLowerCase();
  }
}

