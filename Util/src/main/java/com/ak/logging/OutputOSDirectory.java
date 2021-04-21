package com.ak.logging;

import java.nio.file.Path;

import com.ak.util.OSDirectories;
import com.ak.util.OSDirectory;

import static com.ak.util.OSDirectories.VENDOR_ID;

public enum OutputOSDirectory implements OSDirectory {
  WINDOWS, MAC, UNIX;

  private static final String[] CANDIDATES = {"Downloads", "Documents"};

  @Override
  public Path getDirectory() {
    return OSDirectories.getDirectory(CANDIDATES).resolve(VENDOR_ID);
  }
}
