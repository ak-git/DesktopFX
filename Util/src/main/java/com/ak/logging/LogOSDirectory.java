package com.ak.logging;

import java.nio.file.Path;

import com.ak.util.OSDirectories;
import com.ak.util.OSDirectory;
import com.ak.util.Strings;

import static com.ak.util.OSDirectories.VENDOR_ID;

public enum LogOSDirectory implements OSDirectory {
  WINDOWS {
    @Override
    public Path getDirectory() {
      return OSDirectories.getDirectory("Application Data/" + VENDOR_ID);
    }
  },
  MAC {
    @Override
    public Path getDirectory() {
      return OSDirectories.getDirectory("Library/Application Support/" + VENDOR_ID);
    }
  },
  UNIX {
    @Override
    public Path getDirectory() {
      return OSDirectories.getDirectory(Strings.EMPTY);
    }
  }
}
