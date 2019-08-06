package com.ak.util;

import java.util.function.BooleanSupplier;
import java.util.stream.Stream;

public enum OS implements BooleanSupplier {
  WINDOWS,
  MAC,
  UNIX {
    @Override
    public boolean getAsBoolean() {
      return !(WINDOWS.getAsBoolean() || MAC.getAsBoolean());
    }
  };

  @Override
  public boolean getAsBoolean() {
    return System.getProperty("os.name").toLowerCase().startsWith(name().toLowerCase());
  }

  public static OS get() {
    return Stream.of(values()).filter(BooleanSupplier::getAsBoolean).findFirst().orElseThrow(IllegalStateException::new);
  }
}
