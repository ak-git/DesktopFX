package com.ak.util;

import java.util.function.BooleanSupplier;
import java.util.stream.Stream;

import com.sun.javafx.PlatformUtil;

public enum OS implements BooleanSupplier {
  WINDOWS {
    @Override
    public boolean getAsBoolean() {
      return PlatformUtil.isWindows();
    }
  },
  MAC {
    @Override
    public boolean getAsBoolean() {
      return PlatformUtil.isMac();
    }
  },
  UNIX {
    @Override
    public boolean getAsBoolean() {
      return true;
    }
  };

  public static OS get() {
    return Stream.of(values()).filter(BooleanSupplier::getAsBoolean).findFirst().orElseThrow(IllegalStateException::new);
  }
}
