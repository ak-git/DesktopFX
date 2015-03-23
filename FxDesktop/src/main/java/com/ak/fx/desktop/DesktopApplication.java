package com.ak.fx.desktop;

import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ak.fx.AbstractFxApplication;

public final class DesktopApplication extends AbstractFxApplication {
  static {
    try {
      System.setProperty("java.util.logging.config.file",
          Paths.get(DesktopApplication.class.getResource("logging.properties").toURI()).toAbsolutePath().toString());
    }
    catch (URISyntaxException e) {
      Logger.getLogger(DesktopApplication.class.getName()).log(Level.SEVERE, e.getMessage(), e);
    }
  }

  public static void main(String[] args) {
    launch(args);
  }
}
