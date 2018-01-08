package com.ak.util;

import java.util.Optional;

public class PropertiesSupport {
  public static final boolean IS_TEST = Boolean.valueOf(
      Optional.ofNullable(System.getProperty("test", Boolean.FALSE.toString())).orElse(Strings.EMPTY).trim());

  private PropertiesSupport() {
  }
}

