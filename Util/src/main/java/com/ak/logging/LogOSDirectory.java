package com.ak.logging;

import java.nio.file.Path;
import java.nio.file.Paths;

import com.ak.util.OSDirectory;

import static com.ak.util.OSDirectories.USER_HOME_PATH;
import static com.ak.util.OSDirectories.VENDOR_ID;

public enum LogOSDirectory implements OSDirectory {
  WINDOWS {
    @Override
    public Path getDirectory() {
      return Paths.get(USER_HOME_PATH, "Application Data", VENDOR_ID);
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
