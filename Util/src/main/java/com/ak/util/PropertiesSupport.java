package com.ak.util;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public enum PropertiesSupport implements Supplier<String> {
  LOG_LEVEL {
    @Override
    public String get() {
      return getProperty("level", "INFO", String::valueOf);
    }

    private <T> T getProperty(String name, T defaultValue, Function<String, T> function) {
      return function.apply(
          Optional.ofNullable(System.getProperty(SYSTEM_PROPERTY_PREFIX + "." + name, defaultValue.toString())).
              orElse("").trim()
      );
    }
  };

  private static final String SYSTEM_PROPERTY_PREFIX = "fx";
}
