package com.ak.util;

import java.util.Optional;

public enum PropertiesSupport {
  TEST {
    @Override
    public boolean check() {
      return Boolean.valueOf(
          Optional.ofNullable(System.getProperty(key(), Boolean.FALSE.toString())).orElse(Strings.EMPTY).trim());
    }
  };

  public abstract boolean check();

  public final String key() {
    return name().toLowerCase();
  }
}

