package com.ak.logging;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.ak.util.OSDirectory;

public enum OutputOSDirectory implements OSDirectory {
  WINDOWS, MAC, UNIX;

  @Override
  public Path getDirectory() {
    String path = "/Downloads/";
    File file = new File(USER_HOME_PATH, path);
    if (!file.exists() || !file.isDirectory() || file.isHidden()) {
      path = "/Documents/";
    }
    return Paths.get(USER_HOME_PATH, path, "Aper");
  }
}
