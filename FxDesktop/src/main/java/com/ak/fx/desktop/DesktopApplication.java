package com.ak.fx.desktop;

import com.ak.comm.serial.ComServiceUtils;
import com.ak.fx.FxApplication;

public final class DesktopApplication extends FxApplication {
  public static void main(String[] args) {
    ComServiceUtils.PORTS.next("");
    launch(args);
  }
}
