package com.ak.logging;

import java.nio.file.Path;

import com.ak.util.OSDirectories;
import com.ak.util.OSDirectory;

public enum OutputOSDirectory implements OSDirectory {
  WINDOWS, MAC, UNIX;

  private static final String[] PATHS = {"/Downloads/", "/Documents/"};

  @Override
  public Path getDirectory() {
    return OSDirectories.getDirectory(PATHS);
  }
}
