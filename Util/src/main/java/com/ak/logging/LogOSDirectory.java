package com.ak.logging;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ak.util.OSDirectory;

import static com.ak.util.OSDirectories.USER_HOME_PATH;
import static com.ak.util.OSDirectories.VENDOR_ID;
import static com.ak.util.Strings.EMPTY;

public enum LogOSDirectory implements OSDirectory {
  WINDOWS {
    @Override
    public Path getDirectory() {
      File appDataDir = null;
      try {
        String appDataEV = Optional.ofNullable(System.getenv("APPDATA")).orElse(EMPTY);
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
  }
}
