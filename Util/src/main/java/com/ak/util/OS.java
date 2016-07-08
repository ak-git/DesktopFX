package com.ak.util;

import java.lang.reflect.Method;
import java.util.function.BooleanSupplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

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

    @Override
    public <T> void callApplicationMethod(@Nonnull String methodName, @Nonnull Class<? super T> type, @Nonnull T value) {
      try {
        Class<?> clazz = Class.forName("com.apple.eawt.Application");
        Method method = clazz.getMethod("getApplication");
        Method method2 = clazz.getMethod(methodName, type);
        method2.invoke(method.invoke(null), value);
      }
      catch (Exception ex) {
        Logger.getLogger(getClass().getName()).log(Level.WARNING, ex.getMessage(), ex);
      }
    }
  },
  UNIX {
    @Override
    public boolean getAsBoolean() {
      return PlatformUtil.isUnix();
    }
  };

  public <T> void callApplicationMethod(@Nonnull String methodName, @Nonnull Class<? super T> type, @Nonnull T value) {
    throw new UnsupportedOperationException(name());
  }

  @Nonnull
  public static OS get() {
    return Stream.of(values()).filter(BooleanSupplier::getAsBoolean).findFirst().orElseThrow(IllegalStateException::new);
  }
}
