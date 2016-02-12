package com.ak.fx.util;

import java.util.concurrent.TimeUnit;

public enum UIConstants {
  ;
  public static final int WIDTH_MIN = 1024;
  public static final int HEIGHT_MIN = 768;

  public static long uiDelay(TimeUnit timeUnit) {
    return timeUnit.convert(3L, TimeUnit.SECONDS);
  }
}

