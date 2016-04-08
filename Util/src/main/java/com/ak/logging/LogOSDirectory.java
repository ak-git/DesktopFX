package com.ak.logging;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.ak.util.OSDirectory;

public enum LogOSDirectory implements OSDirectory {
  WINDOWS {
    @Override
    public Path getDirectory() {
      File appDataDir = null;
      try {
        String appDataEV = Optional.ofNullable(System.getenv("APPDATA")).orElse("");
        if (!appDataEV.isEmpty()) {
          appDataDir = new File(appDataEV);
        }
      }
      catch (SecurityException e) {
        Logger.getLogger(getClass().getName()).log(Level.WARNING, e.getMessage(), e);
      }

      if (appDataDir != null && appDataDir.isDirectory()) {
        return Paths.get(appDataDir.getPath(), VENDOR_ID);
      }
      else {
        return Paths.get(USER_HOME_PATH, "Application Data", VENDOR_ID);
      }
    }
  },
  MAC {
    @Override
    public Path getDirectory() {
      return Paths.get(USER_HOME_PATH, "Library", "Application Support", VENDOR_ID);
    }
  },
  UNIX {
    @Override
    public Path getDirectory() {
      return Paths.get(USER_HOME_PATH);
    }
  };

  private static final String USER_HOME_PATH = AccessController.doPrivileged(
      (PrivilegedAction<String>) () -> Optional.ofNullable(System.getProperty("user.home")).orElse(""));
  private static final String VENDOR_ID = Stream.of(LogOSDirectory.class.getPackage().getName().split("\\.")).limit(2).
      collect(Collectors.joining("."));
}
