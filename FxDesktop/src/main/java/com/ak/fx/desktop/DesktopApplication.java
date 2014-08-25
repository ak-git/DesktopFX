package com.ak.fx.desktop;

import com.ak.fx.FxApplication;
import com.ak.util.LogConfig;

public final class DesktopApplication extends FxApplication {
  public static void main(String[] args) {
    LogConfig.initLogger(DesktopApplication.class.getSimpleName(), () -> launch(args));
  }
}
